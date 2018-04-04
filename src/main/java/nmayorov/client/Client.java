package nmayorov.client;

import nmayorov.message.MessageHandler;
import nmayorov.message.MessageHandlerFactory;
import nmayorov.server.Connection;
import nmayorov.message.NameAccepted;
import nmayorov.message.NameSent;
import nmayorov.message.UserText;
import nmayorov.message.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class Client implements Runnable {
    private static final int PAUSE_BETWEEN_IO_CYCLES_MS = 100;

    private DisplaySystem displaySystem;
    private InputSystem inputSystem;

    private Connection connection;
    private Selector selector;

    private volatile boolean acceptInput;
    private Thread inputThread;

    private boolean run;

    MessageHandlerFactory handlers;

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
                    connection.send(new UserText(connection.name, input));
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
        registerHandlers();
    }

    private void registerHandlers() {
        handlers = new MessageHandlerFactory();

        handlers.register(Message.Type.NAME_REQUEST, (message, connection) -> {
            String name = inputSystem.readName();
            connection.send(new NameSent(name));
        });

        handlers.register(Message.Type.NAME_ACCEPTED, (message, connection) -> {
            String name = ((NameAccepted) message).getName();
            synchronized (connection) {
                connection.name = name;
            }
            startToAcceptInput();
        });

        handlers.register(Message.Type.DISCONNECT, (message, connection) -> run = false);
    }

    public void connect(InetSocketAddress address) throws IOException {
        connection = new Connection(SocketChannel.open());
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
            for (SelectionKey key : selectedKeys) {
                selectedKeys.remove(key);
                if (key.isValid() && key.isReadable()) {

                    try {
                        connection.read();
                    } catch (IOException e) {
                        run = false;
                        break;
                    }

                    Message message = Message.getNext(connection.getReadBuffer());
                    while (message != null) {
                        displaySystem.displayMessage(message);
                        MessageHandler handler = handlers.get(message.getType());
                        if (handler != null) {
                            handler.execute(message, connection);
                        }
                        message = Message.getNext(connection.getReadBuffer());
                    }
                }

                if (key.isValid() && key.isWritable()) {
                    try {
                        connection.write();
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
}
