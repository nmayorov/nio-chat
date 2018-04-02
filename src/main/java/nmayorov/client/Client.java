package nmayorov.client;

import nmayorov.Connection;
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

    private enum Status {RUNNING, STOPPED, DISCONNECT};
    private volatile Status status;

    public synchronized void setName(String name) {
        connection.name = name;
    }
    public synchronized String getName() {
        return connection.name;
    }

    class InputLoop implements Runnable {
        private final Client client;
        InputLoop(Client client) {
            this.client = client;
        }

        @Override
        public void run() {
            while (acceptInput) {
                String input = inputSystem.readChatInput();
                connection.send(new UserText(getName(), input));
                synchronized (client) {
                    client.notify();
                }
            }
        }
    }

    public void startToAcceptInput() {
        if (inputThread.isAlive()) {
            return;
        }
        acceptInput = true;
        inputThread.start();
    }

    public void stopToAcceptInput() {
        acceptInput = false;
        while (inputThread.isAlive()) {
            try {
                inputThread.join();
            } catch (InterruptedException e) {
            }
        }
    }

    public String inputName() {
        return inputSystem.readName();
    }

    public Client(InputSystem inputSystem, DisplaySystem displaySystem) {
        this.inputSystem = inputSystem;
        this.displaySystem = displaySystem;
        this.inputThread = new Thread(new InputLoop(this));
    }

    public void connect(InetSocketAddress address) throws IOException {
        connection = new Connection(SocketChannel.open());
        connection.channel.connect(address);

        selector = Selector.open();
        connection.channel.configureBlocking(false);
        connection.channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    public void run() {
        status = Status.RUNNING;
        while (status == Status.RUNNING) {
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
                status = Status.DISCONNECT;
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
                        status = Status.DISCONNECT;
                        break;
                    }

                    Message message = Message.getNext(connection.getReadBuffer());
                    while (message != null) {
                        displaySystem.displayMessage(message);
                        message.handleClientReceive(this, connection);
                        message = Message.getNext(connection.getReadBuffer());
                    }
                }

                if (key.isValid() && key.isWritable()) {
                    try {
                        connection.write();
                    } catch (IOException e) {
                        status = Status.DISCONNECT;
                        break;
                    }

                }
            }
        }

        switch (status) {
            case STOPPED:
                displaySystem.displayText("Input anything to exit.");
                break;
            case DISCONNECT:
                displaySystem.displayText("Connection to server interrupted. Input anything to exit.");
            case RUNNING:
                assert false;
        }

        stopToAcceptInput();
    }

    public void stop() {
        status = Status.STOPPED;
    }
}
