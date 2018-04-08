package nmayorov.app;

import nmayorov.server.ChatLogic;
import nmayorov.server.Server;

import java.io.IOException;
import java.net.InetSocketAddress;

class StartDemoServer {
    private static final int PORT = 5000;

    public static void main(String[] args) throws IOException {
        Server server = new Server(new ChatLogic(), new InetSocketAddress(PORT));
        server.start();
    }
}
