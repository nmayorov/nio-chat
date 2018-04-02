package nmayorov.message;

import nmayorov.Connection;
import nmayorov.client.Client;

public class Disconnect extends Message {
    static final String MESSAGE_NAME = "DISCONNECT";
    static final Integer FIELD_COUNT = 0;

    public Disconnect() {
        super(MESSAGE_NAME);
    }

    @Override
    public void handleClientReceive(Client client, Connection connection) {
        client.stop();
    }
}
