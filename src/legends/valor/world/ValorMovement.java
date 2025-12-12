package legends.valor.world;

import legends.characters.Hero;
import legends.characters.Monster;

/**
 * Handles movement on a ValorBoard according to Legends of Valor rules:
 *  - Move one tile NORTH / SOUTH / WEST / EAST
 *  - No diagonal movement
 *  - Cannot move into INACCESSIBLE tiles
 *  - Heroes cannot move into a space already occupied by another hero
 *  - Monsters cannot move into a space already occupied by another monster
 *
 * Note:
 *  - Heroes and monsters ARE allowed to share a tile (for combat).
 *  - Lane / teleport / recall rules can be added later on top of this.
 */
public class ValorMovement {

    private final ValorBoard board;

    public ValorMovement(ValorBoard board) {
        this.board = board;
    }

    // =========================================================
    //  HERO MOVEMENT
    // =========================================================

    /**
     * Attempts to move the given hero one tile in the given direction.
     * @return true if move succeeded, false if it was illegal.
     */
    public boolean moveHero(Hero hero, ValorDirection dir) {
        int[] pos = findHero(hero);
        if (pos == null) {
            // Hero is not currently on the board
            return false;
        }

        int fromR = pos[0];
        int fromC = pos[1];
        int toR = fromR + dir.deltaRow();
        int toC = fromC + dir.deltaCol();

        if (!board.inBounds(toR, toC)) {
            return false; // off-board
        }

        ValorTile dest = board.getTile(toR, toC);

        // Terrain + hero-occupancy check
        if (!dest.isEmptyForHero()) {
            return false;
        }

        // Perform move
        ValorTile src = board.getTile(fromR, fromC);
        src.removeHero();
        dest.placeHero(hero);

        return true;
    }

    // =========================================================
    //  MONSTER MOVEMENT
    // =========================================================

    /**
     * Attempts to move the given monster one tile in the given direction.
     * @return true if move succeeded, false if it was illegal.
     */
    public boolean moveMonster(Monster monster, ValorDirection dir) {
        int[] pos = findMonster(monster);
        if (pos == null) {
            // Monster is not currently on the board
            return false;
        }

        int fromR = pos[0];
        int fromC = pos[1];
        int toR = fromR + dir.deltaRow();
        int toC = fromC + dir.deltaCol();

        if (!board.inBounds(toR, toC)) {
            return false; // off-board
        }

        ValorTile dest = board.getTile(toR, toC);

        // Terrain + monster-occupancy check
        if (!dest.isEmptyForMonster()) {
            return false;
        }

        // Perform move
        ValorTile src = board.getTile(fromR, fromC);
        src.removeMonster();
        dest.placeMonster(monster);

        return true;
    }

    // =========================================================
    //  POSITION HELPERS
    // =========================================================

    /**
     * Finds the (row, col) of the given hero on the board, or null if not present.
     */
    public int[] findHero(Hero hero) {
        for (int r = 0; r < ValorBoard.ROWS; r++) {
            for (int c = 0; c < ValorBoard.COLS; c++) {
                ValorTile tile = board.getTile(r, c);
                if (tile.getHero() == hero) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }

    /**
     * Finds the (row, col) of the given monster on the board, or null if not present.
     */
    public int[] findMonster(Monster monster) {
        for (int r = 0; r < ValorBoard.ROWS; r++) {
            for (int c = 0; c < ValorBoard.COLS; c++) {
                ValorTile tile = board.getTile(r, c);
                if (tile.getMonster() == monster) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }
}
