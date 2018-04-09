package nmayorov.server;

import nmayorov.connection.Connection;
import nmayorov.connection.ConnectionEvent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;

public abstract class Server {
    protected final InetSocketAddress address;
    protected ArrayBlockingQueue<Connection> newConnections = null;
    protected ArrayBlockingQueue<ConnectionEvent> connectionEvents = null;

    public Server(InetSocketAddress address) {
        this.address = address;

    }

    public void bindConnectionQueues(ArrayBlockingQueue<Connection> newConnectionQueue,
                              ArrayBlockingQueue<ConnectionEvent> connectionEventsQueue) {
        this.newConnections = newConnectionQueue;
        this.connectionEvents = connectionEventsQueue;
    }

    public abstract void start() throws IOException;
}
