package nmayorov.command;

import nmayorov.Connection;
import nmayorov.Server;
import nmayorov.message.ServerText;

import java.util.ArrayList;
import java.util.Iterator;

public class    List extends Command {
    static final String PATTERN = "\\s*\\\\list\\s*$";
    static final String DESCRIPTION = "\\list --- list connected users";

    @Override
    public void execute(Server server, Connection connection) {
        StringBuilder sb = new StringBuilder("Connected users:");
        Iterator<String> user = server.listUsers();
        while (user.hasNext()) {
            sb.append('\n');
            sb.append(user.next());
        }
        connection.send(new ServerText(sb.toString()));
    }
}
