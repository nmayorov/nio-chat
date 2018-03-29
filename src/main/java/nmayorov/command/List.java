package nmayorov.command;

import nmayorov.Connection;
import nmayorov.Server;
import nmayorov.message.ServerText;

import java.util.ArrayList;
import java.util.Iterator;

public class List extends Command {
    static final String PATTERN = "\\s*\\\\list\\s*$";
    static final String DESCRIPTION = "\\list --- list connected users";

    @Override
    public void execute(Server server, Connection connection) {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("Connected users:");
        Iterator<String> user = server.listUsers();
        while (user.hasNext()) {
            lines.add(user.next());
        }
        String message = String.join("\n", lines);
        connection.send(new ServerText(message));
    }
}
