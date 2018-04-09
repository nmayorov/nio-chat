package nmayorov.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.logging.Logger;

public class NioServer extends Server {
    private static final Logger LOGGER = Logger.getLogger(NioServer.class.getName());

    public NioServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void start() throws IOException {
        LOGGER.info("Starting server at " + address);

        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(address);
        Thread selectionThread = new Thread(new SelectionLoop(serverChannel, newConnections, connectionEvents),
                                            "SelectionLoop");
        selectionThread.start();
    }
}
