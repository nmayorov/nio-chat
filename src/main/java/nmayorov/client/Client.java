package nmayorov.client;

import nmayorov.connection.NioSocketConnection;
import nmayorov.message.ChatMessageBuffer;
import nmayorov.message.MessageFactory;
import nmayorov.message.MessageHandler;
import nmayorov.message.MessageHandlerFactory;
import nmayorov.message.NameAccepted;
import nmayorov.message.NameSent;
import nmayorov.message.UserText;
import nmayorov.message.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Client implements Runnable {
    private static final int PAUSE_BETWEEN_IO_CYCLES_MS = 100;

    private final DisplaySystem displaySystem;
    private final InputSystem inputSystem;

    private NioSocketConnection connection;
    private Selector selector;

    private volatile boolean acceptInput;
    private final Thread inputThread;

    private volatile boolean run;

    private final MessageHandlerFactory messageHandlers;

    class InputLoop implements Runnable {
        private final Client client;
        InputLoop(Client client) {
            this.client = client;
        }

        @Override
        public void run() {
            while (acceptInput) {
                String input = inputSystem.readChatInput();
                synchronized (connection) {
                    connection.write(new UserText(connection.name, input).getBytes());
                }
                synchronized (client) {
                    client.notify();
                }
            }
        }
    }

    private void startToAcceptInput() {
        if (inputThread.isAlive()) {
            return;
        }
        acceptInput = true;
        inputThread.start();
    }

    private void stopToAcceptInput() {
        acceptInput = false;
        while (inputThread.isAlive()) {
            try {
                inputThread.join();
            } catch (InterruptedException e) {
            }
        }
    }

    public Client(InputSystem inputSystem, DisplaySystem displaySystem) {
        this.inputSystem = inputSystem;
        this.displaySystem = displaySystem;
        this.inputThread = new Thread(new InputLoop(this));
        this.messageHandlers = registerMessages();
    }

    private MessageHandlerFactory registerMessages() {
        MessageHandlerFactory messageHandlers = new MessageHandlerFactory();

        messageHandlers.register(Message.Type.NAME_REQUEST, (message, connection) -> {
            String name = inputSystem.readName();
            connection.write(new NameSent(name).getBytes());
        });

        messageHandlers.register(Message.Type.NAME_ACCEPTED, (message, connection) -> {
            String name = ((NameAccepted) message).getName();
            synchronized (connection) {
                connection.name = name;
            }
            startToAcceptInput();
        });

        messageHandlers.register(Message.Type.DISCONNECT, (message, connection) -> stop());

        return messageHandlers;
    }

    public void connect(InetSocketAddress address) throws IOException {
        connection = new NioSocketConnection(SocketChannel.open());
        connection.messageBuffer = new ChatMessageBuffer();
        connection.channel.connect(address);

        selector = Selector.open();
        connection.channel.configureBlocking(false);
        connection.channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    public void run() {
        run = true;
        while (run) {
            try {
                synchronized (this) {
                    wait(PAUSE_BETWEEN_IO_CYCLES_MS);
                }
            } catch (InterruptedException e) {
            }

            int selected = 0;
            try {
                selected = selector.select();
            } catch (IOException e) {
                run = false;
            }

            if (selected == 0) {
                continue;
            }

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isValid() && key.isReadable()) {
                    try {
                        connection.readFromChannel();
                    } catch (IOException e) {
                        run = false;
                        break;
                    }

                    connection.messageBuffer.put(connection.read());
                    byte[] messageData = connection.messageBuffer.getNextMessage();
                    while (messageData != null) {
                        Message message = MessageFactory.createFromBytes(messageData);
                        displaySystem.displayMessage(message);
                        MessageHandler handler = messageHandlers.get(message.getType());
                        if (handler != null) {
                            handler.execute(message, connection);
                        }
                        messageData = connection.messageBuffer.getNextMessage();
                    }
                }

                if (key.isValid() && key.isWritable()) {
                    try {
                        connection.writeToChannel();
                    } catch (IOException e) {
                        run = false;
                        break;
                    }

                }
            }
        }
        displaySystem.displayText("Disconnected from server. Input anything to exit.");
        stopToAcceptInput();
    }

    public void stop() {
        run = false;
    }
}
