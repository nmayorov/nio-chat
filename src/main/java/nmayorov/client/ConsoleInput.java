package nmayorov.client;

import java.util.Scanner;

public class ConsoleInput implements InputSystem {
    private Scanner scanner;

    public ConsoleInput() {
        scanner = new Scanner(System.in);
    }

    @Override
    public String readName() {
        return scanner.nextLine();
    }

    @Override
    public String readChatInput() {
        return scanner.nextLine();
    }
}
