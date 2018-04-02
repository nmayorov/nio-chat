package nmayorov.command;

import nmayorov.Connection;
import nmayorov.Server;
import nmayorov.message.ServerText;


public class Help extends Command {
    static final String PATTERN = "\\s*\\\\help\\s*$";
    static final String DESCRIPTION = "\\help --- show help";

    @Override
    public void execute(Server server, Connection connection) {
        String[] lines = {
            "Available commands:",
            Help.DESCRIPTION,
            Name.DESCRIPTION,
            List.DESCRIPTION,
            Exit.DESCRIPTION
        };
        String message = String.join("\n", lines);
        connection.send(new ServerText(message));
    }
}
