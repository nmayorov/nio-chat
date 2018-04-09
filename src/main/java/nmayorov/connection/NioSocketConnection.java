package nmayorov.connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedDeque;

public class NioSocketConnection extends Connection {
    private enum Mode {READ, WRITE}

    private static final int INITIAL_READ_BUFFER_CAPACITY = 128;
    private static final int RESIZE_FACTOR = 2;

    private final ConcurrentLinkedDeque<ByteBuffer> writeBuffers;
    private ByteBuffer readBuffer;

    private final Selector selector;
    public final SocketChannel channel;

    private final ModeChangeRequestQueue modeChangeRequestQueue;

    private Mode mode;

    public NioSocketConnection(Selector selector, SocketChannel channel,
                               ModeChangeRequestQueue modeChangeRequestQueue) throws IOException {
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ, this);

        this.selector = selector;
        this.channel = channel;
        this.modeChangeRequestQueue = modeChangeRequestQueue;

        this.writeBuffers = new ConcurrentLinkedDeque<>();
        this.readBuffer = ByteBuffer.allocate(INITIAL_READ_BUFFER_CAPACITY);

        this.mode = Mode.READ;
    }

    @Override
    public void write(byte[] src) {
        writeBuffers.add(ByteBuffer.wrap(src));
        if (this.mode == Mode.READ) {
            this.mode = Mode.WRITE;
            modeChangeRequestQueue.add(new ModeChangeRequest(this, SelectionKey.OP_WRITE));
            this.selector.wakeup();
        }
    }

    @Override
    public byte[] getData() {
        readBuffer.flip();
        byte[] ret = new byte[readBuffer.limit()];
        readBuffer.get(ret);
        readBuffer.clear();
        return ret;
    }

    public void writeToChannel() throws IOException {
        while (!writeBuffers.isEmpty()) {
            ByteBuffer head = writeBuffers.peek();
            channel.write(head);
            if (head.hasRemaining()) {
                break;
            }
            writeBuffers.remove();
        }
        if (writeBuffers.isEmpty()) {
            this.mode = Mode.READ;
            modeChangeRequestQueue.add(new ModeChangeRequest(this, SelectionKey.OP_READ));
        }
    }

    public int readFromChannel() throws IOException {
        int bytesRead = channel.read(readBuffer);
        if (readBuffer.position() == readBuffer.capacity()) {
            readBuffer.rewind();
            ByteBuffer newBuffer = ByteBuffer.allocate(RESIZE_FACTOR * readBuffer.capacity());
            newBuffer.put(readBuffer);
            readBuffer = newBuffer;
        }
        return bytesRead;
    }

    public boolean nothingToWrite() {
        return writeBuffers.isEmpty();
    }
}
