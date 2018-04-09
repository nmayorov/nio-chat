package nmayorov.connection;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ModeChangeRequestQueue {
    private final ConcurrentLinkedDeque<ModeChangeRequest> queue;
    private final Selector selector;

    public ModeChangeRequestQueue(Selector selector) {
        queue = new ConcurrentLinkedDeque<>();
        this.selector = selector;
    }

    public void add(ModeChangeRequest request) {
        queue.add(request);
    }

    public void process() {
        ModeChangeRequest request = queue.poll();
        while (request != null) {
            SelectionKey key = request.connection.channel.keyFor(this.selector);
            if (key != null && key.isValid()) {
                key.interestOps(request.ops);
            }
            request = queue.poll();
        }
    }
}
