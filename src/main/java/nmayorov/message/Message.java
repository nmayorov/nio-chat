package nmayorov.message;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

abstract public class Message {
    private static final byte FIELD_DELIMITER = 0x1F;
    private static final byte MESSAGE_END = 0x1E;
    private static final Charset CHARSET = Charset.forName("UTF-8");

    public static class InvalidMessageException extends RuntimeException {};

    public enum Type {DISCONNECT, NAME_ACCEPTED, NAME_REQUEST, NAME_SENT, SERVER_TEXT, USER_TEXT};

    private static final HashMap<Type, Integer> FIELD_COUNT;
    static {
        FIELD_COUNT = new HashMap<>();
        FIELD_COUNT.put(Type.DISCONNECT, 0);
        FIELD_COUNT.put(Type.NAME_ACCEPTED, 1);
        FIELD_COUNT.put(Type.NAME_REQUEST, 0);
        FIELD_COUNT.put(Type.NAME_SENT, 1);
        FIELD_COUNT.put(Type.SERVER_TEXT, 1);
        FIELD_COUNT.put(Type.USER_TEXT, 2);
    }

    public static Message getNext(ByteBuffer buffer) throws InvalidMessageException {
        byte[] bytes = ByteBufferCutter.cut(buffer, MESSAGE_END);
        if (bytes == null) {
            return null;
        }

        if (bytes.length == 0 || bytes[bytes.length - 1] != MESSAGE_END) {
            throw new InvalidMessageException();
        }

        String string = new String(bytes, 0, bytes.length - 1, CHARSET);

        String delimiter = Character.toString((char) Message.FIELD_DELIMITER);
        String[] items = string.split(delimiter, -1);

        if (items.length == 0) {
            throw new InvalidMessageException();
        }

        String name = items[0];
        Type type;
        try {
            type = Type.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new InvalidMessageException();
        }

        Integer fieldCount = FIELD_COUNT.get(type);
        if (fieldCount + 1 != items.length) {
            throw new InvalidMessageException();
        }

        switch (type) {
            case USER_TEXT:
                return new UserText(items[1], items[2]);
            case SERVER_TEXT:
                return new ServerText(items[1]);
            case NAME_ACCEPTED:
                return new NameAccepted(items[1]);
            case NAME_REQUEST:
                return new NameRequest();
            case NAME_SENT:
                return new NameSent(items[1]);
            case DISCONNECT:
                return new Disconnect();
            default:
                assert false : "You forgot to add some case!";
        }

        return null;
    }

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
}
