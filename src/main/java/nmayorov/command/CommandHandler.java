package nmayorov.command;

import nmayorov.server.Connection;

public interface CommandHandler {
    void execute(Command command, Connection connection);
}
