package nmayorov.message;

import nmayorov.client.Client;
import nmayorov.Connection;
import nmayorov.Server;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

abstract public class Message {
    private static final byte FIELD_DELIMITER = 0x1F;
    private static final byte MESSAGE_END = 0x1E;
    private static final Charset CHARSET = Charset.forName("UTF-8");

    public static class InvalidMessageException extends RuntimeException {};

    private static final HashMap<String, Integer> FIELD_COUNT;
    static {
        FIELD_COUNT = new HashMap<>();
        FIELD_COUNT.put(UserText.MESSAGE_NAME, UserText.FIELD_COUNT);
        FIELD_COUNT.put(ServerText.MESSAGE_NAME, ServerText.FIELD_COUNT);
        FIELD_COUNT.put(NameAccepted.MESSAGE_NAME, NameAccepted.FIELD_COUNT);
        FIELD_COUNT.put(NameRequest.MESSAGE_NAME, NameRequest.FIELD_COUNT);
        FIELD_COUNT.put(NameSent.MESSAGE_NAME, NameSent.FIELD_COUNT);
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
        if (!FIELD_COUNT.containsKey(name)) {
            throw new InvalidMessageException();
        }

        Integer fieldCount = FIELD_COUNT.get(name);
        if (fieldCount != null && fieldCount + 1 != items.length) {
            throw new InvalidMessageException();
        }

        switch (name) {
            case UserText.MESSAGE_NAME:
                return new UserText(items[1], items[2]);
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

    protected ArrayList<String> fields;

    Message(String messageName) {
        fields = new ArrayList<>();
        fields.add(messageName);
    };

    public byte[] getBytes() {
        String separator = Character.toString((char) FIELD_DELIMITER);
        String end = Character.toString((char) MESSAGE_END);
        String string = String.join(separator, fields) + end;
        return string.getBytes(CHARSET);
    }

    public String getText() {
        return null;
    }
    public void handleServerReceive(Server server, Connection connection) {}
    public void handleClientReceive(Client client, Connection connection) {}

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
