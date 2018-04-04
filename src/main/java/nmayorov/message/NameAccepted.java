package nmayorov.message;


public class NameAccepted extends Message {
    static final String MESSAGE_NAME = "NAME_ACCEPTED";
    static final Integer FIELD_COUNT = 1;

    public NameAccepted(String name) {
        super(MESSAGE_NAME);
        fields.add(name);
    }

    public String getName() {
        return fields.get(1);
    }
}
