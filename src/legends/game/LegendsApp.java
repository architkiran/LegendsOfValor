package legends.game;

import java.util.Scanner;
import legends.valor.game.ValorGame;

/**
 * Top-level application for the Legends project.
 * Shows the main menu, lets the user pick which game to play,
 * then launches that game.
 */
public class LegendsApp implements Game {

    // Simple ANSI colors
    private static final String RESET   = "\u001B[0m";
    private static final String BOLD    = "\u001B[1m";
    private static final String CYAN    = "\u001B[36m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String YELLOW  = "\u001B[33m";

    @Override
    public void run() {
        Scanner in = new Scanner(System.in);

        boolean running = true;
        while (running) {

            // ========= HEADER =========
            System.out.println();
            System.out.println(MAGENTA + "══════════════════════════════════════════════" + RESET);
            System.out.println("          " + BOLD + "WELCOME TO LEGENDS" + RESET);
            System.out.println(MAGENTA + "══════════════════════════════════════════════" + RESET);
            System.out.println();

            // Short description
            System.out.println("Choose which adventure you want to play:");
            System.out.println();

            // ========= MENU BOX =========
            System.out.println(CYAN + "  [1] Legends: Monsters & Heroes" + RESET);
            System.out.println("      Classic exploration with random battles,");
            System.out.println("      markets, and turn-based combat.");
            System.out.println();
            System.out.println(CYAN + "  [2] Legends of Valor" + RESET);
            System.out.println("      3-lane tactical battle for control of the Nexuses.");
            System.out.println();
            System.out.println(YELLOW + "  [Q] Quit" + RESET);
            System.out.println();

            System.out.print("Enter your choice (1 / 2 / Q): ");
            String input = in.nextLine().trim().toUpperCase();

            Game selectedGame = null;

            switch (input) {
                case "1":
                    selectedGame = new MonstersAndHeroesGame();
                    break;
                case "2":
                    selectedGame = new ValorGame();
                    break;
                case "Q":
                    running = false;
                    continue;   // skip launching a game
                default:
                    System.out.println("\nInvalid choice. Please type 1, 2, or Q.\n");
                    continue;
            }

            // Launch the chosen game
            System.out.println();
            System.out.println(MAGENTA + "Launching " + gameName(selectedGame) + "..." + RESET);
            System.out.println();

            selectedGame.run();

            // When the game returns, show a small prompt before redrawing the menu
            System.out.println();
            System.out.print("Press ENTER to return to the main menu...");
            in.nextLine();
        }

        System.out.println();
        System.out.println("Thanks for playing " + BOLD + "Legends" + RESET + "!");
    }

    private String gameName(Game game) {
        if (game instanceof ValorGame) {
            return "Legends of Valor";
        } else if (game instanceof MonstersAndHeroesGame) {
            return "Legends: Monsters & Heroes";
        }
        return "the game";
    }
}