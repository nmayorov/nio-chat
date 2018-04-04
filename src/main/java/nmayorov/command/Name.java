package nmayorov.command;

public class Name extends Command {
    static final String PATTERN = "\\s*\\\\name\\s*(.*)$";
    public static final String DESCRIPTION = "\\name name --- change the name to a new one";

    private String name;
    Name(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
