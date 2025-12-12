package legends.valor.world;

import legends.characters.Hero;
import legends.characters.Monster;

public class ValorMovement {

    private final ValorBoard board;

    public ValorMovement(ValorBoard board) {
        this.board = board;
    }

    // =========================================================
    // HERO MOVEMENT
    // =========================================================
    public boolean moveHero(Hero hero, ValorDirection dir) {
        int[] pos = findHero(hero);
        if (pos == null) return false;

        int fromR = pos[0], fromC = pos[1];
        int toR = fromR + dir.deltaRow();
        int toC = fromC + dir.deltaCol();

        if (!board.inBounds(toR, toC)) return false;

        ValorTile dest = board.getTile(toR, toC);
        if (!dest.isEmptyForHero()) return false;

        // ✅ Valor rule: cannot move behind a monster in the same lane (cannot bypass).
        if (wouldHeroBypassMonster(fromR, fromC, toR, toC)) return false;

        board.moveHero(hero, fromR, fromC, toR, toC);
        return true;
    }

    // =========================================================
    // MONSTER MOVEMENT
    // =========================================================
    public boolean moveMonster(Monster monster, ValorDirection dir) {
        int[] pos = findMonster(monster);
        if (pos == null) return false;

        int fromR = pos[0], fromC = pos[1];
        int toR = fromR + dir.deltaRow();
        int toC = fromC + dir.deltaCol();

        if (!board.inBounds(toR, toC)) return false;

        ValorTile dest = board.getTile(toR, toC);
        if (!dest.isEmptyForMonster()) return false;

        // ✅ Symmetric rule: monster cannot bypass a hero in its lane.
        if (wouldMonsterBypassHero(fromR, fromC, toR, toC)) return false;

        board.moveMonster(monster, fromR, fromC, toR, toC);
        return true;
    }

    // =========================================================
    // TELEPORT / RULE CHECK HELPERS (used by HeroTurnController)
    // =========================================================

    /** Used by teleport too: can hero "appear" at (toR,toC) legally? */
    public boolean canTeleportHeroTo(Hero hero, int toR, int toC) {
        int[] pos = findHero(hero);
        if (pos == null) return false;
        int fromR = pos[0], fromC = pos[1];

        if (!board.inBounds(toR, toC)) return false;
        if (!board.getTile(toR, toC).isEmptyForHero()) return false;

        // Still cannot bypass monsters.
        if (wouldHeroBypassMonster(fromR, fromC, toR, toC)) return false;

        return true;
    }

    public void teleportHeroTo(Hero hero, int toR, int toC) {
        int[] pos = findHero(hero);
        if (pos == null) return;
        board.moveHero(hero, pos[0], pos[1], toR, toC);
    }

    // =========================================================
    // "BYPASS" RULE IMPLEMENTATION
    // =========================================================

    private boolean wouldHeroBypassMonster(int fromR, int fromC, int toR, int toC) {
        int lane = board.getLane(fromC);
        int laneTo = board.getLane(toC);
        if (lane == -1 || laneTo == -1) return true; // walls/invalid
        if (lane != laneTo) return false; // lane switch is allowed; bypass check uses DEST lane's blockers below

        // Find the closest blocking monster ahead of hero (smaller row).
        int blockRow = closestBlockingMonsterRow(fromR, lane);

        // If there is such a monster, hero may not move to a row smaller than blockRow (cannot go "behind" it).
        return blockRow != Integer.MIN_VALUE && toR < blockRow;
    }

    private int closestBlockingMonsterRow(int heroRow, int lane) {
        int[] cols = board.getNexusColumnsForLane(lane);
        int best = Integer.MIN_VALUE;

        for (int r = 0; r < ValorBoard.ROWS; r++) {
            for (int c : cols) {
                Monster m = board.getTile(r, c).getMonster();
                if (m == null || m.getHP() <= 0) continue;

                // "ahead" of hero means r < heroRow
                if (r < heroRow) {
                    best = Math.max(best, r); // closest ahead = largest r among those < heroRow
                }
            }
        }
        return best;
    }

    private boolean wouldMonsterBypassHero(int fromR, int fromC, int toR, int toC) {
        int lane = board.getLane(fromC);
        int laneTo = board.getLane(toC);
        if (lane == -1 || laneTo == -1) return true;
        if (lane != laneTo) return false;

        // Find closest blocking hero below monster (larger row).
        int blockRow = closestBlockingHeroRow(fromR, lane);

        // Monster moves "down". It cannot move to a row larger than blockRow (cannot go behind hero).
        return blockRow != Integer.MAX_VALUE && toR > blockRow;
    }

    private int closestBlockingHeroRow(int monsterRow, int lane) {
        int[] cols = board.getNexusColumnsForLane(lane);
        int best = Integer.MAX_VALUE;

        for (int r = 0; r < ValorBoard.ROWS; r++) {
            for (int c : cols) {
                Hero h = board.getTile(r, c).getHero();
                if (h == null || h.getHP() <= 0) continue;

                // "below" monster means r > monsterRow
                if (r > monsterRow) {
                    best = Math.min(best, r); // closest below = smallest r among those > monsterRow
                }
            }
        }
        return best;
    }

    // =========================================================
    // POSITION HELPERS
    // =========================================================
    public int[] findHero(Hero hero) {
        for (int r = 0; r < ValorBoard.ROWS; r++) {
            for (int c = 0; c < ValorBoard.COLS; c++) {
                ValorTile tile = board.getTile(r, c);
                if (tile.getHero() == hero) return new int[]{r, c};
            }
        }
        return null;
    }

    public int[] findMonster(Monster monster) {
        for (int r = 0; r < ValorBoard.ROWS; r++) {
            for (int c = 0; c < ValorBoard.COLS; c++) {
                ValorTile tile = board.getTile(r, c);
                if (tile.getMonster() == monster) return new int[]{r, c};
            }
        }
        return null;
    }
}
