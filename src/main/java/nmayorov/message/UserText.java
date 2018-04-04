package nmayorov.message;

public class UserText extends Message {
    public UserText(String name, String text) {
        super(Type.USER_TEXT);
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
