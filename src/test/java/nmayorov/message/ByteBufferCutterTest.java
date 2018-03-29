package nmayorov.message;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ByteBufferCutterTest {
    static private final byte STOP_BYTE = 0x1E;

    @Test
    void emptyBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        byte[] result = ByteBufferCutter.cut(buffer, STOP_BYTE);
        Assertions.assertEquals(null, result);
        Assertions.assertEquals(buffer.position(), 0);
        Assertions.assertEquals(buffer.capacity(), buffer.limit());
    }

    @Test
    void noDelimiter() {
        byte[] bytes = {1, 2, 3};
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        byte[] result = ByteBufferCutter.cut(buffer, STOP_BYTE);

        Assertions.assertEquals(null, result);
        Assertions.assertEquals(buffer.position(), bytes.length);
        Assertions.assertEquals(buffer.capacity(), buffer.limit());
    }

    @Test
    void singleMessage() {
        byte[] bytes = {1, 2, 3, STOP_BYTE};
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        byte[] result = ByteBufferCutter.cut(buffer, STOP_BYTE);

        Assertions.assertArrayEquals(bytes, result);
        Assertions.assertEquals(0, buffer.position());
        Assertions.assertEquals(buffer.capacity(), buffer.limit());
    }

    @Test
    void messageWithLeftOver() {
        byte[] bytes = {1, 2, 3, STOP_BYTE, 2, 3};
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(bytes);

        byte[] result = ByteBufferCutter.cut(buffer, STOP_BYTE);
        byte[] truth = {1, 2, 3, STOP_BYTE};
        Assertions.assertArrayEquals(truth, result);
        Assertions.assertEquals(2, buffer.position());
        Assertions.assertEquals(buffer.capacity(), buffer.limit());

        result = ByteBufferCutter.cut(buffer, STOP_BYTE);
        Assertions.assertEquals(null, result);
        Assertions.assertEquals(2, buffer.position());
        Assertions.assertEquals(buffer.capacity(), buffer.limit());
    }

    @Test
    void twoMessagesWithLeftOver() {
        byte[] bytes = {1, 2, 3, STOP_BYTE, 2, 3, STOP_BYTE, 4, 5, 6};
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);

        byte[] result = ByteBufferCutter.cut(buffer, STOP_BYTE);
        byte[] truth = {1, 2, 3, STOP_BYTE};
        Assertions.assertArrayEquals(truth, result);
        Assertions.assertEquals(6, buffer.position());
        Assertions.assertEquals(buffer.capacity(), buffer.limit());

        result = ByteBufferCutter.cut(buffer, STOP_BYTE);
        truth = new byte[] {2, 3, STOP_BYTE};
        Assertions.assertArrayEquals(truth, result);
        Assertions.assertEquals(3, buffer.position());
        Assertions.assertEquals(buffer.capacity(), buffer.limit());

        result = ByteBufferCutter.cut(buffer, STOP_BYTE);
        Assertions.assertArrayEquals(null, result);
        Assertions.assertEquals(3, buffer.position());
        Assertions.assertEquals(buffer.capacity(), buffer.limit());
    }
}
