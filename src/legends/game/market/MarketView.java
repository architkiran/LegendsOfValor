package legends.game.market;

import legends.characters.Hero;
import legends.items.*;

import java.util.List;

public class MarketView {

    // Color constants for display
    private static final String RESET   = "\u001B[0m";
    private static final String GREEN   = "\u001B[92m";
    private static final String YELLOW  = "\u001B[93m";
    private static final String CYAN    = "\u001B[96m";
    private static final String MAGENTA = "\u001B[95m";

    private static final int MENU_WIDTH = 72;

    public void printMainMenu() {
        System.out.println();
        printMenuBox(
                "MARKET",
                new String[]{
                        "  1. Buy Items",
                        "  2. Sell Items",
                        "  B. Back to Map"
                },
                YELLOW
        );
        System.out.print("Choose option: ");
    }

    public void printBuyMenu() {
        System.out.println();
        printMenuBox(
                "BUY MENU",
                new String[]{
                        "  1. Weapons",
                        "  2. Armor",
                        "  3. Potions",
                        "  4. Spells",
                        "  B. Back"
                },
                CYAN
        );
        System.out.print("Choose category: ");
    }

    public void printSellMenu() {
        System.out.println();
        printMenuBox(
                "SELL MENU",
                new String[]{
                        "  1. Weapons",
                        "  2. Armor",
                        "  3. Potions",
                        "  4. Spells",
                        "  B. Back"
                },
                CYAN
        );
        System.out.print("Choose category to sell from: ");
    }

    public void printHeroTransactionHeader(String verb) {
        System.out.println("\n" + MAGENTA + "Choose a hero to " + verb + " with:" + RESET);
    }

    public void printHeroLine(int indexOneBased, Hero hero) {
        System.out.printf("%d. %s (Gold: %d)%n", indexOneBased, hero.getName(), hero.getGold());
    }

    public void printBuySuccess(Hero buyer, Item item) {
        System.out.println(GREEN + "✔ " + buyer.getName() + " bought " + item.getName() + RESET);
        System.out.println("Remaining gold: " + buyer.getGold());
    }

    public void printSellSuccess(Hero seller, Item item, int value) {
        System.out.println(GREEN + "✔ Sold " + item.getName() + " for " + value + " gold." + RESET);
        System.out.println("New gold total: " + seller.getGold());
    }

    public <T extends Item> void printItemTable(List<T> items, String title) {
        final int WIDTH = 61;

        System.out.println("╔" + repeat("═", WIDTH) + "╗");
        System.out.println("║" + center(title, WIDTH) + "║");
        System.out.println("╠" + repeat("═", WIDTH) + "╣");

        System.out.printf(
                "║ %-3s %-20s %-12s %-12s %-8s ║%n",
                "No", "Name", "Type", "Price", "Lvl"
        );

        System.out.println("╠" + repeat("─", WIDTH) + "╣");

        for (int i = 0; i < items.size(); i++) {
            T it = items.get(i);

            System.out.printf(
                    "║ %-3d %-20s %-12s %-12d %-8d ║%n",
                    i + 1,
                    trimTo(it.getName(), 20),
                    trimTo(typeName(it), 12),
                    it.getPrice(),
                    it.getRequiredLevel()
            );
        }

        System.out.println("╚" + repeat("═", WIDTH) + "╝");
    }

    private String typeName(Item it) {
        if (it instanceof Weapon) return "Weapon";
        if (it instanceof Armor)  return "Armor";
        if (it instanceof Potion) return "Potion";
        if (it instanceof Spell)  return "Spell";
        return "Item";
    }

    private String center(String text, int width) {
        if (text.length() >= width) return text;
        int pad = (width - text.length()) / 2;
        return repeat(" ", pad) + text + repeat(" ", width - pad - text.length());
    }

    private String pad(String text, int width) {
        if (text.length() >= width) return text;
        return text + repeat(" ", width - text.length());
    }

    private String trimTo(String s, int maxLen) {
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen - 1);
    }

    private void printMenuBox(String title, String[] lines, String titleColor) {
        System.out.println("╔" + repeat("═", MENU_WIDTH) + "╗");
        String centered = center(title, MENU_WIDTH);
        System.out.println("║" + titleColor + centered + RESET + "║");
        System.out.println("╠" + repeat("═", MENU_WIDTH) + "╣");

        for (String line : lines) {
            String padded = pad(line, MENU_WIDTH);
            System.out.println("║" + GREEN + padded + RESET + "║");
        }

        System.out.println("╚" + repeat("═", MENU_WIDTH) + "╝");
    }

    // Java 8 replacement for String.repeat(...)
    private String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append(s);
        return sb.toString();
    }
}