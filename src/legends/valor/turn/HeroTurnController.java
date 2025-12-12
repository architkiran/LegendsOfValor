package legends.valor.turn;

import legends.characters.Hero;
import legends.characters.Inventory;
import legends.characters.Monster;
import legends.items.*;
import legends.valor.combat.ValorCombat;
import legends.valor.world.*;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Hero turn controller for Legends of Valor.
 *
 * Supported actions (1 action per hero per round):
 * - Move (W/A/S/D)
 * - Attack (F)
 * - Cast Spell (S)
 * - Use Potion (P)
 * - Equip Weapon/Armor (E)
 * - Teleport near another hero in a different lane (T)
 * - Recall to own nexus spawn (R)
 * - Remove Obstacle (O) -> OBSTACLE becomes PLAIN
 * - Wait (N)
 * - Quit match (Q)
 */
public class HeroTurnController {

    private final ValorBoard board;
    private final ValorMovement movement;
    private final ValorCombat combat;
    private final List<Monster> laneMonsters;
    private final ValorInput input;

    // hero -> spawn lane (fixed for Recall)
    private final Map<Hero, Integer> homeLane = new HashMap<>();

    public HeroTurnController(ValorBoard board,
                              ValorMovement movement,
                              ValorCombat combat,
                              List<Monster> laneMonsters,
                              ValorInput input) {
        this.board = board;
        this.movement = movement;
        this.combat = combat;
        this.laneMonsters = laneMonsters;
        this.input = input;
    }

    /**
     * @return false if player quits the match; true otherwise.
     */
    public boolean handleHeroTurn(Hero hero, int heroNumber) {
        bindHomeLaneIfMissing(hero);

        while (true) {
            System.out.println();
            board.print();

            System.out.println("Hero " + heroNumber + " turn: " + hero.getName());
            System.out.println("""
Controls:
  Move: W/A/S/D
  Attack: F
  Cast Spell: S
  Use Potion: P
  Equip: E
  Teleport: T
  Recall: R
  Remove Obstacle: O
  Wait: N
  Quit: Q
""");

            String cmdLine = input.readLine("Enter command: ");
            if (cmdLine == null) continue;
            cmdLine = cmdLine.trim();
            if (cmdLine.isEmpty()) continue;

            char cmd = Character.toUpperCase(cmdLine.charAt(0));

            switch (cmd) {
                case 'Q' -> { return false; }
                case 'N' -> {
                    System.out.println(hero.getName() + " waits this turn.");
                    return true;
                }
                case 'F' -> {
                    if (doAttack(hero)) return true;
                }
                case 'C' -> {
                    if (doCastSpell(hero)) return true;
                }
                case 'P' -> {
                    if (doUsePotion(hero)) return true;
                }
                case 'E' -> {
                    if (doEquip(hero)) return true;
                }
                case 'T' -> {
                    if (doTeleport(hero)) return true;
                }
                case 'R' -> {
                    if (doRecall(hero)) return true;
                }
                case 'O' -> {
                    if (doRemoveObstacle(hero)) return true;
                }
                case 'W', 'A', 'S', 'D' -> {
                    ValorDirection dir = switch (cmd) {
                        case 'W' -> ValorDirection.NORTH;
                        case 'S' -> ValorDirection.SOUTH;
                        case 'A' -> ValorDirection.WEST;
                        case 'D' -> ValorDirection.EAST;
                        default -> null;
                    };
                    if (dir == null) continue;
                    boolean moved = movement.moveHero(hero, dir);
                    if (!moved) {
                        System.out.println("Cannot move there!");
                        continue;
                    }
                    return true;
                }
                default -> System.out.println("Unknown command.");
            }
        }
    }

    // =========================================================
    // Actions
    // =========================================================

    private boolean doAttack(Hero hero) {
        List<Monster> inRange = combat.getMonstersInRange(hero);
        if (inRange.isEmpty()) {
            System.out.println("No monsters in attack range!");
            return false;
        }

        Monster target = pickMonster(inRange);
        if (target == null) return false;

        combat.heroAttack(hero, target);
        cleanupDeadMonsters();
        return true;
    }

