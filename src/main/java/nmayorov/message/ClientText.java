package nmayorov.message;


import nmayorov.Connection;
import nmayorov.Server;
import nmayorov.command.Command;

public class ClientText extends Message {
    static final String MESSAGE_NAME = "CLIENT_TEXT";
    static final Integer FIELD_COUNT = 2;

    public ClientText(String name, String text) {
        super(MESSAGE_NAME);
        fields.add(name);
        fields.add(text);
    }

    @Override
    public String getText() {
        return "[" + fields.get(1) + "] " + fields.get(2);
    }

    @Override
    public void handleServerReceive(Server server, Connection connection) {
        Command command = Command.fromString(fields.get(2));
        if (command == null) {
            server.addMessageToHistory(getBytes());
            server.broadcast(this);
        } else {
            command.execute(server, connection);
        }
    }
}
