package legends.valor.game;

import legends.characters.Hero;
import legends.items.Item;
import legends.market.Market;

import legends.game.market.MarketInput;
import legends.game.market.MarketService;
import legends.game.market.MarketView;

import java.util.List;
import java.util.Scanner;

/**
 * LoV Market controller (does NOT depend on LegendsGame).
 * Reuses existing MarketView/Input/Service from Monsters & Heroes.
 *
 * Policy:
 * - Only the ACTIVE hero buys/sells during their turn.
 * - Market can be opened only when the hero is standing on Heroes' Nexus row.
 */
public class ValorMarketController {

    private final Market market;
    private final MarketView view;
    private final MarketInput input;
    private final MarketService service;

    public ValorMarketController(Market market, Scanner scanner) {
        this.market = market;
        this.view = new MarketView();
        this.input = new MarketInput(scanner);
        this.service = new MarketService();
    }

    /** Blocking market loop for the active hero. */
    public void openForHero(Hero hero) {
        if (hero == null) return;
        if (market == null) {
            System.out.println("Market not available.");
            return;
        }

        boolean running = true;
        while (running) {
            view.printMainMenu(); // 1=Buy, 2=Sell, B=Back

            String cmd = input.readUpperTrimmedLine();
            if (cmd == null) continue;

            switch (cmd) {
                case "1":
                    buyFlow(hero);
                    break;
                case "2":
                    sellFlow(hero);
                    break;
                case "B":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    // ---------------- Buy Flow ----------------

    private void buyFlow(Hero hero) {
        while (true) {
            view.printBuyMenu();
            String choice = input.readUpperTrimmedLine();
            if (choice == null) continue;

            switch (choice) {
                case "1":
                    buyFromList(hero, market.getWeapons(), "BUY WEAPONS");
                    break;
                case "2":
                    buyFromList(hero, market.getArmor(), "BUY ARMOR");
                    break;
                case "3":
                    buyFromList(hero, market.getPotions(), "BUY POTIONS");
                    break;
                case "4":
                    buyFromList(hero, market.getSpells(), "BUY SPELLS");
                    break;
                case "B":
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private <T extends Item> void buyFromList(Hero buyer, List<T> items, String title) {
        if (items == null || items.isEmpty()) {
            System.out.println("No items available in this category.");
            return;
        }

        view.printItemTable(items, title);

        System.out.print("Select item number (0 = back): ");
        int choice = input.readIntOrCancel();

        if (choice == 0) return;
        if (choice < 1 || choice > items.size()) {
            System.out.println("Invalid item.");
            return;
        }

        T item = items.get(choice - 1);

        if (buyer.getLevel() < item.getRequiredLevel()) {
            System.out.println("\u001B[91mLevel too low to use this item!\u001B[0m");
            return;
        }

        if (!buyer.canAfford(item)) {
            System.out.println("\u001B[91mNot enough gold!\u001B[0m");
            return;
        }

        service.buy(buyer, item);
        view.printBuySuccess(buyer, item);

        System.out.print("Press ENTER to continue...");
        input.readUpperTrimmedLine();
    }

    // ---------------- Sell Flow ----------------

    private void sellFlow(Hero seller) {
        MarketService.InventoryCategories cats =
                service.categorizeInventory(seller.getInventory().getItems());

        while (true) {
            view.printSellMenu();
            String choice = input.readUpperTrimmedLine();
            if (choice == null) continue;

            switch (choice) {
                case "1":
                    sellFromList(seller, cats.weapons, "SELL WEAPONS");
                    break;
                case "2":
                    sellFromList(seller, cats.armors, "SELL ARMOR");
                    break;
                case "3":
                    sellFromList(seller, cats.potions, "SELL POTIONS");
                    break;
                case "4":
                    sellFromList(seller, cats.spells, "SELL SPELLS");
                    break;
                case "B":
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private <T extends Item> void sellFromList(Hero seller, List<T> items, String title) {
        if (items == null || items.isEmpty()) {
            System.out.println("No items to sell in this category.");
            return;
        }

        view.printItemTable(items, title + " (Value = 50% Price)");

        System.out.print("Select an item number to sell (or 0 to cancel): ");
        int choice = input.readIntOrCancel();

        if (choice == 0) return;
        if (choice < 1 || choice > items.size()) {
            System.out.println("Invalid item.");
            return;
        }

        T item = items.get(choice - 1);

        int value = service.sell(seller, item);
        items.remove(choice - 1);

        view.printSellSuccess(seller, item, value);

        System.out.print("Press ENTER to continue...");
        input.readUpperTrimmedLine();
    }
}