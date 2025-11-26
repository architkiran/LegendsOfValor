/**
 * Handles hero selection at the start of the game.
 * Allows the player to choose 1–3 heroes from Warriors, Paladins, or Sorcerers.
 * 
 * Responsibilities:
 * - Load all heroes from DataLoader
 * - Apply PDF HP/MP rules (HP = lvl × 100, MP = lvl × 50)
 * - Show a formatted selection menu grouped by hero type
 * - Build the player's final Party object
 */

package legends.game;

import legends.characters.*;
import legends.data.DataLoader;

import java.util.*;

public class HeroSelection {

    // Lists of heroes grouped by type for easy display and selection
    private final List<Warrior>  warriors;
    private final List<Paladin>  paladins;
    private final List<Sorcerer> sorcerers;

    // Single Scanner instance for user input
    private final Scanner in = new Scanner(System.in);

    // ANSI color codes used for formatting menu output
    private static final String RESET = "\u001B[0m";
    private static final String BOLD  = "\u001B[1m";
    private static final String BLUE  = "\u001B[94m";
    private static final String CYAN  = "\u001B[96m";
    private static final String GREEN = "\u001B[92m";
    private static final String YELL  = "\u001B[93m";
    private static final String MAG   = "\u001B[95m";
    private static final String RED   = "\u001B[91m";

    /**
     * Loads all hero types using DataLoader and immediately resets
     * their HP/MP using the PDF rules.
     */
    public HeroSelection(DataLoader loader) {
        this.warriors  = loader.loadWarriors();
        this.paladins  = loader.loadPaladins();
        this.sorcerers = loader.loadSorcerers();

        // Apply PDF rules right away to all heroes
        resetHeroStatsToPDF(warriors);
        resetHeroStatsToPDF(paladins);
        resetHeroStatsToPDF(sorcerers);
    }

    /**
     * Enforces the HP/MP rules for each hero in a list.
     * PDF rules:
     * HP = level × 100
     * MP = level × 50
     */
    private <T extends Hero> void resetHeroStatsToPDF(List<T> heroes) {
        for (Hero h : heroes) {
            h.setHP(h.getLevel() * 100);
            h.setMP(h.getLevel() * 50);
        }
    }

    /**
     * Core selection loop:
     * - Displays all heroes
     * - Lets the user pick 1–3
     * - Builds and returns a Party object
     */
    public Party selectHeroes() {

        final int MIN_PARTY = 1;
        final int MAX_PARTY = 3;

        Party party = new Party();

        while (true) {

            // Print hero menu and get mapping from numbers → actual Hero object
            Map<Integer, Hero> indexMap = printHeroMenu();

            System.out.println();
            System.out.println(YELL + "You may choose between " + MIN_PARTY + " and " + MAX_PARTY + " heroes." + RESET);
            System.out.println("Select by number, or " + BOLD + "0" + RESET + " to finish.");
            System.out.println();

            // Allow selection until 3 heroes max
            while (party.getHeroes().size() < MAX_PARTY) {
                int currentCount = party.getHeroes().size();
                System.out.print("Select hero " + (currentCount + 1) + " of " + MAX_PARTY + " (0 = done): ");

                String line = in.nextLine().trim();

                // User wants to finish choosing
                if (line.equals("0")) {
                    if (party.getHeroes().size() >= MIN_PARTY) break;
                    System.out.println(RED("You must have at least one hero."));
                    continue;
                }

                int choice;
                try { choice = Integer.parseInt(line); }
                catch (NumberFormatException e) {
                    System.out.println(RED("Invalid input. Enter a number."));
                    continue;
                }

                // Look up hero by number
                Hero chosen = indexMap.get(choice);
                if (chosen == null) {
                    System.out.println(RED("No hero with number " + choice + "."));
                    continue;
                }

                // Prevent duplicates
                if (party.getHeroes().contains(chosen)) {
                    System.out.println(YELL + chosen.getName() + " is already chosen." + RESET);
                    continue;
                }

                // Add hero to party
                party.addHero(chosen);
                System.out.println(GREEN + "✔ Added " + chosen.getName() + RESET);

                // Stop selection if limit reached
                if (party.getHeroes().size() == MAX_PARTY) {
                    System.out.println("\nYou have reached the maximum (" + MAX_PARTY + ").");
                }
            }

            // Requirements met → exit loop
            if (party.getHeroes().size() >= MIN_PARTY) break;

            System.out.println("\n" + RED("You must pick at least one hero.") + "\n");
        }

        // Print final summary of chosen heroes
        printFinalParty(party);
        return party;
    }

