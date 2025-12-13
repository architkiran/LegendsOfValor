package legends.valor.ui;

import legends.characters.Hero;
import legends.characters.Monster;
import legends.items.Spell;

public class ValorCombatLogView {

    // ANSI
    private static final String RESET = "\u001B[0m";
    private static final String BOLD  = "\u001B[1m";
    private static final String DIM   = "\u001B[2m";
    private static final String RED   = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW= "\u001B[33m";
    private static final String BLUE  = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String CYAN  = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";

    // --------- DODGE grouping state ----------
    private String pendingDodgeName = null;
    private double pendingDodgeChance = 0.0;
    private int pendingDodgeCount = 0;

    // Call this before printing any other combat box, and at end of phases.
    public void flush() {
        if (pendingDodgeCount <= 0 || pendingDodgeName == null) return;

        printHeader("DODGE", YELLOW);

        String titleLine = " " + BOLD + pendingDodgeName + RESET + " dodged!";
        if (pendingDodgeCount > 1) {
            titleLine = " " + BOLD + pendingDodgeName + RESET + " dodged attacks!";
        }

        System.out.println(titleLine);
        printDetails("Result", YELLOW + "DODGE" + RESET);

        if (pendingDodgeCount > 1) {
            printDetails("Attempts dodged", String.valueOf(pendingDodgeCount));
        }

        int pct = (int) Math.round(pendingDodgeChance * 100.0);
        printDetails("Chance", pct + "%");

        printFooter();

        // reset
        pendingDodgeName = null;
        pendingDodgeChance = 0.0;
        pendingDodgeCount = 0;
    }

    public void heroAttack(Hero hero, Monster target, int damage, double hpBefore, double hpAfter) {
        flush();
        printHeader("ATTACK", CYAN);
        System.out.println(" " + BOLD + hero.getName() + RESET + " attacks " + BOLD + target.getName() + RESET);
        printDetails("Result", GREEN + "HIT" + RESET);
        printDetails("Damage", YELLOW + String.valueOf(damage) + RESET);
        printDetails("Target HP", formatHP(hpBefore) + "  →  " + formatHP(hpAfter));
        printFooter();
    }

    public void monsterAttack(Monster monster, Hero hero, int damage, double hpBefore, double hpAfter) {
        flush();
        printHeader("MONSTER ATTACK", RED);
        System.out.println(" " + BOLD + monster.getName() + RESET + " attacks " + BOLD + hero.getName() + RESET);
        printDetails("Result", GREEN + "HIT" + RESET);
        printDetails("Damage", YELLOW + String.valueOf(damage) + RESET);
        printDetails("Hero HP", formatHP(hpBefore) + "  →  " + formatHP(hpAfter));
        printFooter();
    }

    public void spellCast(Hero hero, Spell spell, Monster target, int damage, double hpBefore, double hpAfter) {
        flush();
        printHeader("SPELL", MAGENTA);
        System.out.println(" " + BOLD + hero.getName() + RESET + " casts " + MAGENTA + spell.getName() + RESET
                + " on " + BOLD + target.getName() + RESET);
        printDetails("Type", CYAN + String.valueOf(spell.getType()) + RESET);
        printDetails("Damage", YELLOW + String.valueOf(damage) + RESET);
        printDetails("Target HP", formatHP(hpBefore) + "  →  " + formatHP(hpAfter));
        printFooter();
    }

    // ✅ Now dodge DOES NOT print immediately — it groups duplicates.
    public void dodge(String dodgerName, double dodgeChance) {
        // If same target keeps dodging, just increment
        if (pendingDodgeName != null
                && pendingDodgeName.equals(dodgerName)
                && Math.abs(pendingDodgeChance - dodgeChance) < 0.000001) {
            pendingDodgeCount++;
            return;
        }

        // Otherwise flush previous grouped dodge and start new group
        flush();
        pendingDodgeName = dodgerName;
        pendingDodgeChance = dodgeChance;
        pendingDodgeCount = 1;
    }

    public void slain(String name) {
        flush();
        printHeader("KILL", GREEN);
        System.out.println(" " + GREEN + BOLD + "✔ " + name + " has been slain!" + RESET);
        printFooter();
    }

    public void fallen(String name) {
        flush();
        printHeader("DOWN", RED);
        System.out.println(" " + RED + BOLD + "✖ " + name + " has fallen!" + RESET);
        printFooter();
    }

    public void info(String title, String msg) {
        flush();
        printHeader(title, BLUE);
        System.out.println(" " + msg);
        printFooter();
    }

    // ---------- formatting helpers ----------

    private void printHeader(String title, String color) {
        System.out.println();
        System.out.println(color + BOLD + "┌──────────────────────────────────────────────┐" + RESET);
        String line = " " + title + " ";
        System.out.println(color + "│" + RESET + padCenter(line, 46) + color + "│" + RESET);
        System.out.println(color + BOLD + "├──────────────────────────────────────────────┤" + RESET);
    }

    private void printDetails(String k, String v) {
        String left = DIM + k + RESET + ": " + v;
        System.out.println(" " + padRight(left, 46));
    }

    private void printFooter() {
        System.out.println(WHITE + BOLD + "└──────────────────────────────────────────────┘" + RESET);
    }

    private String formatHP(double hp) {
        return (hp <= 0) ? RED + "0" + RESET : String.valueOf((int) Math.round(hp));
    }

    private String padRight(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s;
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