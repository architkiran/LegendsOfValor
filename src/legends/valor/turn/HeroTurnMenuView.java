package legends.valor.turn;

import legends.characters.Hero;
import legends.valor.world.ValorBoard;

public class HeroTurnMenuView {

    // ANSI
    private static final String RESET = "\u001B[0m";
    private static final String BOLD  = "\u001B[1m";
    private static final String CYAN  = "\u001B[36m";
    private static final String YELLOW= "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String WHITE = "\u001B[37m";

    public void renderTurnMenu(int heroNumber, Hero hero, int[] pos, int lane) {
        System.out.println();
        System.out.println(CYAN + BOLD + "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓" + RESET);

        String laneName = laneName(lane);
        String where = (pos == null) ? "(?,?)" : "(" + pos[0] + "," + pos[1] + ")";
        String title = " HERO " + heroNumber + " TURN ";
        String nameLine = hero.getName() + "  " + WHITE + where + RESET + "  " + YELLOW + laneName + RESET;

        System.out.println(CYAN + "┃" + RESET + padCenter(title, 46) + CYAN + "┃" + RESET);
        System.out.println(CYAN + "┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫" + RESET);

        // Stats line
        double maxHP = hero.getLevel() * 100.0;
        double maxMP = hero.getLevel() * 50.0;
        String stats = "Lv " + hero.getLevel()
                + " | HP " + (int) hero.getHP() + "/" + (int) maxHP
                + " | MP " + (int) hero.getMP() + "/" + (int) maxMP
                + " | Gold " + hero.getGold();

        System.out.println(CYAN + "┃ " + RESET + padRight(nameLine, 63) + CYAN + "┃" + RESET);
        System.out.println(CYAN + "┃ " + RESET + padRight(stats, 45) + CYAN + "┃" + RESET);

        System.out.println(CYAN + "┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫" + RESET);

        // Controls (2-column-ish)
        System.out.println(CYAN + "┃ " + RESET + formatKey("W/A/S/D", "Move") + "   " + formatKey("F", "Attack") + padRight("", 18) + CYAN + "┃" + RESET);
        System.out.println(CYAN + "┃ " + RESET + formatKey("C", "Cast Spell") + " " + formatKey("P", "Use Potion") + padRight("", 16) + CYAN + "┃" + RESET);
        System.out.println(CYAN + "┃ " + RESET + formatKey("E", "Equip") + "     " + formatKey("T", "Teleport") + padRight("", 19) + CYAN + "┃" + RESET);
        System.out.println(CYAN + "┃ " + RESET + formatKey("R", "Recall") + "    " + formatKey("O", "Remove Obstacle") + padRight("", 12) + CYAN + "┃" + RESET);
        System.out.println(CYAN + "┃ " + RESET + formatKey("M", "Market") + "    " + GREEN + RESET + padRight("", 6) + padRight("", 25) + CYAN + "┃" + RESET);
        System.out.println(CYAN + "┃ " + RESET + formatKey("N", "Wait") + "      " + formatKey("Q", "Quit") + padRight("", 23) + CYAN + "┃" + RESET);

        System.out.println(CYAN + BOLD + "┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛" + RESET);
    }

    private String laneName(int lane) {
        switch (lane) {
            case 0: return "TOP LANE";
            case 1: return "MID LANE";
            case 2: return "BOT LANE";
            default: return "UNKNOWN";
        }
    }

    private String formatKey(String key, String action) {
        return MAGENTA + "[" + key + "]" + RESET + " " + action;
    }

    private String padRight(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s.substring(0, width);
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < width) sb.append(' ');
        return sb.toString();
    }

    private String padCenter(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s.substring(0, width);
        int left = (width - s.length()) / 2;
        int right = width - s.length() - left;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < left; i++) sb.append(' ');
        sb.append(s);
        for (int i = 0; i < right; i++) sb.append(' ');
        return sb.toString();
    }
}