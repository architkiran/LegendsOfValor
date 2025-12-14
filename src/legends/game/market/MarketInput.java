/**
 * MarketInput handles all console-based input interactions related to the market.
 *
 * This class is responsible only for reading and validating user input
 * (strings, integers, and hero selection) and deliberately contains no
 * business logic. It supports the separation of concerns by keeping
 * input parsing independent from market rules and UI rendering.
 */
package legends.game.market;

import legends.characters.Hero;
import legends.characters.Party;

import java.util.List;
import java.util.Scanner;

public class MarketInput {

    // Shared scanner used for all market-related input
    private final Scanner in;

    /**
     * Constructs a MarketInput wrapper around an existing Scanner.
     * The scanner lifecycle is managed externally.
     */
    public MarketInput(Scanner in) {
        this.in = in;
    }

    /**
     * Reads a full line from input, trims whitespace,
     * and normalizes it to uppercase.
     *
     * This ensures consistent command handling regardless of user casing.
     */
    public String readUpperTrimmedLine() {
        return in.nextLine().trim().toUpperCase();
    }

    /**
     * Reads an integer from input.
     *
     * If parsing fails, this method prints an error message and
     * returns 0, which is treated by callers as a cancellation.
     * This preserves the original market interaction behavior.
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

    /**
     * Prompts the user to choose a hero from the given party
     * for a buy or sell transaction.
     *
     * This method delegates all display logic to MarketView
     * and performs only input validation and selection.
     *
     * @param party the party containing available heroes
     * @param verb  the action being performed (e.g., "buy", "sell")
     * @param view  the view responsible for rendering hero options
     * @return the selected Hero, or null if the action is cancelled or invalid
     */
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