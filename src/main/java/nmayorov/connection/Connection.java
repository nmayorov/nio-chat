package nmayorov.connection;

public abstract class Connection {
    private static int idCounter = 0;
    public final int id;

    Connection() {
        id = idCounter;
        ++idCounter;
    }

    public String name = null;
    public MessageBuffer messageBuffer = null;

    private volatile boolean shouldClose = false;

    public abstract void write(byte[] data);
    public abstract byte[] read();

    public void requestClose() {
        shouldClose = true;
    }
    public boolean shouldClose() {
        return shouldClose;
    }
}
