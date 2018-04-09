package nmayorov.app;

import nmayorov.chat.Chat;
import nmayorov.server.NioServer;

import java.io.IOException;
import java.net.InetSocketAddress;

class StartDemoServer {
    private static final int PORT = 5000;

    public static void main(String[] args) throws IOException {
        NioServer server = new NioServer(new InetSocketAddress(PORT));
        Chat chat = new Chat(server, 10, 100);
        chat.start(1);
    }
}
