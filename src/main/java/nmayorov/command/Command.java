package nmayorov.command;

import nmayorov.Connection;
import nmayorov.Server;
import nmayorov.message.ServerText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Command {
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

        return new Command();
    }

    public void execute(Server server, Connection connection) {
        connection.send(new ServerText("Unknown command."));
    }
}
