package nmayorov.server;

public interface ServerLogic {
    void onConnectionAccept(Connection connection);
    void onConnectionClose(Connection connection);
    void onDataReceive(Connection connection);
}
