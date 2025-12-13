package legends.valor.ui;

import legends.characters.Hero;
import legends.characters.Monster;
import legends.valor.world.ValorBoard;
import legends.valor.world.ValorTile;

import java.util.ArrayList;
import java.util.List;

public class ValorRoundStatusView {

    // ANSI
    private static final String RESET = "\u001B[0m";
    private static final String BOLD  = "\u001B[1m";
    private static final String DIM   = "\u001B[2m";
    private static final String RED   = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW= "\u001B[33m";
    private static final String CYAN  = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";

    private static final int WIDTH = 72;

    public void printRoundStatus(int round, ValorBoard board) {
        if (board == null) return;

        // Collect
        List<HeroRow> heroes = new ArrayList<HeroRow>();
        List<MonsterRow> monsters = new ArrayList<MonsterRow>();

        for (int r = 0; r < ValorBoard.ROWS; r++) {
            for (int c = 0; c < ValorBoard.COLS; c++) {
                ValorTile t = board.getTile(r, c);
                if (t == null) continue;

                Hero h = t.getHero();
                if (h != null) {
                    heroes.add(new HeroRow(h, r, c, laneName(board.getLane(c))));
                }

                Monster m = t.getMonster();
                if (m != null) {
                    monsters.add(new MonsterRow(m, r, c, laneName(board.getLane(c))));
                }
            }
        }

        // Header box
        line(BOLD + repeat("=", WIDTH) + RESET);
        String title = " ROUND " + round + " STATUS ";
        System.out.println(centerLine(title, WIDTH));
        line(BOLD + repeat("=", WIDTH) + RESET);

        // HERO section
        printSectionHeader("HEROES ON BOARD", CYAN, heroes.size());
        if (heroes.isEmpty()) {
            line("  " + DIM + "(none)" + RESET);
        } else {
            line("  " + BOLD + String.format("%-18s %-6s %-10s %-12s %-10s", "Name", "Lv", "Pos", "HP", "MP") + RESET);
            line("  " + repeat("-", WIDTH - 2));
            for (HeroRow hr : heroes) {
                double maxHP = hr.hero.getLevel() * 100.0;
                double maxMP = hr.hero.getLevel() * 50.0;

                String hp = String.format("%.0f/%.0f", hr.hero.getHP(), maxHP);
                String mp = String.format("%.0f/%.0f", hr.hero.getMP(), maxMP);

                line("  " + String.format(
                        "%-18s %-6s %-10s %-12s %-10s %s",
                        trim(hr.hero.getName(), 18),
                        "L" + hr.hero.getLevel(),
                        "(" + hr.r + "," + hr.c + ")",
                        colorHP(hr.hero.getHP(), maxHP, hp),
                        mp,
                        DIM + hr.lane + RESET
                ));
            }
        }

        System.out.println();

        // MONSTER section
        printSectionHeader("MONSTERS ON BOARD", RED, monsters.size());
        if (monsters.isEmpty()) {
            line("  " + DIM + "(none)" + RESET);
        } else {
            line("  " + BOLD + String.format("%-18s %-6s %-10s %-10s %-10s", "Name", "Lv", "Pos", "HP", "Lane") + RESET);
            line("  " + repeat("-", WIDTH - 2));
            for (MonsterRow mr : monsters) {
                String hp = String.format("%.0f", mr.monster.getHP());
                line("  " + String.format(
                        "%-18s %-6s %-10s %-10s %-10s",
                        trim(mr.monster.getName(), 18),
                        "L" + mr.monster.getLevel(),
                        "(" + mr.r + "," + mr.c + ")",
                        colorMonsterHP(mr.monster.getHP(), hp),
                        DIM + mr.lane + RESET
                ));
            }
        }

        line(BOLD + repeat("=", WIDTH) + RESET);
        System.out.println();
    }

    // ---------------- helpers ----------------

    private void printSectionHeader(String title, String color, int count) {
        String t = " " + title + " ";
        String right = " (" + count + ") ";
        String line = t + repeat("─", Math.max(0, WIDTH - t.length() - right.length())) + right;
        System.out.println(color + BOLD + line + RESET);
    }

    private String laneName(int laneIdx) {
        switch (laneIdx) {
            case 0: return "TOP";
            case 1: return "MID";
            case 2: return "BOT";
            default: return "—";
        }
    }

    private String colorHP(double hp, double max, String txt) {
        if (hp <= 0) return RED + txt + RESET;
        double ratio = (max <= 0) ? 0 : (hp / max);
        if (ratio <= 0.30) return RED + txt + RESET;
        if (ratio <= 0.60) return YELLOW + txt + RESET;
        return GREEN + txt + RESET;
    }

    private String colorMonsterHP(double hp, String txt) {
        if (hp <= 0) return RED + txt + RESET;
        return WHITE + txt + RESET;
    }

    private void line(String s) {
        System.out.println(s);
    }

    private String centerLine(String text, int width) {
        if (text == null) text = "";
        if (text.length() >= width) return text;
        int left = (width - text.length()) / 2;
        return repeat(" ", left) + text;
    }

    private String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append(s);
        return sb.toString();
    }

    private String trim(String s, int maxLen) {
        if (s == null) return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen - 1);
    }

    // rows
    private static class HeroRow {
        final Hero hero; final int r; final int c; final String lane;
        HeroRow(Hero hero, int r, int c, String lane) { this.hero = hero; this.r = r; this.c = c; this.lane = lane; }
    }
    private static class MonsterRow {
        final Monster monster; final int r; final int c; final String lane;
        MonsterRow(Monster monster, int r, int c, String lane) { this.monster = monster; this.r = r; this.c = c; this.lane = lane; }
    }
}