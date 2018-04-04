package nmayorov.server;

import nmayorov.command.Command;
import nmayorov.command.Exit;
import nmayorov.command.Help;
import nmayorov.command.List;
import nmayorov.command.Name;
import nmayorov.message.Disconnect;
import nmayorov.message.Message;
import nmayorov.message.NameAccepted;
import nmayorov.message.NameRequest;
import nmayorov.message.NameSent;
import nmayorov.message.ServerText;
import nmayorov.message.UserText;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.HashMap;
import java.util.logging.Logger;

public class ChatLogic implements ServerLogic {
    private static final Logger LOGGER = Logger.getLogger(ChatLogic.class.getName());
    private static final int HISTORY_SIZE = 100;

    private final HashMap<String, Connection> connections;
    private final CircularFifoQueue<byte[]> messageHistory;

    public ChatLogic() {
        connections = new HashMap<>();
        messageHistory = new CircularFifoQueue<>(HISTORY_SIZE);
    }

    private void broadcast(Message message) {
        byte[] bytes = message.getBytes();
        synchronized (connections) {
            for (Connection connection : connections.values()) {
                connection.send(bytes);
            }
        }
    }

    private void registerNewConnection(String name, Connection connection) {
        if (name.isEmpty()) {
            connection.send(new ServerText("The name must be non-empty."));
            connection.send(new NameRequest());
        } else if (connections.containsKey(name)) {
            connection.send(new ServerText("The name is already occupied."));
            connection.send(new NameRequest());
        } else {
            connection.send(new NameAccepted(name));
            connection.name = name;
            connection.send(new ServerText("Welcome to the chat! Type \\help for help."));
            synchronized (connections) {
                connections.put(name, connection);
            }
            broadcast(new ServerText(name + " joined the chat!"));
            synchronized (messageHistory) {
                for (byte[] message : messageHistory) {
                    connection.send(message);
                }
            }
            LOGGER.info(String.format("User %d is registered as %s", connections.size(), name));
        }
    }

    private void renameConnection(String newName, Connection connection) {
        if (newName.isEmpty()) {
            connection.send(new ServerText("The name must be non-empty."));
        } else if (connections.containsKey(newName)) {
            connection.send(new ServerText("The name is already occupied."));
        } else {
            connection.send(new NameAccepted(newName));
            String oldName = connection.name;
            synchronized (connections) {
                connections.remove(connection.name);
                connection.name = newName;
                connections.put(connection.name, connection);
            }
            broadcast(new ServerText(String.format("%s is now %s.", oldName, newName)));
            LOGGER.info(String.format("User %s is renamed to %s", oldName, newName));
        }
    }

    @Override
    public void onConnectionAccept(Connection connection) {
        connection.send(new ServerText("Enter your name."));
        connection.send(new NameRequest());
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
    public void onMessageReceive(Connection connection, Message message) {
        switch (message.getClass().getSimpleName()) {
            case "UserText": {
                Command command = Command.fromString(((UserText) message).getMessage());
                if (command != null) {
                    handleCommand(connection, command);
                    break;
                }
                synchronized (messageHistory) {
                    messageHistory.add(message.getBytes());
                }
                broadcast(message);
                break;
            }
            case "NameSent": {
                String name = ((NameSent) message).getName();
                registerNewConnection(name, connection);
                break;
            }
        }
    }

    private void handleCommand(Connection connection, Command command) {
        switch (command.getClass().getSimpleName()) {
            case "Exit": {
                connection.send(new Disconnect());
                connection.requestClose();
                break;
            }
            case "Help": {
                String[] lines = {
                    "Available commands:",
                    Help.DESCRIPTION,
                    Name.DESCRIPTION,
                    List.DESCRIPTION,
                    Exit.DESCRIPTION
                };
                String message = String.join("\n", lines);
                connection.send(new ServerText(message));
                break;
            }
            case "List": {
                StringBuilder sb;
                synchronized (connections) {
                    sb = new StringBuilder(String.format("Currently %d users connected:", connections.size()));
                    for (String user : connections.keySet()) {
                        sb.append('\n');
                        sb.append(user);
                    }
                }
                connection.send(new ServerText(sb.toString()));
                break;
            }
            case "Name": {
                renameConnection(((Name) command).getName(), connection);
                break;
            }
            case "UnknownCommand": {
                connection.send(new ServerText("Unknown command."));
            }
        }
    }
}
