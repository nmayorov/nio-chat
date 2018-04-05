package nmayorov.server;

import nmayorov.message.Message;
import nmayorov.message.MessageHandler;
import nmayorov.message.MessageHandlerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

class ConnectionProcessor implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(ConnectionProcessor.class.getName());

    private ServerLogic serverLogic;
    private MessageHandlerFactory handlerFactory;
    private Selector selector;
    private ArrayBlockingQueue<SocketChannel> inboundConnections;

    ConnectionProcessor(ServerLogic serverLogic, MessageHandlerFactory messageHandlerFactory,
                        Selector selector, ArrayBlockingQueue<SocketChannel> inboundConnections) {
        this.serverLogic = serverLogic;
        this.handlerFactory = messageHandlerFactory;
        this.selector = selector;
        this.inboundConnections = inboundConnections;
    }

    private void processInboundConnections() {
        SocketChannel channel = inboundConnections.poll();
        while (channel != null) {
            Connection connection = new Connection(channel);
            try {
                connection.channel.configureBlocking(false);
                connection.channel.register(selector,
                                            SelectionKey.OP_READ | SelectionKey.OP_WRITE, connection);
                serverLogic.onConnectionAccept(connection);
            } catch (IOException e) {
                LOGGER.warning("Error registering connection");
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
                LOGGER.severe("Error selecting IO channels. The server functionality might be broken.");
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

    private void closeConnection(Connection connection) {
        try {
            connection.channel.close();
            serverLogic.onConnectionClose(connection);
        } catch (IOException e) {
            LOGGER.warning("Error closing connection.");
        }
    }

    private void handleWrite(SelectionKey key) {
        Connection connection = (Connection) key.attachment();
        try {
            connection.write();
        } catch (IOException e) {
            closeConnection(connection);
        }

        if (connection.shouldClose() && connection.nothingToWrite()) {
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
            MessageHandler messageHandler = handlerFactory.get(message.getType());
            if (messageHandler != null) {
                messageHandler.execute(message, connection);
            }
            message = Message.getNext(connection.getReadBuffer());
        }

        if (connection.shouldClose() && connection.nothingToWrite()) {
            closeConnection(connection);
        }
    }
}
