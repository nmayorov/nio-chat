package nmayorov.command;

import nmayorov.Connection;
import nmayorov.Server;
import nmayorov.message.Disconnect;

public class Exit extends Command {
    static final String PATTERN = "\\s*\\\\exit\\s*$";
    static final String DESCRIPTION = "\\exit --- exit the chat";

    @Override
    public void execute(Server server, Connection connection) {
        connection.send(new Disconnect());
        server.removeConnection(connection);
    }
}
