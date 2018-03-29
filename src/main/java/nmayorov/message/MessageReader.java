package nmayorov.message;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class MessageReader {
    static class InvalidMessageException extends RuntimeException {};

    private static final HashMap<String, Integer> FIELD_COUNT;
    static {
        FIELD_COUNT = new HashMap<>();
        FIELD_COUNT.put(ClientText.MESSAGE_NAME, ClientText.FIELD_COUNT);
        FIELD_COUNT.put(ServerText.MESSAGE_NAME, ServerText.FIELD_COUNT);
        FIELD_COUNT.put(NameAccepted.MESSAGE_NAME, NameAccepted.FIELD_COUNT);
        FIELD_COUNT.put(NameRequest.MESSAGE_NAME, NameRequest.FIELD_COUNT);
        FIELD_COUNT.put(NameSent.MESSAGE_NAME, NameSent.FIELD_COUNT);
    }

    public static Message next(ByteBuffer buffer) throws InvalidMessageException {
        byte[] bytes = ByteBufferCutter.cut(buffer, Message.MESSAGE_END);
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
        if (!FIELD_COUNT.containsKey(name)) {
            throw new InvalidMessageException();
        }

        Integer fieldCount = FIELD_COUNT.get(name);
        if (fieldCount != null && fieldCount + 1 != items.length) {
            throw new MessageReader.InvalidMessageException();
        }

        switch (name) {
            case ClientText.MESSAGE_NAME:
                return new ClientText(items[1], items[2]);
            case ServerText.MESSAGE_NAME:
                return new ServerText(items[1]);
            case NameAccepted.MESSAGE_NAME:
                return new NameAccepted(items[1]);
            case NameRequest.MESSAGE_NAME:
                return new NameRequest();
            case NameSent.MESSAGE_NAME:
                return new NameSent(items[1]);
            default:
                assert false : "You forgot to add some case!";
        }

        return null;
    }
}
