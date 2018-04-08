package nmayorov.server;

import nmayorov.command.Command;
import nmayorov.command.CommandHandler;
import nmayorov.command.CommandHandlerFactory;
import nmayorov.command.Exit;
import nmayorov.command.Help;
import nmayorov.command.List;
import nmayorov.command.Name;
import nmayorov.connection.Connection;
import nmayorov.message.ChatMessageBuffer;
import nmayorov.message.Disconnect;
import nmayorov.message.Message;
import nmayorov.message.MessageFactory;
import nmayorov.message.MessageHandler;
import nmayorov.message.MessageHandlerFactory;
import nmayorov.message.NameAccepted;
import nmayorov.message.NameRequest;
import nmayorov.message.NameSent;
import nmayorov.message.ServerText;
import nmayorov.message.UserText;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class ChatLogic implements ServerLogic {
    private static final Logger LOGGER = Logger.getLogger(ChatLogic.class.getName());
    private static final int HISTORY_SIZE = 100;

    private final ConcurrentHashMap<String, Connection> connections;
    private final CircularFifoQueue<byte[]> messageHistory;
    private final CommandHandlerFactory commands;
    private final MessageHandlerFactory messageHandlers;

    public ChatLogic() {
        connections = new ConcurrentHashMap<>();
        messageHistory = new CircularFifoQueue<>(HISTORY_SIZE);
        commands = registerCommands();
        messageHandlers = registerMessages();
    }

    private CommandHandlerFactory registerCommands() {
        CommandHandlerFactory commands = new CommandHandlerFactory();

        commands.register(Command.Type.EXIT, (command, connection) -> {
            connection.write(new Disconnect().getBytes());
            connection.requestClose();
        });

        commands.register(Command.Type.HELP, (command, connection) -> {
            String[] lines = {
                "Available commands:",
                Help.DESCRIPTION,
                Name.DESCRIPTION,
                List.DESCRIPTION,
                Exit.DESCRIPTION
            };
            String message = String.join("\n", lines);
            connection.write(new ServerText(message).getBytes());
        });

        commands.register(Command.Type.LIST, (command, connection) -> {
            StringBuilder sb;
            sb = new StringBuilder(String.format("Currently %d users connected:", connections.size()));
            for (String user : connections.keySet()) {
                sb.append('\n');
                sb.append(user);
            }
            connection.write(new ServerText(sb.toString()).getBytes());
        });

        commands.register(
            Command.Type.NAME,
            (command, connection) -> renameConnection(((Name) command).getName(), connection)
        );

        commands.register(
            Command.Type.UNKNOWN_COMMAND,
            (command, connection) -> connection.write(new ServerText("Unknown command.").getBytes())
        );

        return commands;
    }

    private MessageHandlerFactory registerMessages() {
        MessageHandlerFactory handlers = new MessageHandlerFactory();

        handlers.register(Message.Type.USER_TEXT, (message, connection) -> {
            Command command = Command.fromString(((UserText) message).getMessage());
            if (command != null) {
                CommandHandler handler = commands.get(command.getType());
                if (handler != null) {
                    handler.execute(command, connection);
                }
                return;
            }

            synchronized (messageHistory) {
                messageHistory.add(message.getBytes());
            }
            broadcast(message);
        });

        handlers.register(Message.Type.NAME_SENT, (message, connection) -> {
            String name = ((NameSent) message).getName();
            registerNewConnection(name, connection);
        });

        return handlers;
    }

    @Override
    public void onConnectionAccept(Connection connection) {
        connection.messageBuffer = new ChatMessageBuffer();
        connection.write(new ServerText("Enter your name.").getBytes());
        connection.write(new NameRequest().getBytes());
    }

    @Override
    public void onConnectionClose(Connection connection) {
        if (connections.containsKey(connection.name)) {
            connections.remove(connection.name);
            broadcast(new ServerText(connection.name + " left the chat."));
            LOGGER.info(String.format("User %s disconnected, %d left", connection.name, connections.size()));
        }
    }

    @Override
    public void onDataReceive(Connection connection) {
        connection.messageBuffer.put(connection.read());
        byte[] messageBytes = connection.messageBuffer.getNextMessage();
        while (messageBytes != null) {
            Message message = MessageFactory.createFromBytes(messageBytes);
            MessageHandler messageHandler = messageHandlers.get(message.getType());
            if (messageHandler != null) {
                messageHandler.execute(message, connection);
            }
            messageBytes = connection.messageBuffer.getNextMessage();
        }
    }

    private void broadcast(Message message) {
        byte[] bytes = message.getBytes();
        for (Connection connection : connections.values()) {
            connection.write(bytes);
        }
    }

    private void registerNewConnection(String name, Connection connection) {
        if (name.isEmpty()) {
            connection.write(new ServerText("The name must be non-empty.").getBytes());
            connection.write(new NameRequest().getBytes());
        } else if (connections.containsKey(name)) {
            connection.write(new ServerText("The name is already occupied.").getBytes());
            connection.write(new NameRequest().getBytes());
        } else {
            connection.write(new NameAccepted(name).getBytes());
            connection.name = name;
            connection.write(new ServerText("Welcome to the chat! Type \\help for help.").getBytes());
            connections.put(name, connection);
            broadcast(new ServerText(name + " joined the chat!"));
            synchronized (messageHistory) {
                for (byte[] message : messageHistory) {
                    connection.write(message);
                }
            }
            LOGGER.info(String.format("User %d is registered as %s", connections.size(), name));
        }
    }

    private void renameConnection(String newName, Connection connection) {
        if (newName.isEmpty()) {
            connection.write(new ServerText("The name must be non-empty.").getBytes());
        } else if (connections.containsKey(newName)) {
            connection.write(new ServerText("The name is already occupied.").getBytes());
        } else {
            connection.write(new NameAccepted(newName).getBytes());
            String oldName = connection.name;
            connections.remove(connection.name);
            connection.name = newName;
            connections.put(connection.name, connection);
            broadcast(new ServerText(String.format("%s is now %s.", oldName, newName)));
            LOGGER.info(String.format("User %s is renamed to %s", oldName, newName));
        }
    }
}
