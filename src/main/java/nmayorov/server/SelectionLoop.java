package nmayorov.server;

import nmayorov.connection.Connection;
import nmayorov.connection.ConnectionEvent;
import nmayorov.connection.ModeChangeRequest;
import nmayorov.connection.NioSocketConnection;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

class SelectionLoop implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(SelectionLoop.class.getName());

    private final Selector selector;

    private final ArrayBlockingQueue<Connection> newConnections;
    private final ArrayBlockingQueue<ConnectionEvent> connectionEvents;
    private final ConcurrentLinkedDeque<ModeChangeRequest> modeChangeRequests;

    SelectionLoop(ServerSocketChannel serverSocketChannel,
                  ArrayBlockingQueue<Connection> newConnections,
                  ArrayBlockingQueue<ConnectionEvent> connectionEvents) throws IOException {
        serverSocketChannel.configureBlocking(false);
        this.selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        this.newConnections = newConnections;
        this.connectionEvents = connectionEvents;
        this.modeChangeRequests = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void run() {
        while (true) {
            processChangeRequests();

            try {
                selector.select();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error selecting IO channels, server might be broken", e);
                continue;
            }

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
            while (selectedKeys.hasNext()) {
                SelectionKey key = selectedKeys.next();
                selectedKeys.remove();
                if (!key.isValid()) {
                    continue;
                }
                if (key.isAcceptable()) {
                    accept(key);
                } else if (key.isReadable()) {
                    read(key);
                } else if (key.isWritable()) {
                    write(key);
                }
            }
        }
    }

    private void processChangeRequests() {
        ModeChangeRequest request = modeChangeRequests.poll();
        while (request != null) {
            SelectionKey key = request.connection.channel.keyFor(this.selector);
            if (key != null && key.isValid()) {
                key.interestOps(request.ops);
            }
            request = modeChangeRequests.poll();
        }
    }

    private void accept(SelectionKey key) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        SocketChannel socketChannel;
        String remoteAddress;
        try {
            socketChannel = serverSocketChannel.accept();
            remoteAddress = socketChannel.getRemoteAddress().toString();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error accepting connection", e);
            return;
        }
        LOGGER.info("Connection from " + remoteAddress);

        NioSocketConnection connection;
        try {
            connection = new NioSocketConnection(selector, socketChannel, modeChangeRequests);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error creating connection from SocketChannel", e);
            return;
        }

        try {
            newConnections.put(connection);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Interrupted while putting new Connection");
        }
    }

    private void read(SelectionKey key) {
        NioSocketConnection connection = (NioSocketConnection) key.attachment();

        int read;
        try {
            read = connection.readFromChannel();
        } catch (IOException e) {
            read = -1;
        }

        if (read == -1) {
            key.cancel();
            close(connection);
        }

        try {
            connectionEvents.put(new ConnectionEvent(ConnectionEvent.What.DATA, connection));
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Interrupted while putting DATA ConnectionEvent");
        }
    }

    private void write(SelectionKey key) {
        NioSocketConnection connection = (NioSocketConnection) key.attachment();
        try {
            connection.writeToChannel();
        } catch (IOException e) {
            close(connection);
        }

        if (connection.shouldClose() && connection.nothingToWrite()) {
            close(connection);
        }
    }

    private void close(NioSocketConnection connection) {
        try {
            connection.channel.close();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error closing connection", e);
        }

        try {
            connectionEvents.put(new ConnectionEvent(ConnectionEvent.What.CLOSE, connection));
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Interrupted while putting CLOSE ConnectionEvent");
        }
    }
}
