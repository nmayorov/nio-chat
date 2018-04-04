package nmayorov.server;

import nmayorov.message.Message;

public interface ServerLogic {
    void onConnectionAccept(Connection connection);
    void onConnectionClose(Connection connection);
    void onMessageReceive(Connection connection, Message message);
}
