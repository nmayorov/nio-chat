package nmayorov.message;

import java.nio.charset.Charset;
import java.util.ArrayList;

abstract public class Message {
    public static final byte FIELD_DELIMITER = 0x1F;
    public static final byte MESSAGE_END = 0x1E;
    public static final Charset CHARSET = Charset.forName("UTF-8");

    public enum Type {DISCONNECT, NAME_ACCEPTED, NAME_REQUEST, NAME_SENT, SERVER_TEXT, USER_TEXT}

    private Type type;
    protected ArrayList<String> fields;

    Message(Type type) {
        this.type = type;
        fields = new ArrayList<>();
        fields.add(type.toString());
    }

    public Type getType() {
        return type;
    }

    public byte[] getBytes() {
        String separator = Character.toString((char) FIELD_DELIMITER);
        String end = Character.toString((char) MESSAGE_END);
        String string = String.join(separator, fields) + end;
        return string.getBytes(CHARSET);
    }

    public String getText() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Message objMessage = (Message) obj;
        return fields.equals(objMessage.fields);
    }

    @Override
    public int hashCode() {
        return fields.hashCode();
    }
}
