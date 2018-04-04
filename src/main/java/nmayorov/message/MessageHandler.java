package nmayorov.message;

import nmayorov.server.Connection;

public interface MessageHandler {
    void execute(Message message, Connection connection);
}
