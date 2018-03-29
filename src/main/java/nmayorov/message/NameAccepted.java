package nmayorov.message;

import nmayorov.client.Client;
import nmayorov.Connection;

public class NameAccepted extends Message {
    static final String MESSAGE_NAME = "NAME_ACCEPTED";
    static final Integer FIELD_COUNT = 1;

    public NameAccepted(String name) {
        super(MESSAGE_NAME);
        fields.add(name);
    }

    @Override
    public void handleClientReceive(Client client, Connection connection) {
        client.setName(fields.get(1));
        client.startToAcceptInput();
    }
}
