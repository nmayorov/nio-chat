package nmayorov.message;

public class ServerText extends Message {
    public ServerText(String text) {
        super(Type.SERVER_TEXT);
        fields.add(text);
    }

    @Override
    public String getText() {
        return "[Server] "+ fields.get(1);
    }
}
