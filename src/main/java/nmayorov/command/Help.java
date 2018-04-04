package nmayorov.command;

public class Help extends Command {
    public static final String DESCRIPTION = "\\help --- show help";

    public Help() {
        super(Type.HELP);
    }
}
