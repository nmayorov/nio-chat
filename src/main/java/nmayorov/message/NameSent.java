package nmayorov.message;

import nmayorov.Connection;
import nmayorov.Server;

import java.util.Iterator;

public class NameSent extends Message {
    static final String MESSAGE_NAME = "NAME_SENT";
    static final Integer FIELD_COUNT = 1;

    public NameSent(String name) {
        super(MESSAGE_NAME);
        this.fields.add(name);
    }

    @Override
    public void handleServerReceive(Server server, Connection connection) {
        String name = fields.get(1);
        server.registerNewConnection(name, connection);
    }
}