    private boolean doCastSpell(Hero hero) {
        List<Spell> spells = getSpells(hero.getInventory());
        if (spells.isEmpty()) {
            System.out.println("You have no spells.");
            return false;
        }

        Spell spell = pickSpell(spells);
        if (spell == null) return false;

        List<Monster> inRange = combat.getMonstersInRange(hero);
        if (inRange.isEmpty()) {
            System.out.println("No monsters in range to cast on!");
            return false;
        }

        Monster target = pickMonster(inRange);
        if (target == null) return false;

        boolean ok = combat.heroCastSpell(hero, spell, target);
        cleanupDeadMonsters();
        return ok;
    }

    private boolean doUsePotion(Hero hero) {
        List<Potion> potions = getPotions(hero.getInventory());
        if (potions.isEmpty()) {
            System.out.println("You have no potions.");
            return false;
        }

        Potion p = pickPotion(potions);
        if (p == null) return false;

        // Prefer existing Hero.usePotion(Potion) if present to keep compatibility with M&H logic.
        boolean applied = tryInvokeUsePotion(hero, p);
        if (!applied) {
            // Fallback: apply via setters if your Hero exposes them.
            applied = fallbackApplyPotion(hero, p);
        }

        if (!applied) {
            System.out.println("Potion could not be applied (missing APIs).");
            return false;
        }

        hero.getInventory().removeItem(p);
        System.out.println(hero.getName() + " used potion: " + p.getName());
        return true;
    }

    private boolean doEquip(Hero hero) {
        System.out.println("""
Equip menu:
  1) Weapon
  2) Armor
  0) Cancel
""");
        String line = input.readLine("Choose: ");
        if (line == null) return false;
        line = line.trim();
        if (line.equals("0")) return false;

        if (line.equals("1")) {
            List<Weapon> weapons = getWeapons(hero.getInventory());
            if (weapons.isEmpty()) {
                System.out.println("No weapons to equip.");
                return false;
            }
            Weapon w = pickWeapon(weapons);
            if (w == null) return false;
            hero.equipWeapon(w);
            System.out.println("Equipped weapon: " + w.getName());
            return true;
        }

        if (line.equals("2")) {
            List<Armor> armors = getArmors(hero.getInventory());
            if (armors.isEmpty()) {
                System.out.println("No armors to equip.");
                return false;
            }
            Armor a = pickArmor(armors);
            if (a == null) return false;
            hero.equipArmor(a);
            System.out.println("Equipped armor: " + a.getName());
            return true;
        }

        System.out.println("Invalid choice.");
        return false;
    }

    private boolean doTeleport(Hero hero) {
        int[] myPos = movement.findHero(hero);
        if (myPos == null) return false;

        int myLane = board.getLane(myPos[1]);

        List<Hero> others = getOtherAliveHeroesOnBoard(hero);
        if (others.isEmpty()) {
            System.out.println("No other heroes to teleport to.");
            return false;
        }

        Hero target = pickHero(others);
        if (target == null) return false;

        int[] targetPos = movement.findHero(target);
        if (targetPos == null) return false;

        int targetLane = board.getLane(targetPos[1]);
        if (targetLane == myLane) {
            System.out.println("Teleport must be to a different lane. Choose a hero in another lane.");
            return false;
        }

        // Candidate adjacent positions around target (N/S/E/W), but:
        // - cannot be ahead of the target (row cannot be smaller than target row)
        // - must pass movement rules (no hero overlap, no bypass monsters, accessible, etc.)
        List<int[]> candidates = new ArrayList<>();
        int r0 = targetPos[0], c0 = targetPos[1];

        int[][] dirs = { {-1,0}, {1,0}, {0,-1}, {0,1} };
        for (int[] d : dirs) {
            int r = r0 + d[0], c = c0 + d[1];
            if (!board.inBounds(r, c)) continue;
            if (board.getLane(c) == -1) continue;
            if (r < r0) continue; // not ahead of target

            if (movement.canTeleportHeroTo(hero, r, c)) {
                candidates.add(new int[]{r, c});
            }
        }

        if (candidates.isEmpty()) {
            System.out.println("No valid teleport destination near that hero.");
            return false;
        }

        int[] chosen = pickPosition(candidates);
        if (chosen == null) return false;

        movement.teleportHeroTo(hero, chosen[0], chosen[1]);
        System.out.println(hero.getName() + " teleported!");
        return true;
    }

