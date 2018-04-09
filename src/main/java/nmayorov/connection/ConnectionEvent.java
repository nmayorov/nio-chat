package nmayorov.connection;

abstract public class ConnectionEvent {
    public enum What {DATA, CLOSE}

    public final What what;
    public final Connection connection;

    public ConnectionEvent(What what, Connection connection) {
        this.what = what;
        this.connection = connection;
    }
}
