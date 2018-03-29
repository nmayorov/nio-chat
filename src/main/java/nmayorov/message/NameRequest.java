package nmayorov.message;

import nmayorov.client.Client;
import nmayorov.Connection;

public class NameRequest extends Message {
    static final String MESSAGE_NAME = "NAME_REQUEST";
    static final Integer FIELD_COUNT = 0;

    public NameRequest() {
        super(MESSAGE_NAME);
    }

    @Override
    public void handleClientReceive(Client client, Connection connection) {
        String name = client.inputName();
        connection.send(new NameSent(name));
    }
}
