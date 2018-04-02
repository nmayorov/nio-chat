package nmayorov.command;

import nmayorov.Connection;
import nmayorov.Server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract public class Command {
    private static final String PATTERN = "\\s*\\\\.*$";

    public static Command fromString(String string) {
        if (!Pattern.matches(PATTERN, string)) {
            return null;
        }

        Matcher p = Pattern.compile(Help.PATTERN).matcher(string);
        if (p.matches()) {
            return new Help();
        }

        p = Pattern.compile(Name.PATTERN).matcher(string);
        if (p.find()) {
            return new Name(p.group(1));
        }

        p = Pattern.compile(List.PATTERN).matcher(string);
        if (p.matches()) {
            return new List();
        }

        p = Pattern.compile(Exit.PATTERN).matcher(string);
        if (p.matches()) {
            return new Exit();
        }

        return new UnknownCommand();
    }

    abstract public void execute(Server server, Connection connection);
}
