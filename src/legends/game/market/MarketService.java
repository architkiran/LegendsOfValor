package legends.game.market;

import legends.characters.Hero;
import legends.items.*;

import java.util.ArrayList;
import java.util.List;

public class MarketService {

    public void buy(Hero buyer, Item item) {
        buyer.spendGold(item.getPrice());
        buyer.addItem(item);
    }

    /**
     * Sells an item at 50% of its price, removes it from inventory,
     * and returns the gold value earned.
     */
    public int sell(Hero seller, Item item) {
        int value = item.getPrice() / 2;
        seller.earnGold(value);
        seller.getInventory().removeItem(item);
        return value;
    }

    public InventoryCategories categorizeInventory(List<Item> inventoryItems) {
        InventoryCategories cats = new InventoryCategories();

        for (Item it : inventoryItems) {
            if (it instanceof Weapon) {
                cats.weapons.add((Weapon) it);
            } else if (it instanceof Armor) {
                cats.armors.add((Armor) it);
            } else if (it instanceof Potion) {
                cats.potions.add((Potion) it);
            } else if (it instanceof Spell) {
                cats.spells.add((Spell) it);
            }
        }

        return cats;
    }

    public static class InventoryCategories {
        public final List<Weapon> weapons = new ArrayList<Weapon>();
        public final List<Armor> armors = new ArrayList<Armor>();
        public final List<Potion> potions = new ArrayList<Potion>();
        public final List<Spell> spells = new ArrayList<Spell>();
    }
}