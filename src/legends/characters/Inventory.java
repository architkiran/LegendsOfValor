/**
 * Inventory.java
 * Stores and manages all items a hero owns.
 * Supports adding, removing, and formatted printing of items.
 */

package legends.characters;

import legends.items.Item;
import java.util.ArrayList;
import java.util.List;

public class Inventory {

    private List<Item> items;   // list of items the hero currently owns

    public Inventory() {
        items = new ArrayList<>();
    }

    // --- ADD ITEM TO INVENTORY ---
    // Adds any item to the hero's inventory
    public void addItem(Item item) {
        items.add(item);
    }

    // Backwards-compatible alias for addItem()
    public void add(Item item) {
        addItem(item);
    }

    // --- REMOVE ITEM FROM INVENTORY ---
    // Removes a specific item
    public void removeItem(Item item) {
        items.remove(item);
    }

    // Backwards-compatible alias for removeItem()
    public void remove(Item item) {
        removeItem(item);
    }

    // --- GET ALL ITEMS ---
    // Returns the internal item list
    public List<Item> getItems() {
        return items;
    }

    // --- SIMPLE INVENTORY PRINT ---
    // Prints items in a basic list for debugging or simple views
    public void print() {
        if (items.isEmpty()) {
            System.out.println("(empty)");
            return;
        }

        int index = 1;
        for (Item item : items) {
            System.out.println(index++ + ". " + item.getName() +
                               " (Lvl " + item.getRequiredLevel() + ")");
        }
    }

    // --- FORMATTED INVENTORY PRINT ---
    // Prints items inside a fixed-width box used in UI menus
    public void printFormatted(int width) {

        if (items.isEmpty()) {
            String line = "(empty)";
            int spaces = width - line.length();
            System.out.println("║ " + line + " ".repeat(spaces - 2) + " ║");
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);

            String text = (i + 1) + ". " + item.getName()
                    + "  (Lvl " + item.getRequiredLevel() + ")";

            // Ensure text fits exactly into the interior width
            int insideWidth = width - 2;
            int padding = insideWidth - text.length();
            if (padding < 0) padding = 0;

            String padded = text + " ".repeat(padding);

            System.out.println("║ " + padded + " ║");
        }
    }
}