package legends.valor.world;

import java.util.Random;

import legends.characters.Hero;
import legends.characters.Monster;

public class ValorBoard {

    public static final int ROWS = 8;
    public static final int COLS = 8;

    private final ValorTile[][] grid;
    private final Random rng = new Random();

    // ANSI colors
    private static final String RESET   = "\u001B[0m";
    private static final String BOLD    = "\u001B[1m";
    private static final String RED     = "\u001B[31m";
    private static final String GREEN   = "\u001B[32m";
    private static final String YELLOW  = "\u001B[33m";
    private static final String BLUE    = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String CYAN    = "\u001B[36m";
    private static final String WHITE   = "\u001B[37m";

    public ValorBoard() {
        grid = new ValorTile[ROWS][COLS];
        generateLayout();
    }

    public ValorTile getTile(int row, int col) {
        return grid[row][col];
    }

    public boolean inBounds(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    // =========================================================
    // BOARD GENERATION
    // =========================================================

    private void generateLayout() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {

                // Lane walls
                if (c == 2 || c == 5) {
                    grid[r][c] = new ValorTile(ValorCellType.INACCESSIBLE);
                    continue;
                }

                // Nexus rows
                if (r == 0 || r == ROWS - 1) {
                    grid[r][c] = new ValorTile(ValorCellType.NEXUS);
                    continue;
                }

                // Random terrain in lanes
                grid[r][c] = new ValorTile(randomLaneType());
            }
        }
    }

    private ValorCellType randomLaneType() {
        // Approx distribution:
        // 40% PLAIN, 20% BUSH, 20% CAVE, 15% KOULOU, 5% OBSTACLE
        int v = rng.nextInt(20);

        if (v < 8)           return ValorCellType.PLAIN;
        else if (v < 12)     return ValorCellType.BUSH;
        else if (v < 16)     return ValorCellType.CAVE;
        else if (v < 19)     return ValorCellType.KOULOU;
        else                 return ValorCellType.OBSTACLE;
    }

    // =========================================================
    // LANE & NEXUS HELPERS
    // =========================================================

    public boolean isNexus(int row, int col) {
        return row == 0 || row == ROWS - 1;
    }

    public boolean isHeroesNexus(int row, int col) {
        return row == ROWS - 1;
    }

    public boolean isMonstersNexus(int row, int col) {
        return row == 0;
    }

    /**
     * lane index:
     *   0 = top lane (cols 0–1)
     *   1 = mid lane (cols 3–4)
     *   2 = bot lane (cols 6–7)
     *  -1 = wall / not in lane
     */
    public int getLane(int col) {
        if (col == 0 || col == 1) return 0;
        if (col == 3 || col == 4) return 1;
        if (col == 6 || col == 7) return 2;
        return -1;
    }

    public boolean isWall(int row, int col) {
        return col == 2 || col == 5;
    }

    // =========================================================
    // MOVEMENT / OCCUPANCY
    // =========================================================

    public boolean canHeroEnter(int row, int col) {
        return inBounds(row, col) && grid[row][col].isEmptyForHero();
    }

    public boolean canMonsterEnter(int row, int col) {
        return inBounds(row, col) && grid[row][col].isEmptyForMonster();
    }

    public void moveHero(Hero hero, int fromR, int fromC, int toR, int toC) {
        grid[fromR][fromC].removeHero();
        grid[toR][toC].placeHero(hero);
    }

    public void moveMonster(Monster monster, int fromR, int fromC, int toR, int toC) {
        grid[fromR][fromC].removeMonster();
        grid[toR][toC].placeMonster(monster);
    }

    // =========================================================
    // SPAWN HELPERS
    // =========================================================

    public int[] getNexusColumnsForLane(int lane) {
        switch (lane) {
            case 0: return new int[]{0, 1};
            case 1: return new int[]{3, 4};
            case 2: return new int[]{6, 7};
            default: return new int[0];
        }
    }

    public int[] getHeroSpawnCell(int lane) {
        int[] cols = getNexusColumnsForLane(lane);
        return new int[]{ROWS - 1, cols[0]};
    }

    public int[] getMonsterSpawnCell(int lane) {
        int[] cols = getNexusColumnsForLane(lane);
        // monsters spawn in the RIGHT space of their lane’s Nexus
        if (cols.length < 2) return new int[]{0, cols[0]};
        return new int[]{0, cols[1]};
    }

    // =========================================================
    // PRINTING
    // =========================================================

    public void print() {
        System.out.println();
        System.out.println(MAGENTA + BOLD + "===  LEGENDS OF VALOR MAP  ===" + RESET);
        System.out.println();

        printTopBorder();

        for (int r = 0; r < ROWS; r++) {
            System.out.print("  ");
            System.out.print("┃");
            for (int c = 0; c < COLS; c++) {
                String sym = getCellSymbol(grid[r][c], r);
                System.out.print(" " + sym + " ");
                if (c < COLS - 1) {
                    System.out.print("┃");
                }
            }
            System.out.println("┃");

            if (r < ROWS - 1) {
                printMiddleBorder();
            }
        }

        printBottomBorder();
    }

    private void printTopBorder() {
        System.out.print("  ");
        System.out.print("┏");
        for (int c = 0; c < COLS; c++) {
            System.out.print("━━━");
            if (c < COLS - 1) System.out.print("┳");
        }
        System.out.println("┓");
    }

    private void printMiddleBorder() {
        System.out.print("  ");
        System.out.print("┣");
        for (int c = 0; c < COLS; c++) {
            System.out.print("━━━");
            if (c < COLS - 1) System.out.print("╋");
        }
        System.out.println("┫");
    }

    private void printBottomBorder() {
        System.out.print("  ");
        System.out.print("┗");
        for (int c = 0; c < COLS; c++) {
            System.out.print("━━━");
            if (c < COLS - 1) System.out.print("┻");
        }
        System.out.println("┛");
    }

    /**
     * Decide what symbol to show in a cell.
     *
     * Priority:
     *   0) BOTH (yellow '*')
     *   1) Hero (cyan 'H')
     *   2) Monster (red 'M')
     *   3) Terrain type (N, X, O, B, C, K, .)
     */
    private String getCellSymbol(ValorTile tile, int row) {

        // ✅ 0) BOTH present?
        if (tile.hasHero() && tile.hasMonster()) {
            return color(YELLOW, "*");
        }

        // 1) Hero present?
        if (tile.hasHero()) {
            return color(CYAN, "H");
        }

        // 2) Monster present?
        if (tile.hasMonster()) {
            return color(RED, "M");
        }

        // 3) Otherwise, terrain
        ValorCellType type = tile.getType();

        switch (type) {
            case NEXUS:
                // Monsters' nexus (top row) red, heroes' nexus (bottom row) blue
                return (row == 0) ? color(RED, "N") : color(BLUE, "N");
            case INACCESSIBLE:
                return color(WHITE, "X");
            case OBSTACLE:
                return color(MAGENTA, "O");
            case BUSH:
                return color(GREEN, "B");
            case CAVE:
                return color(CYAN, "C");
            case KOULOU:
                return color(YELLOW, "K");
            case PLAIN:
            default:
                return color(WHITE, ".");
        }
    }

    private String color(String code, String text) {
        return code + text + RESET;
    }

    // =========================================================
    // VICTORY CHECKS
    // =========================================================

    /** Heroes win if any hero stands on the monsters' Nexus row (row 0). */
    public boolean heroesReachedEnemyNexus() {
        int monstersRow = 0;
        for (int c = 0; c < COLS; c++) {
            if (grid[monstersRow][c].hasHero()) return true;
        }
        return false;
    }

    /** Monsters win if any monster stands on the heroes' Nexus row (last row). */
    public boolean monstersReachedHeroesNexus() {
        int heroesRow = ROWS - 1;
        for (int c = 0; c < COLS; c++) {
            if (grid[heroesRow][c].hasMonster()) return true;
        }
        return false;
    }
}