    private boolean doRecall(Hero hero) {
        Integer lane = homeLane.get(hero);
        if (lane == null) {
            System.out.println("Recall failed: home lane unknown.");
            return false;
        }

        // Find the two hero-nexus cells for that lane (row = 7)
        int[] cols = board.getNexusColumnsForLane(lane);
        int r = ValorBoard.ROWS - 1;

        // pick first available nexus cell
        int[] dest = null;
        if (cols.length >= 1 && board.canHeroEnter(r, cols[0])) dest = new int[]{r, cols[0]};
        else if (cols.length >= 2 && board.canHeroEnter(r, cols[1])) dest = new int[]{r, cols[1]};

        if (dest == null) {
            System.out.println("Recall failed: your nexus cells are occupied.");
            return false;
        }

        movement.teleportHeroTo(hero, dest[0], dest[1]);
        System.out.println(hero.getName() + " recalled to Nexus.");
        return true;
    }

    private boolean doRemoveObstacle(Hero hero) {
        int[] pos = movement.findHero(hero);
        if (pos == null) return false;

        System.out.println("""
Remove obstacle direction:
  W = north, A = west, S = south, D = east
  0 = cancel
""");
        String line = input.readLine("Direction: ");
        if (line == null) return false;
        line = line.trim().toUpperCase();
        if (line.equals("0")) return false;
        if (line.isEmpty()) return false;

        char d = line.charAt(0);
        int dr = 0, dc = 0;
        if (d == 'W') dr = -1;
        else if (d == 'S') dr = 1;
        else if (d == 'A') dc = -1;
        else if (d == 'D') dc = 1;
        else {
            System.out.println("Invalid direction.");
            return false;
        }

        int r = pos[0] + dr;
        int c = pos[1] + dc;
        if (!board.inBounds(r, c)) {
            System.out.println("Out of bounds.");
            return false;
        }

        ValorTile t = board.getTile(r, c);
        if (t.getType() != ValorCellType.OBSTACLE) {
            System.out.println("That cell is not an obstacle.");
            return false;
        }

        t.setType(ValorCellType.PLAIN);
        System.out.println("Obstacle removed!");
        return true;
    }

    // =========================================================
    // Helpers
    // =========================================================

    private void bindHomeLaneIfMissing(Hero hero) {
        if (homeLane.containsKey(hero)) return;

        int[] pos = movement.findHero(hero);
        if (pos == null) return;

        int lane = board.getLane(pos[1]);
        if (lane != -1) homeLane.put(hero, lane);
    }

    private void cleanupDeadMonsters() {
        laneMonsters.removeIf(m -> m == null || m.getHP() <= 0);
    }

    private Monster pickMonster(List<Monster> monsters) {
        if (monsters.size() == 1) return monsters.get(0);

        System.out.println("Monsters in range:");
        for (int i = 0; i < monsters.size(); i++) {
            Monster m = monsters.get(i);
            System.out.println((i + 1) + ") " + m.getName() + " (HP=" + (int)m.getHP() + ")");
        }
        int idx = readIndex("Choose monster #: ", monsters.size());
        return idx < 0 ? null : monsters.get(idx);
    }

    private Spell pickSpell(List<Spell> spells) {
        System.out.println("Spells:");
        for (int i = 0; i < spells.size(); i++) {
            Spell s = spells.get(i);
            System.out.println((i + 1) + ") " + s.getName()
                    + " [type=" + s.getType()
                    + ", dmg=" + s.getDamage()
                    + ", mana=" + s.getManaCost() + "]");
        }
        int idx = readIndex("Choose spell #: ", spells.size());
        return idx < 0 ? null : spells.get(idx);
    }

    private Potion pickPotion(List<Potion> potions) {
        System.out.println("Potions:");
        for (int i = 0; i < potions.size(); i++) {
            Potion p = potions.get(i);
            System.out.println((i + 1) + ") " + p.getName()
                    + " [+" + p.getEffectAmount() + " " + p.getAttributes() + "]");
        }
        int idx = readIndex("Choose potion #: ", potions.size());
        return idx < 0 ? null : potions.get(idx);
    }

    private Weapon pickWeapon(List<Weapon> weapons) {
        System.out.println("Weapons:");
        for (int i = 0; i < weapons.size(); i++) {
            Weapon w = weapons.get(i);
            System.out.println((i + 1) + ") " + w.getName()
                    + " [lvl=" + w.getRequiredLevel() + ", dmg=" + w.getDamage() + "]");
        }
        int idx = readIndex("Choose weapon #: ", weapons.size());
        return idx < 0 ? null : weapons.get(idx);
    }

