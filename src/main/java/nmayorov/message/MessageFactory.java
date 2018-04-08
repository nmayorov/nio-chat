package nmayorov.message;

import java.util.HashMap;

public class MessageFactory {
    public static class InvalidMessageException extends RuntimeException {}

    private static final HashMap<Message.Type, Integer> FIELD_COUNT;
    static {
        FIELD_COUNT = new HashMap<>();
        FIELD_COUNT.put(Message.Type.DISCONNECT, 0);
        FIELD_COUNT.put(Message.Type.NAME_ACCEPTED, 1);
        FIELD_COUNT.put(Message.Type.NAME_REQUEST, 0);
        FIELD_COUNT.put(Message.Type.NAME_SENT, 1);
        FIELD_COUNT.put(Message.Type.SERVER_TEXT, 1);
        FIELD_COUNT.put(Message.Type.USER_TEXT, 2);
    }

    public static Message createFromBytes(byte[] bytes) throws InvalidMessageException {
        if (bytes == null) {
            return null;
        }

        if (bytes.length == 0 || bytes[bytes.length - 1] != Message.MESSAGE_END) {
            throw new InvalidMessageException();
        }

        String string = new String(bytes, 0, bytes.length - 1, Message.CHARSET);

        String delimiter = Character.toString((char) Message.FIELD_DELIMITER);
        String[] items = string.split(delimiter, -1);

        if (items.length == 0) {
            throw new InvalidMessageException();
        }

        String name = items[0];
        Message.Type type;
        try {
            type = Message.Type.valueOf(name);
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
}
