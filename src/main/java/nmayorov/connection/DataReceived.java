package nmayorov.connection;

public class DataReceived extends ConnectionEvent {
    public final byte[] data;
    public DataReceived(Connection connection) {
        super(What.DATA, connection);
        data = connection.getData();
    }
}
