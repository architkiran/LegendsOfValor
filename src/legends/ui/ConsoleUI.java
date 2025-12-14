package legends.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ConsoleUI {

    private ConsoleUI() {}

    // ===== ANSI =====
    public static final String RESET = "\u001B[0m";
    public static final String BOLD  = "\u001B[1m";
    public static final String DIM   = "\u001B[2m";

    public static final String RED     = "\u001B[31m";
    public static final String GREEN   = "\u001B[32m";
    public static final String YELLOW  = "\u001B[33m";
    public static final String BLUE    = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN    = "\u001B[36m";
    public static final String WHITE   = "\u001B[37m";

    // ===== Box + Tables =====
    public static void clearLineJunk() {
        // fixes weird "^[" artifacts on some terminals if cursor control got printed
        System.out.print(RESET);
    }

    public static void sectionTitle(String title) {
        System.out.println();
        System.out.println(MAGENTA + BOLD + "══ " + title + " ══" + RESET);
    }

    /**
     * Draw a box like:
     * ┏━━━━━━━━━━━━━━━━━━━━┓
     * ┃ Title              ┃
     * ┣━━━━━━━━━━━━━━━━━━━━┫
     * ┃ line1              ┃
     * ┃ line2              ┃
     * ┗━━━━━━━━━━━━━━━━━━━━┛
     *
     * IMPORTANT:
     * - Width must be computed from VISIBLE length (strip ANSI).
     * - Padding width must match exactly: "┃ " + content(padded to innerWidth) + " ┃"
     */
    public static void boxed(String title, List<String> lines) {
        if (title == null) title = "";
        if (lines == null) lines = Collections.emptyList();

        // innerWidth = max visible width of title/lines
        int innerWidth = visibleLen(title);
        for (String s : lines) innerWidth = Math.max(innerWidth, visibleLen(s));

        // Full box width uses innerWidth (content area), plus fixed side decorations printed below.
        // Top/bottom lines need: "┏" + ("━" repeated (innerWidth + 2)) + "┓"
        // because inside we print: "┃ " + content(innerWidth padded) + " ┃"  => innerWidth + 2 spaces
        String top = "┏" + repeat("━", innerWidth + 2) + "┓";
        String mid = "┣" + repeat("━", innerWidth + 2) + "┫";
        String bot = "┗" + repeat("━", innerWidth + 2) + "┛";

        System.out.println(top);
        System.out.println("┃ " + padRight(title, innerWidth) + " ┃");
        System.out.println(mid);
        for (String s : lines) {
            System.out.println("┃ " + padRight(s, innerWidth) + " ┃");
        }
        System.out.println(bot);
    }

    public static String padRight(String s, int width) {
        if (s == null) s = "";
        int len = visibleLen(s);
        if (len >= width) return s;
        return s + repeat(" ", width - len);
    }

    public static String padLeft(String s, int width) {
        if (s == null) s = "";
        int len = visibleLen(s);
        if (len >= width) return s;
        return repeat(" ", width - len) + s;
    }

    /**
     * Build a simple padded table (no borders).
     * Each column width is computed with visible length (ANSI stripped).
     */
    public static List<String> table(List<String[]> rows) {
        if (rows == null || rows.isEmpty()) return Collections.emptyList();

        int cols = rows.get(0).length;
        int[] widths = new int[cols];

        for (String[] r : rows) {
            for (int c = 0; c < cols; c++) {
                String cell = (r[c] == null) ? "" : r[c];
                widths[c] = Math.max(widths[c], visibleLen(cell));
            }
        }

        List<String> out = new ArrayList<>();
        for (String[] r : rows) {
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < cols; c++) {
                String cell = (r[c] == null) ? "" : r[c];
                sb.append(padRight(cell, widths[c]));
                if (c < cols - 1) sb.append("  ");
            }
            out.add(sb.toString());
        }
        return out;
    }

    // ===== Helpers =====

    private static int visibleLen(String s) {
        return stripAnsi(s).length();
    }

    private static String stripAnsi(String s) {
        if (s == null) return "";
        return s.replaceAll("\u001B\\[[;\\d]*m", "");
    }

    private static String repeat(String s, int n) {
        if (n <= 0) return "";
        StringBuilder sb = new StringBuilder(n * s.length());
        for (int i = 0; i < n; i++) sb.append(s);
        return sb.toString();
    }
}