    // =========================================================
    // PRINTING / UI HELPERS
    // =========================================================

    /**
     * Displays all available heroes grouped by class.
     * Also creates and returns a map linking index numbers to heroes.
     */
    private Map<Integer, Hero> printHeroMenu() {
        Map<Integer, Hero> indexMap = new LinkedHashMap<>();
        int idx = 1;

        System.out.println();
        System.out.println("=================================================");
        System.out.println("            " + MAG + "🧙 HERO SELECTION 🛡️" + RESET);
        System.out.println("=================================================");
        System.out.println("Choose from " + BLUE + "Warriors" + RESET + ", "
                + CYAN + "Paladins" + RESET + ", " + GREEN + "Sorcerers" + RESET + ".");
        System.out.println();

        String header = BOLD +
                String.format("%-4s %-20s %-4s %-6s %-6s %-6s %-6s %-6s",
                        "No", "Name", "Lvl", "HP", "MP", "STR", "DEX", "AGI") + RESET;

        // --- Warriors section ---
        if (!warriors.isEmpty()) {
            System.out.println(BLUE + "⚔️  WARRIORS — Strong melee fighters" + RESET);
            System.out.println(header);
            System.out.println("--------------------------------------------------------");
            for (Warrior w : warriors) {
                printHeroRow(idx, w);
                indexMap.put(idx++, w);
            }
            System.out.println();
        }

        // --- Paladins section ---
        if (!paladins.isEmpty()) {
            System.out.println(CYAN + "🛡️  PALADINS — Holy warriors with balanced stats" + RESET);
            System.out.println(header);
            System.out.println("--------------------------------------------------------");
            for (Paladin p : paladins) {
                printHeroRow(idx, p);
                indexMap.put(idx++, p);
            }
            System.out.println();
        }

        // --- Sorcerers section ---
        if (!sorcerers.isEmpty()) {
            System.out.println(MAG + "✨ SORCERERS — Powerful magic users" + RESET);
            System.out.println(header);
            System.out.println("--------------------------------------------------------");
            for (Sorcerer s : sorcerers) {
                printHeroRow(idx, s);
                indexMap.put(idx++, s);
            }
            System.out.println();
        }

        return indexMap;
    }

    /**
     * Prints one formatted row in the hero table.
     */
    private void printHeroRow(int idx, Hero h) {
        System.out.printf(
                "%-4d %-20s %-4d %-6d %-6d %-6d %-6d %-6d%n",
                idx,
                h.getName(),
                h.getLevel(),
                (int) h.getHP(),
                (int) h.getMP(),
                (int) h.getStrength(),
                (int) h.getDexterity(),
                (int) h.getAgility()
        );
    }

    /**
     * Prints the final chosen party summary after selection finishes.
     */
    private void printFinalParty(Party party) {
        System.out.println();
        System.out.println("=============== " + GREEN + "YOUR PARTY" + RESET + " ===============");

        for (Hero h : party.getHeroes()) {
            System.out.println(" → " + BOLD + h.getName() + RESET + " (Level " + h.getLevel() + ")");
            System.out.println("    HP: " + (int) h.getHP() + " / " + (h.getLevel() * 100));
            System.out.println("    MP: " + (int) h.getMP() + " / " + (h.getLevel() * 50));
            System.out.println("    STR: " + (int) h.getStrength()
                    + "   DEX: " + (int) h.getDexterity()
                    + "   AGI: " + (int) h.getAgility()
                    + "   Gold: " + h.getGold());
            System.out.println();
        }

        System.out.println("===========================================\n");
    }

    private static String RED(String s) {
        return "\u001B[91m" + s + RESET;
    }
}