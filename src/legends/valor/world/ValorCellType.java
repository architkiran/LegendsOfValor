package legends.valor.world;

/**
 * Types of spaces in Legends of Valor.
 *
 * Each type stores:
 *  - whether it is walkable (accessible)
 *  - a one-character base symbol (before color is applied)
 *
 * NOTE:
 *   Coloring is handled in ValorBoard.getCellSymbol()
 *   based on row (Nexus) or tile type.
 */
public enum ValorCellType {

    // Nexus tiles (top = monsters, bottom = heroes)
    NEXUS(true, "N"),

    // Completely blocked terrain (cannot enter)
    INACCESSIBLE(false, "X"),

    // Removable obstacle (hero can remove with a turn)
    OBSTACLE(false, "O"),

    // Normal terrain (no bonus)
    PLAIN(true, "."),

    // Bush: DEX bonus while inside
    BUSH(true, "B"),

    // Cave: AGI bonus while inside
    CAVE(true, "C"),

    // Koulou: STR bonus while inside
    KOULOU(true, "K");

    private final boolean accessible;
    private final String symbol;

    ValorCellType(boolean accessible, String symbol) {
        this.accessible = accessible;
        this.symbol = symbol;
    }

    /** Can a hero/monster step on this tile (ignoring occupancy)? */
    public boolean isAccessible() {
        return accessible;
    }

    /** Base symbol used before ANSI color injection. */
    public String getSymbol() {
        return symbol;
    }
}
