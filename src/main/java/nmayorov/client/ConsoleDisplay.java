package nmayorov.client;

public class ConsoleDisplay implements DisplaySystem {
    @Override
    public void displayMessage(String messageText) {
        if (messageText != null) {
            System.out.println(messageText);
        }
    }
}
