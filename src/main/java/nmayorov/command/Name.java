package nmayorov.command;

public class Name extends Command {
    public static final String DESCRIPTION = "\\name name --- change the name to a new one";

    private String name;

    public Name(String name) {
        super(Type.NAME);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
