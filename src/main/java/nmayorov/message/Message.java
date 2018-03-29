package nmayorov.message;

import nmayorov.client.Client;
import nmayorov.Connection;
import nmayorov.Server;

import java.nio.charset.Charset;
import java.util.ArrayList;

abstract public class Message {
    static final byte FIELD_DELIMITER = 0x1F;
    static final byte MESSAGE_END = 0x1E;
    static final Charset CHARSET = Charset.forName("UTF-8");

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
