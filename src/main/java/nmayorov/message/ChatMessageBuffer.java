package nmayorov.message;

import nmayorov.connection.MessageBuffer;

import java.nio.ByteBuffer;
import java.nio.InvalidMarkException;

public class ChatMessageBuffer implements MessageBuffer {
    private static final int INITIAL_CAPACITY = 128;
    private static final int RESIZE_FACTOR = 2;
    private ByteBuffer buffer;

    public ChatMessageBuffer() {
        buffer = ByteBuffer.allocate(INITIAL_CAPACITY);
    }

    @Override
    public void put(byte[] data) {
        int requiredCapacity = buffer.position() + data.length;
        if (requiredCapacity > buffer.capacity()) {
            ByteBuffer newBuffer = ByteBuffer.allocate(Math.max(RESIZE_FACTOR * buffer.capacity(), requiredCapacity));
            buffer.flip();
            newBuffer.put(buffer);
            buffer = newBuffer;
        }
        buffer.put(data);
    }

    @Override
    public byte[] getNextMessage() {
        return ByteBufferCutter.cut(buffer, Message.MESSAGE_END);
    }
}
