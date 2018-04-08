package nmayorov.message;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

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

        for (Message message : messages) {
            Message messageFromBytes = MessageFactory.createFromBytes(message.getBytes());
            Assertions.assertEquals(message, messageFromBytes);
        }
    }

    @Test
    void testInvalidMessageException() {
        Message message = new UserText("Lena", "bye");
        byte[] bytes = message.getBytes();
        bytes[2] = (byte) 'X';

        Assertions.assertThrows(MessageFactory.InvalidMessageException.class,
                                () -> MessageFactory.createFromBytes(bytes));
    }
}
