package nmayorov.message;

import nmayorov.Connection;
import nmayorov.Server;

import java.util.Iterator;

public class NameSent extends Message {
    static final String MESSAGE_NAME = "NAME_SENT";
    static final Integer FIELD_COUNT = 1;

    public NameSent(String name) {
        super(MESSAGE_NAME);
        this.fields.add(name);
    }

    @Override
    public void handleServerReceive(Server server, Connection connection) {
        String name = fields.get(1);
        if (name.isEmpty()) {
            connection.send(new ServerText("The name must be non-empty."));
            connection.send(new NameRequest());
        } else if (server.hasUser(name)) {
            connection.send(new ServerText("The name is already occupied."));
            connection.send(new NameRequest());
        } else {
            connection.send(new NameAccepted(name));
            connection.name = name;
            connection.send(new ServerText("Welcome to the chat! Type \\help for help."));
            server.addConnection(connection);
            server.broadcast(new ServerText(name + " joined the chat!"));
            Iterator<byte[]> messages = server.getMessageHistory();
            while (messages.hasNext()) {
                connection.send(messages.next());
            }
        }
    }
}
