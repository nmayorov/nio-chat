package nmayorov.connection;

public class ModeChangeRequest {
    public NioSocketConnection connection;
    public int ops;

    ModeChangeRequest(NioSocketConnection connection, int ops) {
        this.connection = connection;
        this.ops = ops;
    }
}
