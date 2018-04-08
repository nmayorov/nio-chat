package nmayorov.message;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ChatMessageBufferTest {
    @Test
    void testPutAndGetNext() {
        ChatMessageBuffer buffer = new ChatMessageBuffer();

        byte[] m1 = {1, 2, 3, Message.MESSAGE_END};
        byte[] m2 = {4, 5, 6, 7, Message.MESSAGE_END};
        byte[] unfinished = {2, 3, 4};
        buffer.put(m1);
        buffer.put(m2);
        buffer.put(unfinished);

        Assertions.assertArrayEquals(m1, buffer.getNextMessage());
        Assertions.assertArrayEquals(m2, buffer.getNextMessage());
        Assertions.assertEquals(null, buffer.getNextMessage());

        buffer.put(new byte[] {5, Message.MESSAGE_END});
        byte[] m3 = {2, 3, 4, 5, Message.MESSAGE_END};
        Assertions.assertArrayEquals(m3, buffer.getNextMessage());
    }
}
