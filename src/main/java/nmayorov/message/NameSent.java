package nmayorov.message;

public class NameSent extends Message {
    static final String MESSAGE_NAME = "NAME_SENT";
    static final Integer FIELD_COUNT = 1;

    public NameSent(String name) {
        super(MESSAGE_NAME);
        this.fields.add(name);
    }

    public String getName() {
        return fields.get(1);
    }
}
