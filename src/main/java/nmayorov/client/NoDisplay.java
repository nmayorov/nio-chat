package nmayorov.client;

import nmayorov.message.Message;

public class NoDisplay implements DisplaySystem {
    @Override
    public void displayMessage(Message message) {
    }

    @Override
    public void displayText(String text) {
    }
}
