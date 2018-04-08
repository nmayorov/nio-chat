package nmayorov.server;

import nmayorov.connection.NioSocketConnection;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

class ConnectionProcessor implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(ConnectionProcessor.class.getName());

    private final ServerLogic serverLogic;
    private final Selector selector;
    private final ArrayBlockingQueue<SocketChannel> inboundConnections;

    ConnectionProcessor(ServerLogic serverLogic, Selector selector,
                        ArrayBlockingQueue<SocketChannel> inboundConnections) {
        this.serverLogic = serverLogic;
        this.selector = selector;
        this.inboundConnections = inboundConnections;
    }

    private void processInboundConnections() {
        SocketChannel channel = inboundConnections.poll();
        while (channel != null) {
            NioSocketConnection connection = new NioSocketConnection(channel);
            try {
                connection.channel.configureBlocking(false);
                connection.channel.register(selector,
                                            SelectionKey.OP_READ | SelectionKey.OP_WRITE, connection);
                serverLogic.onConnectionAccept(connection);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error registering connection", e);
            }
            channel = inboundConnections.poll();
        }
    }

    @Override
    public void run() {
        while (true) {
            processInboundConnections();

            int selected;
            try {
                selected = selector.select();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,
                           "Error selecting IO channels. The server functionality might be broken", e);
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
    }

    private void closeConnection(NioSocketConnection connection) {
        try {
            connection.channel.close();
            serverLogic.onConnectionClose(connection);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING,"Error closing connection", e);
        }
    }

    private void handleWrite(SelectionKey key) {
        NioSocketConnection connection = (NioSocketConnection) key.attachment();
        try {
            connection.writeToChannel();
        } catch (IOException e) {
            closeConnection(connection);
        }

        if (connection.shouldClose() && connection.nothingToWrite()) {
            closeConnection(connection);
        }
    }

    private void handleRead(SelectionKey key) {
        NioSocketConnection connection = (NioSocketConnection) key.attachment();
        int read;
        try {
            read = connection.readFromChannel();
        } catch (IOException e) {
            read = -1;
        }

        if (read == -1) {
            key.cancel();
            closeConnection(connection);
            return;
        }

        serverLogic.onDataReceive(connection);
        if (connection.shouldClose() && connection.nothingToWrite()) {
            closeConnection(connection);
        }
    }
}
