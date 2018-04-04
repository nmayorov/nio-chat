package nmayorov.message;

import java.util.HashMap;

public class MessageHandlerFactory {
    private HashMap<Message.Type, MessageHandler> handlers = new HashMap<>();

    public void register(Message.Type type, MessageHandler handler) {
        handlers.put(type, handler);
    }

    public MessageHandler get(Message.Type type) {
        return handlers.get(type);
    }
}
