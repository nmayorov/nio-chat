package nmayorov.message;


public class Disconnect extends Message {
    static final String MESSAGE_NAME = "DISCONNECT";
    static final Integer FIELD_COUNT = 0;

    public Disconnect() {
        super(MESSAGE_NAME);
    }
}
