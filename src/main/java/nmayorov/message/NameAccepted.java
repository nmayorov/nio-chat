package nmayorov.message;


public class NameAccepted extends Message {
    public NameAccepted(String name) {
        super(Type.NAME_ACCEPTED);
        fields.add(name);
    }

    public String getName() {
        return fields.get(1);
    }
}
