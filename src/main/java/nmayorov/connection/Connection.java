package nmayorov.connection;

public abstract class Connection {
    public volatile String name = null;
    public MessageBuffer messageBuffer = null;

    private volatile boolean shouldClose = false;

    public abstract void write(byte[] data);
    public abstract byte[] getData();

    public void requestClose() {
        shouldClose = true;
    }
    public boolean shouldClose() {
        return shouldClose;
    }
}
