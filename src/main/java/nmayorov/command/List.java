package nmayorov.command;

import nmayorov.Connection;
import nmayorov.Server;
import nmayorov.message.ServerText;

public class List extends Command {
    static final String PATTERN = "\\s*\\\\list\\s*$";
    static final String DESCRIPTION = "\\list --- list connected users";

    @Override
    public void execute(Server server, Connection connection) {
        StringBuilder sb = new StringBuilder("Connected users:");
        for (String user : server.getConnectedUsers()) {
            sb.append('\n');
            sb.append(user);
        }
        connection.send(new ServerText(sb.toString()));
    }
}
