package legends.valor.world;

import legends.characters.Hero;
import legends.characters.Monster;

public class ValorMovement {

    private final ValorBoard board;

    public ValorMovement(ValorBoard board) {
        if (board == null) {
            throw new IllegalArgumentException("ValorBoard cannot be null");
        }
        this.board = board;
    }

    // =========================================================
    // HERO MOVEMENT
    // =========================================================
    public boolean moveHero(Hero hero, ValorDirection dir) {
        if (hero == null || dir == null) return false;

        int[] pos = findHero(hero);
        if (pos == null) return false;

        int fromR = pos[0], fromC = pos[1];
        int toR = fromR + dir.deltaRow();
        int toC = fromC + dir.deltaCol();

        if (!board.inBounds(toR, toC)) return false;

        ValorTile dest = board.getTile(toR, toC);
        if (!dest.isEmptyForHero()) return false;

        // Valor rule: cannot move behind a monster in the same lane (cannot bypass).
        if (wouldHeroBypassMonster(fromR, fromC, toR, toC)) return false;

        // TERRAIN EXIT
        ValorTile fromTile = board.getTile(fromR, fromC);
        fromTile.onExit(hero);

        board.moveHero(hero, fromR, fromC, toR, toC);

        // TERRAIN ENTER
        dest.onEnter(hero);

        return true;
    }

    // =========================================================
    // MONSTER MOVEMENT
    // =========================================================
    public boolean moveMonster(Monster monster, ValorDirection dir) {
        if (monster == null || dir == null) return false;

        int[] pos = findMonster(monster);
        if (pos == null) return false;

        int fromR = pos[0], fromC = pos[1];
        int toR = fromR + dir.deltaRow();
        int toC = fromC + dir.deltaCol();

        if (!board.inBounds(toR, toC)) return false;

        ValorTile dest = board.getTile(toR, toC);
        if (!dest.isEmptyForMonster()) return false;

        // Symmetric rule: monster cannot bypass a hero in its lane.
        if (wouldMonsterBypassHero(fromR, fromC, toR, toC)) return false;

        // TERRAIN EXIT (safe: monsters receive no bonuses)
        ValorTile fromTile = board.getTile(fromR, fromC);
        fromTile.onExit(monster);

        board.moveMonster(monster, fromR, fromC, toR, toC);

        // TERRAIN ENTER
        dest.onEnter(monster);

        return true;
    }

    // =========================================================
    // TELEPORT / RULE CHECK HELPERS (used by HeroTurnController)
    // =========================================================

    /** Used by teleport too: can hero "appear" at (toR,toC) legally? */
    public boolean canTeleportHeroTo(Hero hero, int toR, int toC) {
        if (hero == null) return false;

        int[] pos = findHero(hero);
        if (pos == null) return false;

        int fromR = pos[0], fromC = pos[1];

        if (!board.inBounds(toR, toC)) return false;
        if (!board.getTile(toR, toC).isEmptyForHero()) return false;

        int fromLane = board.getLane(fromC);
        int toLane   = board.getLane(toC);
        if (fromLane == -1 || toLane == -1) return false;

        // If staying in same lane, reuse normal "cannot bypass monster" rule.
        if (fromLane == toLane) {
            return !wouldHeroBypassMonster(fromR, fromC, toR, toC);
        }

        // If teleporting to a different lane: cannot appear "behind" the foremost monster in that lane.
        int blockRowDest = closestBlockingMonsterRow(ValorBoard.ROWS, toLane);
        return blockRowDest == Integer.MIN_VALUE || toR >= blockRowDest;
    }

    public void teleportHeroTo(Hero hero, int toR, int toC) {
        if (hero == null) return;
        if (!board.inBounds(toR, toC)) return;

        // Keep it safe: don't teleport into an occupied/illegal destination
        if (!board.getTile(toR, toC).isEmptyForHero()) return;

        int[] pos = findHero(hero);
        if (pos == null) return;

        // TERRAIN EXIT
        ValorTile fromTile = board.getTile(pos[0], pos[1]);
        fromTile.onExit(hero);

        board.moveHero(hero, pos[0], pos[1], toR, toC);

        // TERRAIN ENTER
        board.getTile(toR, toC).onEnter(hero);
    }

