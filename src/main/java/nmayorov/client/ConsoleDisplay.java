package nmayorov.client;

import nmayorov.message.Message;

public class ConsoleDisplay implements DisplaySystem {
    @Override
    public void displayMessage(Message message) {
        String text = message.getText();
        if (text != null) {
            System.out.println(text);
        }
    }

    @Override
    public void displayText(String text) {
        System.out.println(text);
    }
}
