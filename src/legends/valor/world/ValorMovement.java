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

        // ===== TERRAIN EXIT =====
        ValorTile fromTile = board.getTile(fromR, fromC);
        fromTile.onExit(hero);

        board.moveHero(hero, fromR, fromC, toR, toC);

        // ===== TERRAIN ENTER =====
        dest.onEnter(hero);

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

        // ===== TERRAIN EXIT (safe: monsters receive no bonuses) =====
        ValorTile fromTile = board.getTile(fromR, fromC);
        fromTile.onExit(monster);

        board.moveMonster(monster, fromR, fromC, toR, toC);

        // ===== TERRAIN ENTER =====
        dest.onEnter(monster);

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

        int fromLane = board.getLane(fromC);
        int toLane   = board.getLane(toC);
        if (fromLane == -1 || toLane == -1) return false;

        // 1) If staying in same lane, reuse normal "cannot bypass monster" rule.
        if (fromLane == toLane) {
            if (wouldHeroBypassMonster(fromR, fromC, toR, toC)) return false;
            return true;
        }

        // 2) If teleporting to a DIFFERENT lane:
        int blockRowDest = closestBlockingMonsterRow(ValorBoard.ROWS, toLane);
        if (blockRowDest != Integer.MIN_VALUE && toR < blockRowDest) return false;

        return true;
    }

    public void teleportHeroTo(Hero hero, int toR, int toC) {
        int[] pos = findHero(hero);
        if (pos == null) return;

        // ===== TERRAIN EXIT =====
        ValorTile fromTile = board.getTile(pos[0], pos[1]);
        fromTile.onExit(hero);

        board.moveHero(hero, pos[0], pos[1], toR, toC);

        // ===== TERRAIN ENTER =====
        board.getTile(toR, toC).onEnter(hero);
    }

    // =========================================================
    // "BYPASS" RULE IMPLEMENTATION
    // =========================================================

    private boolean wouldHeroBypassMonster(int fromR, int fromC, int toR, int toC) {
        int lane = board.getLane(fromC);
        int laneTo = board.getLane(toC);
        if (lane == -1 || laneTo == -1) return true;
        if (lane != laneTo) return false;

        int blockRow = closestBlockingMonsterRow(fromR, lane);
        return blockRow != Integer.MIN_VALUE && toR < blockRow;
    }

    private int closestBlockingMonsterRow(int heroRow, int lane) {
        int[] cols = board.getNexusColumnsForLane(lane);
        int best = Integer.MIN_VALUE;

        for (int r = 0; r < ValorBoard.ROWS; r++) {
            for (int c : cols) {
                Monster m = board.getTile(r, c).getMonster();
                if (m == null || m.getHP() <= 0) continue;
                if (r < heroRow) best = Math.max(best, r);
            }
        }
        return best;
    }

    private boolean wouldMonsterBypassHero(int fromR, int fromC, int toR, int toC) {
        int lane = board.getLane(fromC);
        int laneTo = board.getLane(toC);
        if (lane == -1 || laneTo == -1) return true;
        if (lane != laneTo) return false;

        int blockRow = closestBlockingHeroRow(fromR, lane);
        return blockRow != Integer.MAX_VALUE && toR > blockRow;
    }

    private int closestBlockingHeroRow(int monsterRow, int lane) {
        int[] cols = board.getNexusColumnsForLane(lane);
        int best = Integer.MAX_VALUE;

        for (int r = 0; r < ValorBoard.ROWS; r++) {
            for (int c : cols) {
                Hero h = board.getTile(r, c).getHero();
                if (h == null || h.getHP() <= 0) continue;
                if (r > monsterRow) best = Math.min(best, r);
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