package legends.game;

/**
 * Entry point of the Legends of Valor game.
 * This class simply initializes the game engine and starts execution.
 * 
 * Main responsibilities:
 *  - Create a LegendsGame instance
 *  - Call run() to start intro → hero selection → gameplay loop
 */
public class Main {

    /**
     * Program entry point.
     * Creates the game and launches the main run sequence.
     */
    public static void main(String[] args) {

        // Create the core game controller object
        LegendsGame game = new LegendsGame();

        // Begin the full game flow (intro, hero select, exploration, battles)
        game.run();
    }
}