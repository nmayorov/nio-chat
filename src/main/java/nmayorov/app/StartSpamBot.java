package nmayorov.app;

import nmayorov.client.Client;
import nmayorov.client.ConsoleDisplay;
import nmayorov.client.SpamBot;

import java.net.InetSocketAddress;

class StartSpamBot {
    private static final int PORT = 5000;

    static public void main(String[] args) throws Exception {
        Client client = new Client(new SpamBot(), new ConsoleDisplay());
        client.connect(new InetSocketAddress(PORT));
        client.run();
    }
}
