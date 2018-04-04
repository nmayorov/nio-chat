package nmayorov.server;

import nmayorov.message.MessageHandlerFactory;

public interface ServerLogic {
    void onConnectionAccept(Connection connection);
    void onConnectionClose(Connection connection);
    MessageHandlerFactory registerMessageHandlers();
}
