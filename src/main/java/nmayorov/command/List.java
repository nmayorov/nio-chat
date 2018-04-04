package nmayorov.command;

public class List extends Command {
    public static final String DESCRIPTION = "\\list --- list connected users";

    public List() {
        super(Type.LIST);
    }
}
