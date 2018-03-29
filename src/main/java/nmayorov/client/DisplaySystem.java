package nmayorov.client;

import nmayorov.message.Message;

public interface DisplaySystem {
    void displayMessage(Message message);
    void displayText(String text);
}
