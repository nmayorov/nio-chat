package nmayorov.client;

import nmayorov.connection.ModeChangeRequestQueue;
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
    private final DisplaySystem displaySystem;
    private final InputSystem inputSystem;

    private NioSocketConnection connection;
    private Selector selector;

    private volatile boolean acceptInput;
    private final Thread inputThread;

    private volatile boolean run;

    private final MessageHandlerFactory messageHandlers;
    private ModeChangeRequestQueue modeChangeRequestQueue;

    class InputLoop implements Runnable {
        @Override
        public void run() {
            while (acceptInput) {
                String input = inputSystem.readChatInput();
                connection.write(new UserText(connection.name, input).getBytes());
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
        this.inputThread = new Thread(new InputLoop());
        this.messageHandlers = registerMessages();
    }

    private MessageHandlerFactory registerMessages() {
        MessageHandlerFactory messageHandlers = new MessageHandlerFactory();

        messageHandlers.register(Message.Type.NAME_REQUEST, (message, connection) -> {
            String name = inputSystem.readName();
            connection.write(new NameSent(name).getBytes());
        });

        messageHandlers.register(Message.Type.NAME_ACCEPTED, (message, connection) -> {
            connection.name = ((NameAccepted) message).getName();;
            startToAcceptInput();
        });

        messageHandlers.register(Message.Type.DISCONNECT, (message, connection) -> stop());

        return messageHandlers;
    }

    public void connect(InetSocketAddress address) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(address);

        selector = Selector.open();
        this.modeChangeRequestQueue = new ModeChangeRequestQueue(selector);

        connection = new NioSocketConnection(selector, socketChannel, modeChangeRequestQueue);
        connection.messageBuffer = new ChatMessageBuffer();
    }

    public void run() {
        run = true;
        while (run) {
            modeChangeRequestQueue.process();

            try {
                selector.select();
            } catch (IOException e) {
                run = false;
                continue;
            }

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (!key.isValid()) {
                    continue;
                }

                if (key.isReadable()) {
                    try {
                        connection.readFromChannel();
                    } catch (IOException e) {
                        run = false;
                        break;
                    }

                    connection.messageBuffer.put(connection.getData());
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
                } else if (key.isWritable()) {
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
