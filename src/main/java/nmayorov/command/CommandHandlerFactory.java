package nmayorov.command;

import java.util.HashMap;

public class CommandHandlerFactory {
    private final HashMap<Command.Type, CommandHandler> handlers = new HashMap<>();

    public void register(Command.Type type, CommandHandler handler) {
        handlers.put(type, handler);
    }

    public CommandHandler get(Command.Type type) {
        return handlers.get(type);
    }
}
