package legends.valor.ui;

import legends.characters.Hero;
import legends.characters.Monster;
import legends.items.Spell;

import java.util.regex.Pattern;

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

    // Box width (visible chars inside the border)
    private static final int WIDTH = 58;

    // ANSI escape matcher (for correct width calc)
    private static final Pattern ANSI = Pattern.compile("\\u001B\\[[;\\d]*m");

    // ---------------------------------------------------------
    // Public API (same method names you already use)
    // ---------------------------------------------------------

    public void heroAttack(Hero hero, Monster target, int damage, double hpBefore, double hpAfter) {
        printBoxHeader("ATTACK", CYAN);

        String headline = BOLD + hero.getName() + RESET + "  →  " + BOLD + target.getName() + RESET;
        printLine(" " + headline);

        printKeyVals(
                "Result", badgeHit(),
                "Damage", YELLOW + damage + RESET,
                "HP", formatHP(hpBefore) + dimArrow() + formatHP(hpAfter)
        );

        printBoxFooter();
    }

    public void monsterAttack(Monster monster, Hero hero, int damage, double hpBefore, double hpAfter) {
        printBoxHeader("MONSTER ATTACK", RED);

        String headline = BOLD + monster.getName() + RESET + "  →  " + BOLD + hero.getName() + RESET;
        printLine(" " + headline);

        printKeyVals(
                "Result", badgeHit(),
                "Damage", YELLOW + damage + RESET,
                "HP", formatHP(hpBefore) + dimArrow() + formatHP(hpAfter)
        );

        printBoxFooter();
    }

    public void spellCast(Hero hero, Spell spell, Monster target, int damage, double hpBefore, double hpAfter) {
        printBoxHeader("SPELL", MAGENTA);

        String headline = BOLD + hero.getName() + RESET
                + " casts " + MAGENTA + spell.getName() + RESET
                + "  →  " + BOLD + target.getName() + RESET;
        printLine(" " + headline);

        printKeyVals(
                "Type", CYAN + String.valueOf(spell.getType()) + RESET,
                "Damage", YELLOW + damage + RESET,
                "HP", formatHP(hpBefore) + dimArrow() + formatHP(hpAfter)
        );

        printBoxFooter();
    }

    public void dodge(String dodgerName, double dodgeChance) {
        printBoxHeader("DODGE", YELLOW);

        String headline = BOLD + dodgerName + RESET + " dodged!";
        printLine(" " + headline);

        String pct = (int) Math.round(dodgeChance * 100) + "%";
        printKeyVals(
                "Result", badgeDodge(),
                "Chance", YELLOW + pct + RESET
        );

        printBoxFooter();
    }

    /** Keep it clean: no full box spam, just a strong one-liner. */
    public void slain(String name) {
        System.out.println(GREEN + BOLD + "✔ KILL: " + name + " has been slain!" + RESET);
    }

    /** Keep it clean: no full box spam, just a strong one-liner. */
    public void fallen(String name) {
        System.out.println(RED + BOLD + "✖ DOWN: " + name + " has fallen!" + RESET);
    }

    public void info(String title, String msg) {
        printBoxHeader(title, BLUE);
        printLine(" " + msg);
        printBoxFooter();
    }

    // ---------------------------------------------------------
    // Formatting helpers (ANSI-safe width)
    // ---------------------------------------------------------

    private String badgeHit() {
        return GREEN + BOLD + "HIT" + RESET;
    }

    private String badgeDodge() {
        return YELLOW + BOLD + "DODGE" + RESET;
    }

    private String dimArrow() {
        return DIM + "  →  " + RESET;
    }

    private void printBoxHeader(String title, String color) {
        System.out.println();
        System.out.println(color + BOLD + "┌" + repeat("─", WIDTH) + "┐" + RESET);

        String t = " " + title + " ";
        System.out.println(color + "│" + RESET + padCenterVisible(BOLD + t + RESET, WIDTH) + color + "│" + RESET);

        System.out.println(color + BOLD + "├" + repeat("─", WIDTH) + "┤" + RESET);
    }

    private void printBoxFooter() {
        System.out.println(WHITE + BOLD + "└" + repeat("─", WIDTH) + "┘" + RESET);
    }

    private void printLine(String content) {
        // one content line inside the box
        System.out.println(WHITE + "│" + RESET + padRightVisible(content, WIDTH) + WHITE + "│" + RESET);
    }

    /**
     * Print 2–3 key/value lines compactly (no extra blank lines).
     * Example:
     *   Result: HIT
     *   Damage: 279
     *   HP: 100 -> 0
     */
    private void printKeyVals(String k1, String v1, String k2, String v2) {
        printKV(k1, v1);
        printKV(k2, v2);
    }

    private void printKeyVals(String k1, String v1, String k2, String v2, String k3, String v3) {
        printKV(k1, v1);
        printKV(k2, v2);
        printKV(k3, v3);
    }

    private void printKV(String key, String value) {
        String left = " " + DIM + key + RESET + ": " + value;
        printLine(left);
    }

    private String formatHP(double hp) {
        int v = (int) Math.round(hp);
        if (v <= 0) return RED + "0" + RESET;
        return String.valueOf(v);
    }

    // ---------------- width + padding (ANSI-safe) ----------------

    private int visibleLen(String s) {
        if (s == null) return 0;
        return stripAnsi(s).length();
    }

    private String stripAnsi(String s) {
        if (s == null) return "";
        return ANSI.matcher(s).replaceAll("");
    }

    private String padRightVisible(String s, int width) {
        if (s == null) s = "";
        int len = visibleLen(s);
        if (len >= width) return truncateVisible(s, width);
        StringBuilder sb = new StringBuilder(s);
        for (int i = 0; i < (width - len); i++) sb.append(' ');
        return sb.toString();
    }

    private String padCenterVisible(String s, int width) {
        if (s == null) s = "";
        int len = visibleLen(s);
        if (len >= width) return truncateVisible(s, width);

        int left = (width - len) / 2;
        int right = width - len - left;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < left; i++) sb.append(' ');
        sb.append(s);
        for (int i = 0; i < right; i++) sb.append(' ');
        return sb.toString();
    }

    /**
     * Truncate by visible chars. Keeps ANSI codes but stops once visible width reached.
     * Simple + good enough for console.
     */
    private String truncateVisible(String s, int width) {
        if (s == null) return "";
        if (visibleLen(s) <= width) return s;

        StringBuilder out = new StringBuilder();
        int visible = 0;

        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);

            // Copy ANSI sequences without counting them
            if (ch == '\u001B') {
                int j = i;
                while (j < s.length() && s.charAt(j) != 'm') j++;
                if (j < s.length()) {
                    out.append(s, i, j + 1);
                    i = j;
                    continue;
                }
            }

            if (visible >= width) break;
            out.append(ch);
            visible++;
        }

        return out.toString();
    }

    private String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append(s);
        return sb.toString();
    }
}