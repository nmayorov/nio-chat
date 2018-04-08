package nmayorov.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract public class Command {
    public enum Type {EXIT, HELP, LIST, NAME, UNKNOWN_COMMAND}

    private static final Pattern GENERAL_PATTERN = Pattern.compile("\\s*\\\\.*$");

    private static class PatternAndType {
        final Pattern pattern;
        final Type type;

        PatternAndType(Pattern pattern, Type type) {
            this.pattern = pattern;
            this.type = type;
        }
    }

    private static final PatternAndType[] PATTERNS = {
        new PatternAndType(Pattern.compile("\\s*\\\\exit\\s*$"), Type.EXIT),
        new PatternAndType(Pattern.compile("\\s*\\\\help\\s*$"), Type.HELP),
        new PatternAndType(Pattern.compile("\\s*\\\\list\\s*$"), Type.LIST),
        new PatternAndType(Pattern.compile("\\s*\\\\name\\s*(.*)$"), Type.NAME),
    };

    public static Command fromString(String string) {
        if (!GENERAL_PATTERN.matcher(string).matches()) {
            return null;
        }

        Type type = Type.UNKNOWN_COMMAND;
        Matcher matcher = null;
        for (PatternAndType patternAndType : PATTERNS) {
            matcher = patternAndType.pattern.matcher(string);
            if (matcher.find()) {
                type = patternAndType.type;
                break;
            }
        }

        switch (type) {
            case EXIT:
                return new Exit();
            case HELP:
                return new Help();
            case LIST:
                return new List();
            case NAME:
                return new Name(matcher.group(1));
            case UNKNOWN_COMMAND:
                return new UnknownCommand();
        }
        assert false : "Unhandled command type";

        return null;
    }

    private final Type type;

    Command(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
