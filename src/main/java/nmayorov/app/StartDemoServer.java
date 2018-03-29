package nmayorov.app;

import nmayorov.Server;

import java.io.IOException;
import java.net.InetSocketAddress;

public class StartDemoServer {
    private static final int PORT = 5000;

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start(new InetSocketAddress(PORT));
        server.run();
    }
}
