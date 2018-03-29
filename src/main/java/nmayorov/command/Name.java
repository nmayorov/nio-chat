package nmayorov.command;

import nmayorov.Connection;
import nmayorov.Server;
import nmayorov.message.NameAccepted;
import nmayorov.message.ServerText;

public class Name extends Command {
    static final String PATTERN = "\\s*\\\\name\\s*(.*)$";
    static final String DESCRIPTION = "\\name name --- change the name to a new one";

    private String name;
    Name(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void execute(Server server, Connection connection) {
        if (name.isEmpty()) {
            connection.send(new ServerText("The name must be non-empty."));
        } else if (server.hasUser(name)) {
            connection.send(new ServerText("The name is already occupied."));
        } else {
            connection.send(new NameAccepted(name));
            String oldName = connection.name;
            server.renameConnection(connection, name);
            server.broadcast(new ServerText(String.format("%s is now %s.", oldName, name)));
        }
    }
}
