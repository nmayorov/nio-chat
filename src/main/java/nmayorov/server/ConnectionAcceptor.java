package nmayorov.server;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

class ConnectionAcceptor implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(ConnectionProcessor.class.getName());

    private final ServerSocketChannel serverChannel;
    private final Selector[] selectors;
    private final ArrayBlockingQueue<SocketChannel> inboundSockets;

    ConnectionAcceptor(ServerSocketChannel serverChannel, Selector[] selectors,
                       ArrayBlockingQueue<SocketChannel> inboundSockets) {
        this.serverChannel = serverChannel;
        this.selectors = selectors;
        this.inboundSockets = inboundSockets;
    }

    @Override
    public void run() {
        while (serverChannel.isOpen()) {
            try {
                SocketChannel channel = serverChannel.accept();
                LOGGER.info("Connection from " + channel.getRemoteAddress().toString());
                try {
                    inboundSockets.put(channel);
                } catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING,
                               "Interrupted while trying to put inbound connection. Connection refused", e);
                    channel.close();
                    continue;
                }
                for (Selector selector : selectors) {
                    selector.wakeup();
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error accepting connection", e);
            }
        }
    }
}
