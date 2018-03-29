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
        server.renameConnection(name, connection);
    }
}
