package nmayorov.message;

public class UserText extends Message {
    static final String MESSAGE_NAME = "USER_TEXT";
    static final Integer FIELD_COUNT = 2;

    public UserText(String name, String text) {
        super(MESSAGE_NAME);
        fields.add(name);
        fields.add(text);
    }

    public String getMessage() {
        return fields.get(2);
    }

    @Override
    public String getText() {
        return "[" + fields.get(1) + "] " + fields.get(2);
    }
}
