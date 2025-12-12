package legends.ui;

import java.util.ArrayList;
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

    public static void boxed(String title, List<String> lines) {
        int w = title.length();
        for (String s : lines) w = Math.max(w, stripAnsi(s).length());
        w += 4;

        String top = "┏" + "━".repeat(w) + "┓";
        String mid = "┣" + "━".repeat(w) + "┫";
        String bot = "┗" + "━".repeat(w) + "┛";

        System.out.println(top);
        System.out.println("┃ " + padRight(title, w - 2) + "┃");
        System.out.println(mid);
        for (String s : lines) {
            System.out.println("┃ " + padRight(s, w - 2) + "┃");
        }
        System.out.println(bot);
    }

    public static String padRight(String s, int width) {
        int len = stripAnsi(s).length();
        if (len >= width) return s;
        return s + " ".repeat(width - len);
    }

    public static String padLeft(String s, int width) {
        int len = stripAnsi(s).length();
        if (len >= width) return s;
        return " ".repeat(width - len) + s;
    }

    public static List<String> table(List<String[]> rows) {
        if (rows == null || rows.isEmpty()) return List.of();

        int cols = rows.get(0).length;
        int[] widths = new int[cols];

        for (String[] r : rows) {
            for (int c = 0; c < cols; c++) {
                widths[c] = Math.max(widths[c], stripAnsi(r[c]).length());
            }
        }

        List<String> out = new ArrayList<>();
        for (String[] r : rows) {
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < cols; c++) {
                sb.append(padRight(r[c], widths[c]));
                if (c < cols - 1) sb.append("  ");
            }
            out.add(sb.toString());
        }
        return out;
    }

    private static String stripAnsi(String s) {
        if (s == null) return "";
        return s.replaceAll("\u001B\\[[;\\d]*m", "");
    }
}