package nmayorov.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

public class Server {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    private static final int INBOUND_QUEUE_CAPACITY = 128;

    private ServerLogic serverLogic;
    private InetSocketAddress address;

    public Server(ServerLogic serverLogic, InetSocketAddress address) {
        this.serverLogic = serverLogic;
        this.address = address;
    }

    public void start() throws IOException{
        start(Runtime.getRuntime().availableProcessors());
    }

    public void start(int numThreads) throws IOException {
        LOGGER.info(String.format("Starting server at %s using %d threads", address, numThreads));

        ArrayBlockingQueue<SocketChannel> inboundConnections = new ArrayBlockingQueue<>(INBOUND_QUEUE_CAPACITY);

        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(address);

        Selector[] selectors = new Selector[numThreads];
        for (int i = 0; i < numThreads; ++i) {
            selectors[i] = Selector.open();
        }

        Thread acceptingThread = new Thread(new ConnectionAcceptor(serverChannel, selectors, inboundConnections));
        acceptingThread.start();

        for (int i = 0; i < numThreads; ++i) {
            Thread processingThread = new Thread(
                    new ConnectionProcessor(serverLogic, selectors[i], inboundConnections));
            processingThread.start();
        }
    }
}
