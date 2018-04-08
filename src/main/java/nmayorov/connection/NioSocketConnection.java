package nmayorov.connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedDeque;

public class NioSocketConnection extends Connection {
    private static final int INITIAL_READ_BUFFER_CAPACITY = 128;
    private static final int RESIZE_FACTOR = 2;

    private final ConcurrentLinkedDeque<ByteBuffer> writeBuffers;
    private ByteBuffer readBuffer;

    public SocketChannel channel;

    public NioSocketConnection(SocketChannel channel) {
        this.channel = channel;
        writeBuffers = new ConcurrentLinkedDeque<>();
        readBuffer = ByteBuffer.allocate(INITIAL_READ_BUFFER_CAPACITY);
    }

    @Override
    public void write(byte[] src) {
        writeBuffers.add(ByteBuffer.wrap(src));
    }

    @Override
    public byte[] read() {
        readBuffer.flip();
        byte[] ret = new byte[readBuffer.limit()];
        readBuffer.get(ret);
        readBuffer.clear();
        return ret;
    }

    public void writeToChannel() throws IOException {
        while (!writeBuffers.isEmpty()) {
            ByteBuffer head = writeBuffers.peekFirst();
            channel.write(head);
            if (head.hasRemaining()) {
                break;
            }
            writeBuffers.removeFirst();
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
