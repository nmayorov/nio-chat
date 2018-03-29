package nmayorov.message;

public class ServerText extends Message {
    static final String MESSAGE_NAME = "SERVER_TEXT";
    static final Integer FIELD_COUNT = 1;

    public ServerText(String text) {
        super(MESSAGE_NAME);
        fields.add(text);
    }

    @Override
    public String getText() {
        return "[Server] "+ fields.get(1);
    }
}
