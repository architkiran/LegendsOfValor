/**
 * MarketState.java
 * Represents the Market state in the game.
 * Handles buying and selling of items, switching heroes,
 * and interacting with the in-game Market object.
 *
 * This state remains active until the user chooses to go back
 * to the ExplorationState. All item transactions happen here.
 */

package legends.game;

import legends.characters.*;
import legends.items.*;
import legends.market.Market;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MarketState implements GameState {

    private final LegendsGame game;
    private final Market market;
    private final Scanner in = new Scanner(System.in);

    // Color constants for display
    private static final String RESET   = "\u001B[0m";
    private static final String GREEN   = "\u001B[92m";
    private static final String YELLOW  = "\u001B[93m";
    private static final String CYAN    = "\u001B[96m";
    private static final String MAGENTA = "\u001B[95m";

    // Menu display width for formatting
    private static final int MENU_WIDTH = 72;

    // Whether the market is still accepting input
    private boolean waitingForInput = true;

    /**
     * Creates a new MarketState where players can buy/sell items.
     * @param game   reference to the running game
     * @param market active market object containing item lists
     */
    public MarketState(LegendsGame game, Market market) {
        this.game = game;
        this.market = market;
    }

    /**
     * Indicates if the MarketState should exit.
     * @return true if exiting to another state
     */
    @Override
    public boolean isFinished() {
        return !waitingForInput;
    }

    /**
     * Displays the main Market menu (Buy, Sell, Back).
     */
    @Override
    public void render() {
        if (!waitingForInput) return;

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

    /**
     * Handles user selections from the Market menu.
     */
    @Override
    public void handleInput(String input) {
        switch (input.trim().toUpperCase()) {
            case "1":
                waitingForInput = true;
                handleBuyMenu();
                break;
            case "2":
                waitingForInput = true;
                handleSellMenu();
                break;
            case "B":
                waitingForInput = false;
                game.setState(new ExplorationState(game.getParty(), game.getMap(), game));
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    /**
     * Market-specific update logic (unused here).
     */
    @Override
    public void update(LegendsGame game) {}

    /**
     * Handles the Buy submenu and its category choices.
     */
    private void handleBuyMenu() {

        while (true) {
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

            String choice = in.nextLine().trim().toUpperCase();

            switch (choice) {
                case "1":
                    buyFromList(market.getWeapons(), "BUY WEAPONS");
                    break;
                case "2":
                    buyFromList(market.getArmor(), "BUY ARMOR");
                    break;
                case "3":
                    buyFromList(market.getPotions(), "BUY POTIONS");
                    break;
                case "4":
                    buyFromList(market.getSpells(), "BUY SPELLS");
                    break;
                case "B":
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    /**
     * Allows the player to pick which hero will buy/sell.
     * @param verb the action ("buy" / "sell") used in the prompt
     * @return the chosen Hero, or null if cancelled
     */
    private Hero chooseHeroForTransaction(String verb) {
        List<Hero> heroes = game.getParty().getHeroes();
        if (heroes.isEmpty()) {
            System.out.println("No heroes in party!");
            return null;
        }

        System.out.println("\n" + MAGENTA + "Choose a hero to " + verb + " with:" + RESET);
        for (int i = 0; i < heroes.size(); i++) {
            Hero h = heroes.get(i);
            System.out.printf("%d. %s (Gold: %d)%n", i + 1, h.getName(), h.getGold());
        }
        System.out.print("Enter number (0 = cancel): ");

        int idx;
        try {
            idx = Integer.parseInt(in.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid number.");
            return null;
        }
        if (idx == 0) return null;

        idx--;
        if (idx < 0 || idx >= heroes.size()) {
            System.out.println("Invalid hero.");
            return null;
        }
        return heroes.get(idx);
    }

    /**
     * Handles buying items from a given category.
     */
    private <T extends Item> void buyFromList(List<T> items, String title) {

        if (items == null || items.isEmpty()) {
            System.out.println("No items available in this category.");
            return;
        }

        Hero buyer = chooseHeroForTransaction("buy");
        if (buyer == null) return;

        System.out.println();
        printItemTable(items, title);

        System.out.print("Select item number (0 = back): ");
        int choice;
        try {
            choice = Integer.parseInt(in.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid number.");
            return;
        }
        if (choice == 0) return;

        int idx = choice - 1;
        if (idx < 0 || idx >= items.size()) {
            System.out.println("Invalid item.");
            return;
        }

        T item = items.get(idx);

        // Level check
        if (buyer.getLevel() < item.getRequiredLevel()) {
            System.out.println("\u001B[91mLevel too low to use this item!\u001B[0m");
            return;
        }

        // Gold check
        if (!buyer.canAfford(item)) {
            System.out.println("\u001B[91mNot enough gold!\u001B[0m");
            return;
        }

        buyer.spendGold(item.getPrice());
        buyer.addItem(item);

        System.out.println(GREEN + "✔ " + buyer.getName() + " bought " + item.getName()
                + RESET);
        System.out.println("Remaining gold: " + buyer.getGold());
        System.out.print("Press ENTER to continue...");
        in.nextLine();
    }

    /**
     * Handles the Sell submenu logic.
     */
    private void handleSellMenu() {

        Hero seller = chooseHeroForTransaction("sell");
        if (seller == null) return;

        List<Item> inventoryItems = seller.getInventory().getItems();
        List<Weapon> weapons = new ArrayList<>();
        List<Armor> armors  = new ArrayList<>();
        List<Potion> potions = new ArrayList<>();
        List<Spell> spells   = new ArrayList<>();

        // Split items into categories
        for (Item it : inventoryItems) {
            if (it instanceof Weapon) weapons.add((Weapon) it);
            else if (it instanceof Armor) armors.add((Armor) it);
            else if (it instanceof Potion) potions.add((Potion) it);
            else if (it instanceof Spell) spells.add((Spell) it);
        }

        while (true) {
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

            String choice = in.nextLine().trim().toUpperCase();
            switch (choice) {
                case "1":
                    sellFromList(seller, weapons, "SELL WEAPONS");
                    break;
                case "2":
                    sellFromList(seller, armors, "SELL ARMOR");
                    break;
                case "3":
                    sellFromList(seller, potions, "SELL POTIONS");
                    break;
                case "4":
                    sellFromList(seller, spells, "SELL SPELLS");
                    break;
                case "B":
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    /**
     * Handles selling items from the chosen list.
     */
    private <T extends Item> void sellFromList(Hero seller, List<T> items, String title) {
        if (items == null || items.isEmpty()) {
            System.out.println("No " + title.toLowerCase().replace("sell ", "") + " in inventory.");
            return;
        }

        System.out.println();
        printItemTable(items, title + " (Value = 50% Price)");

        System.out.print("Select an item number to sell (or 0 to cancel): ");
        int choice;
        try {
            choice = Integer.parseInt(in.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid number.");
            return;
        }
        if (choice == 0) return;

        int idx = choice - 1;
        if (idx < 0 || idx >= items.size()) {
            System.out.println("Invalid item.");
            return;
        }

        T item = items.get(idx);

        int value = item.getPrice() / 2;
        seller.earnGold(value);
        seller.getInventory().removeItem(item);
        items.remove(idx);

        System.out.println(GREEN + "✔ Sold " + item.getName()
                + " for " + value + " gold." + RESET);
        System.out.println("New gold total: " + seller.getGold());
        System.out.print("Press ENTER to continue...");
        in.nextLine();
    }

    /**
     * Prints the item table used in Buy/Sell listings.
     */
    private <T extends Item> void printItemTable(List<T> items, String title) {

        final int WIDTH = 61;

        System.out.println("╔" + "═".repeat(WIDTH) + "╗");
        System.out.println("║" + center(title, WIDTH) + "║");
        System.out.println("╠" + "═".repeat(WIDTH) + "╣");

        System.out.printf(
            "║ %-3s %-20s %-12s %-12s %-8s ║%n",
            "No", "Name", "Type", "Price", "Lvl"
        );

        System.out.println("╠" + "─".repeat(WIDTH) + "╣");

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

        System.out.println("╚" + "═".repeat(WIDTH) + "╝");
    }

    /**
     * Returns a display-friendly type name for an item.
     */
    private String typeName(Item it) {
        if (it instanceof Weapon) return "Weapon";
        if (it instanceof Armor)  return "Armor";
        if (it instanceof Potion) return "Potion";
        if (it instanceof Spell)  return "Spell";
        return "Item";
    }

    /**
     * Centers text within a fixed width.
     */
    private String center(String text, int width) {
        if (text.length() >= width) return text;
        int pad = (width - text.length()) / 2;
        return " ".repeat(pad) + text + " ".repeat(width - pad - text.length());
    }

    /**
     * Pads text on the right up to a fixed width.
     */
    private String pad(String text, int width) {
        if (text.length() >= width) return text;
        return text + " ".repeat(width - text.length());
    }

    /**
     * Cuts off a string if it is longer than maxLen.
     */
    private String trimTo(String s, int maxLen) {
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen - 1);
    }

    /**
     * Prints a general-purpose menu box for Market menus.
     */
    private void printMenuBox(String title, String[] lines, String titleColor) {
        System.out.println("╔" + "═".repeat(MENU_WIDTH) + "╗");
        String centered = center(title, MENU_WIDTH);
        System.out.println("║" + titleColor + centered + RESET + "║");
        System.out.println("╠" + "═".repeat(MENU_WIDTH) + "╣");
        for (String line : lines) {
            String padded = pad(line, MENU_WIDTH);
            System.out.println("║" + GREEN + padded + RESET + "║");
        }
        System.out.println("╚" + "═".repeat(MENU_WIDTH) + "╝");
    }
}