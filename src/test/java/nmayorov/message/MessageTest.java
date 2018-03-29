package nmayorov.message;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.nio.ByteBuffer;

class MessageTest {
    @Test
    void testFromToBytesConversion() {
        Message messages[] = {
            new UserText("Mike", "hi"),
            new NameAccepted("Nick"),
            new NameRequest(),
            new NameSent("Andrew"),
            new ServerText("hello"),
        };

        ByteBuffer buffer = ByteBuffer.allocate(128);
        for (Message message : messages) {
            buffer.put(message.getBytes());
        }

        for (Message message : messages) {
            Message messageFromBytes = Message.getNext(buffer);
            Assertions.assertEquals(message, messageFromBytes);
        }
    }

    @Test
    void testInvalidMessageException() {
        Message message = new UserText("Lena", "bye");
        byte[] bytes = message.getBytes();
        bytes[2] = (byte) 'X';

        ByteBuffer buffer = ByteBuffer.allocate(64);
        buffer.put(bytes);

        Assertions.assertThrows(Message.InvalidMessageException.class,
                                () -> Message.getNext(buffer));
    }

    @Test
    void testIncompleteMessage() {
        Message message = new ServerText("disconnected");
        byte[] bytes = message.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(64);
        buffer.put(bytes, 0, 20);
        Assertions.assertEquals(null, Message.getNext(buffer));
    }
}
