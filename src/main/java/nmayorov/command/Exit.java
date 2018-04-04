package nmayorov.command;

public class Exit extends Command {
    public static final String DESCRIPTION = "\\exit --- exit the chat";

    public Exit() {
        super(Type.EXIT);
    }
}
