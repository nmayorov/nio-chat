package nmayorov;

import nmayorov.message.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;


public class Connection {
    private static final int INITIAL_READ_BUFFER_CAPACITY = 128;
    private static final int RESIZE_FACTOR = 2;

    private ArrayDeque<ByteBuffer> writeBuffers;
    private ByteBuffer readBuffer;

    public SocketChannel channel;
    public String name;

    public Connection(SocketChannel channel) {
        this.channel = channel;
        name = null;
        writeBuffers = new ArrayDeque<>();
        readBuffer = ByteBuffer.allocate(INITIAL_READ_BUFFER_CAPACITY);
    }

    public void send(byte[] bytes) {
        writeBuffers.add(ByteBuffer.wrap(bytes));
    }
    public void send(Message message) {
        send(message.getBytes());
    }

    public void write() throws IOException {
        while (!writeBuffers.isEmpty()) {
            ByteBuffer head = writeBuffers.peekFirst();
            channel.write(head);
            if (head.hasRemaining()) {
                break;
            }
            writeBuffers.removeFirst();
        }
    }

    public int read() throws IOException {
        int bytesRead = channel.read(readBuffer);
        if (readBuffer.position() == readBuffer.capacity()) {
            readBuffer.rewind();
            ByteBuffer newBuffer = ByteBuffer.allocate(RESIZE_FACTOR * readBuffer.capacity());
            newBuffer.put(readBuffer);
            readBuffer = newBuffer;
        }
        return bytesRead;
    }

    public ByteBuffer getReadBuffer() {
        return readBuffer;
    }
}
