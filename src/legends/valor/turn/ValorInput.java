package legends.valor.turn;

/**
 * Simple abstraction over how we read commands from the player.
 * Lets us swap console input for tests/bots later.
 */
public interface ValorInput {
    /**
     * Read a single line command from the user after showing a prompt.
     */
    String readLine(String prompt);
}