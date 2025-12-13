package legends.game;

import legends.characters.Hero;
import legends.characters.Party;
import legends.items.*;
import legends.market.Market;

import legends.game.market.MarketInput;
import legends.game.market.MarketService;
import legends.game.market.MarketView;

import java.util.List;
import java.util.Scanner;

public class MarketState implements GameState {

    private final LegendsGame game;
    private final Market market;

    private final Scanner in = new Scanner(System.in);

    // Collaborators (separation of concerns)
    private final MarketView view;
    private final MarketInput input;
    private final MarketService service;

    // Whether the market is still accepting input
    private boolean waitingForInput = true;

    public MarketState(LegendsGame game, Market market) {
        this.game = game;
        this.market = market;

        this.view = new MarketView();
        this.input = new MarketInput(in);
        this.service = new MarketService();
    }

    @Override
    public boolean isFinished() {
        return !waitingForInput;
    }

    @Override
    public void render() {
        if (!waitingForInput) return;
        view.printMainMenu();
    }

    @Override
    public void handleInput(String raw) {
        if (raw == null) return;

        String inputStr = raw.trim().toUpperCase();

        if ("1".equals(inputStr)) {
            handleBuyFlow();
            return;
        }

        if ("2".equals(inputStr)) {
            handleSellFlow();
            return;
        }

        if ("B".equals(inputStr)) {
            waitingForInput = false;
            game.setState(new ExplorationState(game.getParty(), game.getMap(), game));
            return;
        }

        System.out.println("Invalid option.");
    }

    @Override
    public void update(LegendsGame game) {
        // Market is input-driven; no continuous updates.
    }

    // ---------------- Buy Flow ----------------

    private void handleBuyFlow() {
        while (true) {
            view.printBuyMenu();

            String choice = input.readUpperTrimmedLine();

            if ("1".equals(choice)) {
                buyFromList(market.getWeapons(), "BUY WEAPONS");
            } else if ("2".equals(choice)) {
                buyFromList(market.getArmor(), "BUY ARMOR");
            } else if ("3".equals(choice)) {
                buyFromList(market.getPotions(), "BUY POTIONS");
            } else if ("4".equals(choice)) {
                buyFromList(market.getSpells(), "BUY SPELLS");
            } else if ("B".equals(choice)) {
                return;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    private <T extends Item> void buyFromList(List<T> items, String title) {
        if (items == null || items.isEmpty()) {
            System.out.println("No items available in this category.");
            return;
        }

        Hero buyer = input.chooseHeroForTransaction(game.getParty(), "buy", view);
        if (buyer == null) return;

        view.printItemTable(items, title);

        System.out.print("Select item number (0 = back): ");
        int choice = input.readIntOrCancel();
        if (choice == 0) return;
        if (choice < 1 || choice > items.size()) {
            System.out.println("Invalid item.");
            return;
        }

        T item = items.get(choice - 1);

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

        // Execute purchase (behavior unchanged)
        service.buy(buyer, item);

        view.printBuySuccess(buyer, item);

        System.out.print("Press ENTER to continue...");
        in.nextLine();
    }

    // ---------------- Sell Flow ----------------

    private void handleSellFlow() {
        Hero seller = input.chooseHeroForTransaction(game.getParty(), "sell", view);
        if (seller == null) return;

        MarketService.InventoryCategories cats = service.categorizeInventory(seller.getInventory().getItems());

        while (true) {
            view.printSellMenu();

            String choice = input.readUpperTrimmedLine();

            if ("1".equals(choice)) {
                sellFromList(seller, cats.weapons, "SELL WEAPONS");
            } else if ("2".equals(choice)) {
                sellFromList(seller, cats.armors, "SELL ARMOR");
            } else if ("3".equals(choice)) {
                sellFromList(seller, cats.potions, "SELL POTIONS");
            } else if ("4".equals(choice)) {
                sellFromList(seller, cats.spells, "SELL SPELLS");
            } else if ("B".equals(choice)) {
                return;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    private <T extends Item> void sellFromList(Hero seller, List<T> items, String title) {
        if (items == null || items.isEmpty()) {
            System.out.println("No " + title.toLowerCase().replace("sell ", "") + " in inventory.");
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

        // Keep the local category list consistent with what was sold
        items.remove(choice - 1);

        view.printSellSuccess(seller, item, value);

        System.out.print("Press ENTER to continue...");
        in.nextLine();
    }
}