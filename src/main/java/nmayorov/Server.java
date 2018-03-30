package nmayorov;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Collections;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import nmayorov.message.Message;
import nmayorov.message.NameAccepted;
import nmayorov.message.NameRequest;
import nmayorov.message.ServerText;

public class Server {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
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
        LOGGER.log(Level.INFO, "Starting server at " + address);
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(address);
        serverChannel.configureBlocking(false);
        selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
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

    public void broadcast(Message message) {
        byte[] bytes = message.getBytes();
        for (Connection connection : connections.values()) {
            connection.send(bytes);
        }
    }

    public Set<String> getConnectedUsers() {
        return Collections.unmodifiableSet(connections.keySet());
    }

    public void registerNewConnection(String name, Connection connection) {
        if (name.isEmpty()) {
            connection.send(new ServerText("The name must be non-empty."));
            connection.send(new NameRequest());
        } else if (connections.containsKey(name)) {
            connection.send(new ServerText("The name is already occupied."));
            connection.send(new NameRequest());
        } else {
            connection.send(new NameAccepted(name));
            connection.name = name;
            connection.send(new ServerText("Welcome to the chat! Type \\help for help."));
            connections.put(name, connection);
            broadcast(new ServerText(name + " joined the chat!"));
            for (byte[] message : messageHistory) {
                connection.send(message);
            }
            LOGGER.log(Level.INFO, String.format("User %d is registered as %s", connections.size(), name));
        }
    }

    public void renameConnection(String newName, Connection connection) {
        if (newName.isEmpty()) {
            connection.send(new ServerText("The name must be non-empty."));
        } else if (connections.containsKey(newName)) {
            connection.send(new ServerText("The name is already occupied."));
        } else {
            connection.send(new NameAccepted(newName));
            String oldName = connection.name;
            connections.remove(connection.name);
            connection.name = newName;
            connections.put(connection.name, connection);
            broadcast(new ServerText(String.format("%s is now %s.", oldName, newName)));
            LOGGER.log(Level.INFO, String.format("User %s is renamed to %s", oldName, newName));
        }
    }

    public void saveMessageInHistory(byte[] bytes) {
        messageHistory.add(bytes);
    }

    private void cancelConnection(Connection connection) {
        if (connections.containsKey(connection.name)) {
            connections.remove(connection.name);
            broadcast(new ServerText(connection.name + " left the chat."));
            LOGGER.log(Level.INFO, String.format("User %s disconnected", connection.name));
        }
    }

    private void handleAccept(SelectionKey serverKey) {
        Connection connection = null;
        try {
            ServerSocketChannel serverChannel = (ServerSocketChannel) serverKey.channel();
            connection = new Connection(serverChannel.accept());
            LOGGER.log(Level.INFO,
                       "Connection attempt from " + connection.channel.getRemoteAddress().toString());
            connection.channel.configureBlocking(false);
            connection.channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, connection);
            connection.send(new ServerText("Enter your name."));
            connection.send(new NameRequest());
        } catch (IOException e) {
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

        Message message = Message.getNext(connection.getReadBuffer());
        while (message != null) {
            message.handleServerReceive(this, connection);
            message = Message.getNext(connection.getReadBuffer());
        }
    }
}
