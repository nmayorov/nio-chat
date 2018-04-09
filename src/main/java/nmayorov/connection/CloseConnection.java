package nmayorov.connection;

public class CloseConnection extends ConnectionEvent {
    public CloseConnection(Connection connection) {
        super(What.CLOSE, connection);
    }
}
