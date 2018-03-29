package nmayorov;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.Iterator;

import nmayorov.message.Message;
import nmayorov.message.MessageReader;
import nmayorov.message.NameRequest;
import nmayorov.message.ServerText;
import org.apache.commons.collections4.queue.CircularFifoQueue;

public class Server {
    private static final int HISTORY_SIZE = 100;

    private ServerSocketChannel serverChannel;
    private Selector selector;

    private HashMap<String, Connection> connections;
    private CircularFifoQueue<byte[]> messageHistory;

    public Server() {
        connections = new HashMap<>();
        messageHistory = new CircularFifoQueue<>(HISTORY_SIZE);
    }

    public void start(InetSocketAddress address) throws IOException {
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(address);
        serverChannel.configureBlocking(false);
        selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void broadcast(Message message) {
        byte[] bytes = message.getBytes();
        for (Connection connection : connections.values()) {
            connection.send(bytes);
        }
    }

    public boolean hasUser(String name) {
        return connections.containsKey(name);
    }

    public Iterator<String> listUsers() { return connections.keySet().iterator(); }

    public void addConnection(Connection connection) {
        connections.put(connection.name, connection);
    }

    public void renameConnection(Connection connection, String newName) {
        connections.remove(connection.name);
        connection.name = newName;
        addConnection(connection);
    }

    public Iterator<byte[]> getMessageHistory() {
        return messageHistory.iterator();
    }

    public void addMessageToHistory(byte[] bytes) {
        messageHistory.add(bytes);
    }

    private void cancelConnection(Connection connection) {
        if (connections.containsKey(connection.name)) {
            connections.remove(connection.name);
            broadcast(new ServerText(connection.name + " left the chat."));
        }
    }

    public void run() {
        while (serverChannel.isOpen()) {
            int selected;
            try {
                selected = selector.select();
            } catch (IOException e) {
                selected = 0;
            }

            if (selected == 0) {
                continue;
            }

            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();
                if (key.isValid() && key.isAcceptable()) {
                    handleAccept(key);
                }
                if (key.isValid() && key.isReadable()) {
                    handleRead(key);
                }
                if (key.isValid() && key.isWritable()) {
                    handleWrite(key);
                }
            }
        }
    }

    private void handleAccept(SelectionKey serverChannelKey) {
        SelectionKey sockerChannelKey = null;
        Connection connection = null;
        try {
            ServerSocketChannel serverChannel = (ServerSocketChannel) serverChannelKey.channel();
            connection = new Connection(serverChannel.accept());
            connection.channel.configureBlocking(false);
            sockerChannelKey = connection.channel.register(
                    selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, connection);
            connection.send(new ServerText("Enter your name."));
            connection.send(new NameRequest());
        } catch (IOException e) {
            if (sockerChannelKey != null) {
                sockerChannelKey.cancel();
            }
            if (connection != null) {
                cancelConnection(connection);
            }
        }
    }

    private void handleWrite(SelectionKey key) {
        Connection connection = (Connection) key.attachment();
        try {
            connection.write();
        } catch (IOException e) {
            key.cancel();
            cancelConnection(connection);
        }
    }

    private void handleRead(SelectionKey key) {
        Connection connection = (Connection) key.attachment();

        int read;
        try {
            read = connection.read();
        } catch (IOException e) {
            read = -1;
        }

        if (read == -1) {
            key.cancel();
            cancelConnection(connection);
            return;
        };

        Message message = MessageReader.next(connection.getReadBuffer());
        while (message != null) {
            message.handleServerReceive(this, connection);
            message = MessageReader.next(connection.getReadBuffer());
        }
    }
}
