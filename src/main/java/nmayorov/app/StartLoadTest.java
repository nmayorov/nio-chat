package nmayorov.app;

import nmayorov.client.Client;
import nmayorov.client.NoDisplay;
import nmayorov.client.SpamBot;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

class StartLoadTest {
    private static final int PORT = 5000;
    private static final int DEFAULT_BOT_COUNT = 100;

    public static void main(String[] args) throws IOException {
        int botCount;
        if (args.length == 1) {
            botCount = Integer.valueOf(args[0]);
        } else {
            botCount = DEFAULT_BOT_COUNT;
        }

        ArrayList<Client> clients = new ArrayList<>();
        for (int i = 0; i < botCount; ++i) {
            Client client = new Client(new SpamBot(), new NoDisplay());
            clients.add(client);
            client.connect(new InetSocketAddress(PORT));
            Thread t = new Thread(client);
            t.start();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (Client client : clients) {
                client.stop();
            }
        }));
    }
}
