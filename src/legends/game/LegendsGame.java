/**
 * LegendsGame
 *
 * Main game controller class responsible for:
 * - Initializing the map, market, hero party, and game states
 * - Running the intro screen and main game loop
 * - Managing transitions between different game states
 *
 * This class acts as the central engine of the game.
 */

package legends.game;

import java.util.Scanner;

import legends.world.WorldMap;
import legends.world.MapGenerator;
import legends.characters.Party;
import legends.data.DataLoader;
import legends.market.Market;
import legends.items.Item;

import java.util.List;

public class LegendsGame {

    // Current active game state (exploration, battle, inventory, etc.)
    private GameState state;

    // Core game components
    private WorldMap map;
    private Party party;
    private Market market;

    private final Scanner scanner = new Scanner(System.in);

    // ANSI color codes for text styling
    private static final String RESET = "\u001B[0m";
    private static final String BOLD  = "\u001B[1m";
    private static final String CYAN  = "\u001B[96m";
    private static final String GREEN = "\u001B[92m";
    private static final String YELL  = "\u001B[93m";

    /**
     * Constructor initializes:
     * - A randomly generated world map
     * - An empty temporary party
     * - A market loaded with all items in the game
     * - Initial game state (exploration)
     */
    public LegendsGame() {

        // Generate a default 8×8 map
        this.map = MapGenerator.generate(8);

        // Initial empty party — actual heroes selected later
        this.party = new Party();

        // Load items and construct the market
        DataLoader loader = new DataLoader();
        List<Item> items = loader.loadAllItems();
        this.market = new Market(items);

        // Set the initial state before hero selection
        this.state = new ExplorationState(party, map, this);
    }

    /**
     * Main entry point for running the entire game.
     * Handles intro, hero selection, and transitions to exploration.
     */
    public void run() {

        showIntroScreen();  // Display the opening screen and instructions

        // Load game data
        DataLoader loader = new DataLoader();
        List<Item> items = loader.loadAllItems();
        DataLoader.globalMonsters = loader.loadAllMonsters();

        // Allow the player to pick heroes
        HeroSelection selection = new HeroSelection(loader);

        System.out.println(GREEN + "Welcome to Legends of Valor!" + RESET);
        this.party = selection.selectHeroes();

        // Set exploration as the starting state after hero selection
        this.state = new ExplorationState(party, map, this);

        // Begin the main gameplay loop
        gameLoop();
    }

    /**
     * Displays introduction, instructions, and waits for user confirmation.
     */
    private void showIntroScreen() {

        System.out.println();
        System.out.println(CYAN + "══════════════════════════════════════════════" + RESET);
        System.out.println("           🏰  " + BOLD + "LEGENDS OF VALOR" + RESET + "  🗡️");
        System.out.println(CYAN + "══════════════════════════════════════════════" + RESET);
        System.out.println();

        System.out.println(BOLD + "Welcome, traveler!" + RESET);
        System.out.println(
                "Your journey begins in a world filled with danger,\n" +
                "magic, and legendary monsters. Build a powerful team\n" +
                "of heroes and guide them to victory.");
        System.out.println();

        // Gameplay explanation section
        System.out.println(YELL + BOLD + "HOW TO PLAY THE GAME:" + RESET);

        System.out.println(" • " + BOLD + "Explore the World:" + RESET + " Move across the map to find markets and battles.");
        System.out.println(" • " + BOLD + "P is the Party's current position on the map." + RESET);
        System.out.println(" • " + BOLD + "Explore:" + RESET + " Use W / A / S / D keys to move.");
        System.out.println(" • " + BOLD + "Assemble Your Party:" + RESET + " Choose 1–3 heroes to form your team.");
        System.out.println(" • " + BOLD + "Visit Markets:" + RESET + " Buy weapons, armor, spells, and potions.");
        System.out.println(" • " + BOLD + "Manage your items by opening the inventory with 'I'.");
        System.out.println(" • " + BOLD + "Battles:" + RESET + " Fight monsters using strategy!");
        System.out.println("       - Heroes act in turn");
        System.out.println("       - Monsters act after all heroes");
        System.out.println("       - Manage spells, potions, and equipment wisely");
        System.out.println();

        System.out.println(" • " + BOLD + "After each battle:" + RESET);
        System.out.println("       - Heroes heal 10% HP/MP per round");
        System.out.println("       - Fainted heroes revive at 50% HP/MP");
        System.out.println("       - Heroes earn XP & gold to grow stronger");
        System.out.println();

        System.out.println(GREEN + BOLD + "TIP:" + RESET);
        System.out.println(" • Press 'I' during exploration to open inventory.");
        System.out.println(" • Press 'Q' in battle to flee.");
        System.out.println(" • Safe tiles reduce chance of encounters.");
        System.out.println();

        // Wait for player to start
        System.out.println(CYAN + "Press ENTER when you're ready to begin..." + RESET);
        scanner.nextLine();
    }

    /**
     * Core loop that drives rendering, input handling, and state updates.
     */
    private void gameLoop() {
        while (true) {

            // Draw the current state's interface
            state.render();

            // If state signals completion, exit loop
            if (state.isFinished()) {
                break;
            }

            // Accept player input
            System.out.print("");
            String input = scanner.nextLine();

            // Pass input to the state
            state.handleInput(input);

            // Perform any background updates
            state.update(this);
        }

        System.out.println("Thank you for playing Legends of Valor!");
    }

    // -----------------------------
    // State and component getters
    // -----------------------------
    public void setState(GameState newState) {
        this.state = newState;
    }

    public WorldMap getMap() {
        return map;
    }

    public Party getParty() {
        return party;
    }

    public Market getMarket() {
        return market;
    }
}