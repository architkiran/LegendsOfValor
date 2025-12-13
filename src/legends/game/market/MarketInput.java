package legends.game.market;

import legends.characters.Hero;
import legends.characters.Party;

import java.util.List;
import java.util.Scanner;

public class MarketInput {

    private final Scanner in;

    public MarketInput(Scanner in) {
        this.in = in;
    }

    public String readUpperTrimmedLine() {
        return in.nextLine().trim().toUpperCase();
    }

    /**
     * Reads an integer from input. Returns 0 if parsing fails.
     * (Matches your original behavior where invalid numbers cancel the action.)
     */
    public int readIntOrCancel() {
        String line = in.nextLine().trim();
        int val;
        try {
            val = Integer.parseInt(line);
        } catch (Exception e) {
            System.out.println("Invalid number.");
            return 0;
        }
        return val;
    }

    public Hero chooseHeroForTransaction(Party party, String verb, MarketView view) {
        List<Hero> heroes = party.getHeroes();
        if (heroes.isEmpty()) {
            System.out.println("No heroes in party!");
            return null;
        }

        view.printHeroTransactionHeader(verb);
        for (int i = 0; i < heroes.size(); i++) {
            view.printHeroLine(i + 1, heroes.get(i));
        }
        System.out.print("Enter number (0 = cancel): ");

        int idx = readIntOrCancel();
        if (idx == 0) return null;

        int zeroBased = idx - 1;
        if (zeroBased < 0 || zeroBased >= heroes.size()) {
            System.out.println("Invalid hero.");
            return null;
        }
        return heroes.get(zeroBased);
    }
}