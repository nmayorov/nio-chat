package nmayorov.message;

public class NameSent extends Message {
    public NameSent(String name) {
        super(Type.NAME_SENT);
        this.fields.add(name);
    }

    public String getName() {
        return fields.get(1);
    }
}
