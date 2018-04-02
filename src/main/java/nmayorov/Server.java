package nmayorov;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import nmayorov.message.Message;
import nmayorov.message.NameAccepted;
import nmayorov.message.NameRequest;
import nmayorov.message.ServerText;

public class Server {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    private static final int HISTORY_SIZE = 100;

    private final InetSocketAddress address;
    private ServerSocketChannel serverChannel;
    private Selector selector;

    private HashMap<String, Connection> connections;
    private CircularFifoQueue<byte[]> messageHistory;

    private Thread acceptingThread;
    private final ArrayDeque<Connection> connectionsToRegister;
    private LinkedList<Connection> connectionsToRemove;

    private class ConnectionAcceptor implements Runnable {
        @Override
        public void run() {
            while (serverChannel.isOpen()) {
                try {
                    SocketChannel channel = serverChannel.accept();
                    synchronized (connectionsToRegister) {
                        connectionsToRegister.add(new Connection(channel));
                    }
                    selector.wakeup();
                    LOGGER.info("Connection from " + channel.getRemoteAddress().toString());
                } catch (IOException e) {
                    LOGGER.warning("Error accepting connection");
                }
            }
        }
    }

    public Server(InetSocketAddress address) {
        this.address = address;
        connectionsToRegister = new ArrayDeque<>();
        connectionsToRemove = new LinkedList<>();
        connections = new HashMap<>();
        messageHistory = new CircularFifoQueue<>(HISTORY_SIZE);
        acceptingThread = new Thread(new ConnectionAcceptor());
    }

    private void handleConnectionsToRegister() {
        Connection connection;
        synchronized (connectionsToRegister) {
            connection = connectionsToRegister.poll();
        }
        while (connection != null) {
            try {
                connection.channel.configureBlocking(false);
                connection.channel.register(selector,
                                            SelectionKey.OP_READ | SelectionKey.OP_WRITE, connection);
                connection.send(new ServerText("Enter your name."));
                connection.send(new NameRequest());
            } catch (IOException e) {
                LOGGER.warning("Error registering connection");
            }
            synchronized (connectionsToRegister) {
                connection = connectionsToRegister.poll();
            }
        }
    }

    private void handleConnectionsToRemove() {
        Iterator<Connection> it = connectionsToRemove.iterator();
        while (it.hasNext()) {
            Connection connection = it.next();
            if (connection.nothingToWrite()) {
                closeConnection(connection);
                it.remove();
            }
        }
    }

    public void start() throws IOException {
        LOGGER.log(Level.INFO, "Starting server at " + address);

        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(address);
        selector = Selector.open();

        acceptingThread.start();
        while (serverChannel.isOpen()) {
            handleConnectionsToRegister();
            handleConnectionsToRemove();

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
                if (key.isValid() && key.isReadable()) {
                    handleRead(key);
                }
                if (key.isValid() && key.isWritable()) {
                    handleWrite(key);
                }
            }
        }
        try {
            acceptingThread.join();
        } catch (InterruptedException e) {
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

    public void removeConnection(Connection connection) {
        connectionsToRemove.add(connection);
    }

    public void saveMessageInHistory(byte[] bytes) {
        messageHistory.add(bytes);
    }

    private void closeConnection(Connection connection) {
        if (connections.containsKey(connection.name)) {
            try {
                connection.channel.close();
            } catch (Exception e) {
            }
            connections.remove(connection.name);
            broadcast(new ServerText(connection.name + " left the chat."));
            LOGGER.info(String.format("User %s disconnected, % d left", connection.name, connections.size()));
        }
    }

    private void handleWrite(SelectionKey key) {
        Connection connection = (Connection) key.attachment();
        try {
            connection.write();
        } catch (IOException e) {
            key.cancel();
            closeConnection(connection);
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
            closeConnection(connection);
            return;
        }

        Message message = Message.getNext(connection.getReadBuffer());
        while (message != null) {
            message.handleServerReceive(this, connection);
            message = Message.getNext(connection.getReadBuffer());
        }
    }
}
