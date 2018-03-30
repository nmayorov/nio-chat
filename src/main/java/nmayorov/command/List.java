package nmayorov.command;

import java.util.Set;

import nmayorov.Connection;
import nmayorov.Server;
import nmayorov.message.ServerText;

public class List extends Command {
    static final String PATTERN = "\\s*\\\\list\\s*$";
    static final String DESCRIPTION = "\\list --- list connected users";

    @Override
    public void execute(Server server, Connection connection) {
        Set<String> users = server.getConnectedUsers();
        StringBuilder sb = new StringBuilder(String.format("Currently %d users connected:", users.size()));
        for (String user : users) {
            sb.append('\n');
            sb.append(user);
        }
        connection.send(new ServerText(sb.toString()));
    }
}
