package legends.valor.turn;

import java.util.Scanner;

/**
 * Console-based implementation of ValorInput.
 */
public class ConsoleValorInput implements ValorInput {

    private final Scanner in = new Scanner(System.in);

    @Override
    public String readLine(String prompt) {
        System.out.print(prompt);
        return in.nextLine();
    }
}