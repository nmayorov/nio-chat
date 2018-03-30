package nmayorov.app;

import nmayorov.client.Client;
import nmayorov.client.ConsoleDisplay;
import nmayorov.client.SpamBot;

import java.io.IOException;
import java.net.InetSocketAddress;

public class StartLoadTest {
    private static final int PORT = 5000;
    private static int DEFAULT_BOT_COUNT = 1000;

    public static void main(String[] args) throws IOException {
        int botCount;
        if (args.length == 2) {
            botCount = Integer.valueOf(args[1]);
        } else {
            botCount = DEFAULT_BOT_COUNT;
        }
        for (int i = 0; i < botCount; ++i) {
            Client c = new Client(new SpamBot(), new ConsoleDisplay());
            c.connect(new InetSocketAddress(PORT));
            Thread t = new Thread(c);
            t.start();
        }
    }
}
