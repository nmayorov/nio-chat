package nmayorov.client;

import java.util.Random;

public class SpamBot implements InputSystem {
    static private final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static private final int MAX_WAIT_BEFORE_MESSAGE_MS = 10000;
    static private final int MAX_NAME_LENGTH = 10;
    static private final int MAX_MESSAGE_LENGTH = 100;

    private Random rng;

    private String randomString(int length) {
        char[] letters = new char[length];
        for (int i = 0; i < length; ++i) {
            letters[i] = LETTERS.charAt(rng.nextInt(LETTERS.length()));
        }
        return new String(letters);
    }

    public SpamBot() {
        rng = new Random();
    }

    @Override
    public String readName() {
        return randomString(rng.nextInt(MAX_NAME_LENGTH));
    }

    @Override
    public String readChatInput() {
        try {
            Thread.sleep(rng.nextInt(MAX_WAIT_BEFORE_MESSAGE_MS));
        } catch (InterruptedException e) {
        }

        int action = rng.nextInt(10);
        switch (action) {
            case 0:
                return "\\help";
            case 1:
                return "\\list";
            case 2:
                return "\\name " + randomString(10);
            default:
                return randomString(rng.nextInt(MAX_MESSAGE_LENGTH));
        }
    }
}
