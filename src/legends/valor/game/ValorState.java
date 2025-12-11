package legends.valor.game;

/**
 * Generic state interface for Legends of Valor.
 *
 * Mirrors the Monsters & Heroes GameState idea, but decoupled from LegendsGame.
 */
public interface ValorState {

    /** Draw / print this state's UI. */
    void render();

    /** Handle a single line of user input. */
    void handleInput(String input);

    /** Background updates for this state (win/lose checks, respawns, etc.). */
    void update(ValorGame game);

    /** Whether this state is finished and should be left. */
    boolean isFinished();
}