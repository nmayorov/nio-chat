package nmayorov.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

class CommandTest {
    @Test
    void fromString() {
        Command command = Command.fromString("  \\help   ");
        Assertions.assertTrue(Help.class.isInstance(command));

        command = Command.fromString(" \\name  new name  ");
        Assertions.assertTrue(Name.class.isInstance(command));
        Assertions.assertEquals("new name  ", ((Name) command).getName());

        command = Command.fromString("  \\list    ");
        Assertions.assertTrue(List.class.isInstance(command));
    }
}
