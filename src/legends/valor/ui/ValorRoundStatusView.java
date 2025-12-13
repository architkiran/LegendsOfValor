package legends.valor.ui;

import legends.characters.Hero;
import legends.characters.Monster;
import legends.valor.world.ValorBoard;

public class ValorRoundStatusView {

    // ANSI
    private static final String RESET = "\u001B[0m";
    private static final String BOLD  = "\u001B[1m";
    private static final String DIM   = "\u001B[2m";
    private static final String RED   = "\u001B[31m";
    private static final String CYAN  = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";
    private static final String GREEN = "\u001B[32m";

    // Column widths (tune if needed)
    private static final int W_NAME = 20;
    private static final int W_LV   = 4;
    private static final int W_POS  = 9;
    private static final int W_HP   = 12;
    private static final int W_MP   = 10;
    private static final int W_LANE = 6;

    private static final int TABLE_WIDTH = 86;

    private static final String LINE = repeat("=", TABLE_WIDTH);
    private static final String DASH = repeat("-", TABLE_WIDTH);

    public void printRoundStatus(ValorBoard board, int round) {
        if (board == null) return;

        int heroCount = countHeroes(board);
        int monsterCount = countMonsters(board);

        System.out.println();
        System.out.println(WHITE + BOLD + LINE + RESET);
        System.out.println(center("ROUND " + round + " STATUS", TABLE_WIDTH));
        System.out.println(WHITE + BOLD + LINE + RESET);

        // ---------------- Heroes ----------------
        System.out.println();
        System.out.println(CYAN + BOLD + "HEROES ON BOARD" + RESET + " " + DIM + "(" + heroCount + ")" + RESET);
        System.out.println(CYAN + repeat("-", TABLE_WIDTH) + RESET);

        // Header
        String heroHeader =
                padCell("Name", W_NAME) + " " +
                padCell("Lv", W_LV)     + " " +
                padCell("Pos", W_POS)   + " " +
                padCell("HP", W_HP)     + " " +
                padCell("MP", W_MP)     + " " +
                padCell("Lane", W_LANE);

        System.out.println(heroHeader);
        System.out.println(DIM + repeat("-", TABLE_WIDTH) + RESET);

        // Rows (IMPORTANT: use padCell so ANSI in HP doesn't break alignment)
        for (int r = 0; r < ValorBoard.ROWS; r++) {
            for (int c = 0; c < ValorBoard.COLS; c++) {
                Hero h = board.getTile(r, c).getHero();
                if (h == null) continue;

                String name = trimTo(h.getName(), W_NAME);
                String lv = "L" + h.getLevel();
                String pos = "(" + r + "," + c + ")";

                String hp = coloredHP((int) Math.round(h.getHP())) + "/" + (h.getLevel() * 100);
                String mp = (int) Math.round(h.getMP()) + "/" + (h.getLevel() * 50);
                String lane = laneName(board.getLane(c));

                String row =
                        padCell(name, W_NAME) + " " +
                        padCell(lv,   W_LV)   + " " +
                        padCell(pos,  W_POS)  + " " +
                        padCell(hp,   W_HP)   + " " +   // <-- ANSI-safe now
                        padCell(mp,   W_MP)   + " " +
                        padCell(lane, W_LANE);

                System.out.println(row);
            }
        }

        // ---------------- Monsters ----------------
        System.out.println();
        System.out.println(RED + BOLD + "MONSTERS ON BOARD" + RESET + " " + DIM + "(" + monsterCount + ")" + RESET);
        System.out.println(RED + repeat("-", TABLE_WIDTH) + RESET);

        String monsterHeader =
                padCell("Name", W_NAME) + " " +
                padCell("Lv", W_LV)     + " " +
                padCell("Pos", W_POS)   + " " +
                padCell("HP", 10)       + " " +
                padCell("Lane", W_LANE);

        System.out.println(monsterHeader);
        System.out.println(DIM + repeat("-", TABLE_WIDTH) + RESET);

        for (int r = 0; r < ValorBoard.ROWS; r++) {
            for (int c = 0; c < ValorBoard.COLS; c++) {
                Monster m = board.getTile(r, c).getMonster();
                if (m == null) continue;

                String name = trimTo(m.getName(), W_NAME);
                String lv = "L" + m.getLevel();
                String pos = "(" + r + "," + c + ")";
                String hp = String.valueOf((int) Math.round(m.getHP()));
                String lane = laneName(board.getLane(c));

                String row =
                        padCell(name, W_NAME) + " " +
                        padCell(lv,   W_LV)   + " " +
                        padCell(pos,  W_POS)  + " " +
                        padCell(hp,   10)     + " " +
                        padCell(lane, W_LANE);

                System.out.println(row);
            }
        }

        System.out.println(WHITE + BOLD + LINE + RESET);
        System.out.println();
    }

    // ---------------- helpers ----------------

    private int countHeroes(ValorBoard board) {
        int cnt = 0;
        for (int r = 0; r < ValorBoard.ROWS; r++) {
            for (int c = 0; c < ValorBoard.COLS; c++) {
                if (board.getTile(r, c).getHero() != null) cnt++;
            }
        }
        return cnt;
    }

    private int countMonsters(ValorBoard board) {
        int cnt = 0;
        for (int r = 0; r < ValorBoard.ROWS; r++) {
            for (int c = 0; c < ValorBoard.COLS; c++) {
                if (board.getTile(r, c).getMonster() != null) cnt++;
            }
        }
        return cnt;
    }

    private static String laneName(int lane) {
        switch (lane) {
            case 0: return "TOP";
            case 1: return "MID";
            case 2: return "BOT";
            default: return "-";
        }
    }

    private static String coloredHP(int hp) {
        if (hp <= 0) return RED + "0" + RESET;
        return GREEN + hp + RESET;
    }

    private static String trimTo(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max);
    }

    private static String center(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s;
        int left = (width - s.length()) / 2;
        int right = width - s.length() - left;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < left; i++) sb.append(' ');
        sb.append(s);
        for (int i = 0; i < right; i++) sb.append(' ');
        return sb.toString();
    }

    private static String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append(s);
        return sb.toString();
    }

    // ===== ANSI-safe padding (fixes your alignment issue) =====

    private static String padCell(String s, int width) {
        if (s == null) s = "";
        int printable = printableLength(s);
        if (printable >= width) return s;

        StringBuilder sb = new StringBuilder(s);
        for (int i = 0; i < width - printable; i++) sb.append(' ');
        return sb.toString();
    }

    private static int printableLength(String s) {
        return stripAnsi(s).length();
    }

    private static String stripAnsi(String s) {
        // removes ANSI like \u001B[32m ... \u001B[0m
        return s.replaceAll("\u001B\\[[;\\d]*m", "");
    }
}