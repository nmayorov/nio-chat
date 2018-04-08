package nmayorov.message;

import nmayorov.connection.Connection;

public interface MessageHandler {
    void execute(Message message, Connection connection);
}