    // =========================================================
    // "BYPASS" RULE IMPLEMENTATION
    // =========================================================

    private boolean wouldHeroBypassMonster(int fromR, int fromC, int toR, int toC) {
        int laneFrom = board.getLane(fromC);
        int laneTo   = board.getLane(toC);

        // If either cell isn't in a lane, treat as illegal/bypass scenario.
        if (laneFrom == -1 || laneTo == -1) return true;

        // Only enforce bypass rule within the same lane.
        if (laneFrom != laneTo) return false;

        int blockRow = closestBlockingMonsterRow(fromR, laneFrom);
        return blockRow != Integer.MIN_VALUE && toR < blockRow;
    }

    /**
     * @return greatest row index r such that:
     *  - there's an alive monster in lane
     *  - r is strictly ABOVE (closer to enemy nexus) than referenceRow
     * If none exists, returns Integer.MIN_VALUE.
     */
    private int closestBlockingMonsterRow(int referenceRow, int lane) {
        int[] cols = board.getNexusColumnsForLane(lane);
        int best = Integer.MIN_VALUE;

        for (int r = 0; r < ValorBoard.ROWS; r++) {
            for (int i = 0; i < cols.length; i++) {
                int c = cols[i];
                Monster m = board.getTile(r, c).getMonster();
                if (!isAlive(m)) continue;

                if (r < referenceRow) {
                    best = Math.max(best, r);
                }
            }
        }
        return best;
    }

    private boolean wouldMonsterBypassHero(int fromR, int fromC, int toR, int toC) {
        int laneFrom = board.getLane(fromC);
        int laneTo   = board.getLane(toC);

        if (laneFrom == -1 || laneTo == -1) return true;
        if (laneFrom != laneTo) return false;

        int blockRow = closestBlockingHeroRow(fromR, laneFrom);
        return blockRow != Integer.MAX_VALUE && toR > blockRow;
    }

    /**
     * @return smallest row index r such that:
     *  - there's an alive hero in lane
     *  - r is strictly BELOW (closer to heroes nexus) than referenceRow
     * If none exists, returns Integer.MAX_VALUE.
     */
    private int closestBlockingHeroRow(int referenceRow, int lane) {
        int[] cols = board.getNexusColumnsForLane(lane);
        int best = Integer.MAX_VALUE;

        for (int r = 0; r < ValorBoard.ROWS; r++) {
            for (int i = 0; i < cols.length; i++) {
                int c = cols[i];
                Hero h = board.getTile(r, c).getHero();
                if (!isAlive(h)) continue;

                if (r > referenceRow) {
                    best = Math.min(best, r);
                }
            }
        }
        return best;
    }

    private boolean isAlive(Hero h) {
        return h != null && h.getHP() > 0;
    }

    private boolean isAlive(Monster m) {
        return m != null && m.getHP() > 0;
    }

    // =========================================================
    // POSITION HELPERS
    // =========================================================
    public int[] findHero(Hero hero) {
        if (hero == null) return null;

        for (int r = 0; r < ValorBoard.ROWS; r++) {
            for (int c = 0; c < ValorBoard.COLS; c++) {
                ValorTile tile = board.getTile(r, c);
                if (tile.getHero() == hero) return new int[]{r, c};
            }
        }
        return null;
    }

    public int[] findMonster(Monster monster) {
        if (monster == null) return null;

        for (int r = 0; r < ValorBoard.ROWS; r++) {
            for (int c = 0; c < ValorBoard.COLS; c++) {
                ValorTile tile = board.getTile(r, c);
                if (tile.getMonster() == monster) return new int[]{r, c};
            }
        }
        return null;
    }
}