    private Armor pickArmor(List<Armor> armors) {
        System.out.println("Armors:");
        for (int i = 0; i < armors.size(); i++) {
            Armor a = armors.get(i);
            System.out.println((i + 1) + ") " + a.getName()
                    + " [lvl=" + a.getRequiredLevel() + ", red=" + a.getReduction() + "]");
        }
        int idx = readIndex("Choose armor #: ", armors.size());
        return idx < 0 ? null : armors.get(idx);
    }

    private Hero pickHero(List<Hero> heroes) {
        System.out.println("Teleport targets:");
        for (int i = 0; i < heroes.size(); i++) {
            Hero h = heroes.get(i);
            System.out.println((i + 1) + ") " + h.getName());
        }
        int idx = readIndex("Choose hero #: ", heroes.size());
        return idx < 0 ? null : heroes.get(idx);
    }

    private int[] pickPosition(List<int[]> positions) {
        System.out.println("Teleport destinations:");
        for (int i = 0; i < positions.size(); i++) {
            int[] p = positions.get(i);
            System.out.println((i + 1) + ") (" + p[0] + "," + p[1] + ")");
        }
        int idx = readIndex("Choose destination #: ", positions.size());
        return idx < 0 ? null : positions.get(idx);
    }

    private int readIndex(String prompt, int size) {
        String s = input.readLine(prompt);
        if (s == null) return -1;
        s = s.trim();
        try {
            int v = Integer.parseInt(s);
            if (v < 1 || v > size) return -1;
            return v - 1;
        } catch (Exception e) {
            return -1;
        }
    }

    private List<Hero> getOtherAliveHeroesOnBoard(Hero self) {
        List<Hero> out = new ArrayList<>();
        for (int r = 0; r < ValorBoard.ROWS; r++) {
            for (int c = 0; c < ValorBoard.COLS; c++) {
                Hero h = board.getTile(r, c).getHero();
                if (h != null && h != self && h.getHP() > 0 && !out.contains(h)) {
                    out.add(h);
                }
            }
        }
        return out;
    }

    private List<Spell> getSpells(Inventory inv) {
        List<Spell> out = new ArrayList<>();
        for (Item it : inv.getItems()) if (it instanceof Spell s) out.add(s);
        return out;
    }

    private List<Potion> getPotions(Inventory inv) {
        List<Potion> out = new ArrayList<>();
        for (Item it : inv.getItems()) if (it instanceof Potion p) out.add(p);
        return out;
    }

    private List<Weapon> getWeapons(Inventory inv) {
        List<Weapon> out = new ArrayList<>();
        for (Item it : inv.getItems()) if (it instanceof Weapon w) out.add(w);
        return out;
    }

    private List<Armor> getArmors(Inventory inv) {
        List<Armor> out = new ArrayList<>();
        for (Item it : inv.getItems()) if (it instanceof Armor a) out.add(a);
        return out;
    }

    private boolean tryInvokeUsePotion(Hero hero, Potion p) {
        try {
            Method m = hero.getClass().getMethod("usePotion", Potion.class);
            m.invoke(hero, p);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Fallback potion application using common setter names.
     * If your Hero doesn't have these setters, it will safely fail and return false.
     */
    private boolean fallbackApplyPotion(Hero hero, Potion p) {
        try {
            for (PotionAttribute attr : p.getAttributes()) {
                switch (attr) {
                    case HEALTH -> invokeSetter(hero, "setHP", hero.getHP() + p.getEffectAmount());
                    case MANA -> invokeSetter(hero, "setMP", hero.getMP() + p.getEffectAmount());
                    case STRENGTH -> invokeSetter(hero, "setStrength", hero.getStrength() + p.getEffectAmount());
                    case DEXTERITY -> invokeSetter(hero, "setDexterity", hero.getDexterity() + p.getEffectAmount());
                    case AGILITY -> invokeSetter(hero, "setAgility", hero.getAgility() + p.getEffectAmount());
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void invokeSetter(Hero hero, String name, double value) throws Exception {
        Method m = hero.getClass().getMethod(name, double.class);
        m.invoke(hero, value);
    }
}
