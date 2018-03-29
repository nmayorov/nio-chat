package nmayorov.app;

import nmayorov.client.Client;
import nmayorov.client.ConsoleDisplay;
import nmayorov.client.ConsoleInput;

import java.net.InetSocketAddress;

public class StartDemoClient {
    private static final int PORT = 5000;

    static public void main(String[] args) throws Exception {
        Client client = new Client(new ConsoleInput(), new ConsoleDisplay());
        client.connect(new InetSocketAddress(PORT));
        client.run();
    }
}
