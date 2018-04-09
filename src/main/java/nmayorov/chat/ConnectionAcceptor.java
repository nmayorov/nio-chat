package nmayorov.chat;

import nmayorov.connection.Connection;
import nmayorov.message.ChatMessageBuffer;
import nmayorov.message.NameRequest;
import nmayorov.message.ServerText;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

class ConnectionAcceptor implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(ConnectionAcceptor.class.getName());

    private final ArrayBlockingQueue<Connection> newConnections;
    ConnectionAcceptor(ArrayBlockingQueue<Connection> newConnections) {
        this.newConnections = newConnections;
    }

    @Override
    public void run() {
        Connection connection;
        while (true) {
            try {
                connection = newConnections.take();
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, "Interrupting while taking connection from queue", e);
                continue;
            }
            connection.messageBuffer = new ChatMessageBuffer();
            connection.write(new ServerText("Enter your name.").getBytes());
            connection.write(new NameRequest().getBytes());
        }
    }
}
