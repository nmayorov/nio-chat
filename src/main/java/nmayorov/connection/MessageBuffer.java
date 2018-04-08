package nmayorov.connection;

public interface MessageBuffer {
    void put(byte[] data);
    byte[] getNextMessage();
}
