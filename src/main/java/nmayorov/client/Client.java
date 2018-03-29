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
    private DisplaySystem displaySystem;
    private InputSystem inputSystem;

    private Connection connection;
    private Selector selector;

    private volatile boolean acceptInput;
    private Thread inputThread = new Thread(new InputThread());

    public synchronized void setName(String name) {
        connection.name = name;
    }
    public synchronized String getName() {
        return connection.name;
    }

    class InputThread implements Runnable {
        @Override
        public void run() {
            while (acceptInput) {
                String input = inputSystem.readChatInput();
                connection.send(new UserText(getName(), input));
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
    }

    public void connect(InetSocketAddress address) throws IOException {
        connection = new Connection(SocketChannel.open());
        connection.channel.connect(address);

        selector = Selector.open();
        connection.channel.configureBlocking(false);
        connection.channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    public void run() {
        boolean broken = false;
        while (connection.channel.isConnected()) {
            int selected = 0;
            try {
                selected = selector.select();
            } catch (IOException e) {
                broken = true;
            }

            if (broken) {
                break;
            }

            if (selected == 0) {
                continue;
            }

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            for (SelectionKey key : selectedKeys) {
                selectedKeys.remove(key);
                if (key.isReadable()) {

                    try {
                        connection.read();
                    } catch (IOException e) {
                        broken = true;
                        break;
                    }

                    Message message = Message.getNext(connection.getReadBuffer());
                    while (message != null) {
                        displaySystem.displayMessage(message);
                        message.handleClientReceive(this, connection);
                        message = Message.getNext(connection.getReadBuffer());
                    }
                }

                if (key.isWritable()) {
                    try {
                        connection.write();
                    } catch (IOException e) {
                        broken = true;
                        break;
                    }

                }
            }
        }
        displaySystem.displayText("Server was shutdown. Send anything to exit.");
        stopToAcceptInput();
    }
}
