package nmayorov.command;

import nmayorov.Connection;
import nmayorov.Server;
import nmayorov.message.ServerText;

public class UnknownCommand extends Command {
    @Override
    public void execute(Server server, Connection connection) {
        connection.send(new ServerText("Unknown command."));
    }
}
