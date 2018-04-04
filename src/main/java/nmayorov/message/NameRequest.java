package nmayorov.message;


public class NameRequest extends Message {
    static final String MESSAGE_NAME = "NAME_REQUEST";
    static final Integer FIELD_COUNT = 0;

    public NameRequest() {
        super(MESSAGE_NAME);
    }
}
