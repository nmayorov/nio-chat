package nmayorov.server;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.logging.Logger;

public class ConnectionAcceptor implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(ConnectionProcessor.class.getName());

    private ServerSocketChannel serverChannel;
    private Selector[] selectors;
    private Queue<SocketChannel> inboundSockets;

    ConnectionAcceptor(ServerSocketChannel serverChannel, Selector[] selectors, Queue<SocketChannel> inboundSockets) {
        this.serverChannel = serverChannel;
        this.selectors = selectors;
        this.inboundSockets = inboundSockets;
    }

    @Override
    public void run() {
        while (serverChannel.isOpen()) {
            try {
                SocketChannel channel = serverChannel.accept();
                inboundSockets.add(channel);
                for (Selector selector : selectors) {
                    selector.wakeup();
                }
                LOGGER.info("Connection from " + channel.getRemoteAddress().toString());
            } catch (IOException e) {
                LOGGER.warning("Error accepting connection");
            }
        }
    }
}
