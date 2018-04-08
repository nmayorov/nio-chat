package nmayorov.command;

import nmayorov.connection.Connection;

public interface CommandHandler {
    void execute(Command command, Connection connection);
}
