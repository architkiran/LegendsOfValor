package legends.valor.turn;

import legends.characters.Hero;
import legends.characters.Monster;
import legends.items.*;
import legends.market.Market;
import legends.valor.combat.ValorCombat;
import legends.valor.world.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class HeroActionService {

    private final ValorBoard board;
    private final ValorMovement movement;
    private final ValorCombat combat;
    private final List<Monster> laneMonsters;
    private final HeroTurnUIHelper ui;
    private final Map<Hero, Integer> homeLane;

    private final Market market;
    private final Scanner scanner;

    public HeroActionService(ValorBoard board,
                             ValorMovement movement,
                             ValorCombat combat,
                             List<Monster> laneMonsters,
                             HeroTurnUIHelper ui,
                             Map<Hero, Integer> homeLane,
                             Market market,
                             Scanner scanner) {
        this.board = board;
        this.movement = movement;
        this.combat = combat;
        this.laneMonsters = laneMonsters;
        this.ui = ui;
        this.homeLane = homeLane;
        this.market = market;
        this.scanner = scanner;
    }

    public boolean attack(Hero hero) {
        List<Monster> inRange = combat.getMonstersInRange(hero);
        if (inRange.isEmpty()) {
            System.out.println("No monsters in attack range!");
            return false;
        }

        Monster target = ui.pickMonster(inRange);
        if (target == null) return false;

        combat.heroAttack(hero, target);
        cleanupDeadMonsters();
        return true;
    }

    public boolean castSpell(Hero hero) {
        List<Spell> spells = ui.getSpells(hero.getInventory());
        if (spells.isEmpty()) {
            System.out.println("You have no spells.");
            return false;
        }

        Spell spell = ui.pickSpell(spells);
        if (spell == null) return false;

        List<Monster> inRange = combat.getMonstersInRange(hero);
        if (inRange.isEmpty()) {
            System.out.println("No monsters in range to cast on!");
            return false;
        }

        Monster target = ui.pickMonster(inRange);
        if (target == null) return false;

        boolean ok = combat.heroCastSpell(hero, spell, target);
        cleanupDeadMonsters();
        return ok;
    }

    public boolean usePotion(Hero hero) {
        List<Potion> potions = ui.getPotions(hero.getInventory());
        if (potions.isEmpty()) {
            System.out.println("You have no potions.");
            return false;
        }

        Potion p = ui.pickPotion(potions);
        if (p == null) return false;

        hero.usePotion(p);
        hero.getInventory().removeItem(p);

        System.out.println(hero.getName() + " used potion: " + p.getName());
        return true;
    }

    public boolean equip(Hero hero, ValorInput input) {
        System.out.println(
                "Equip menu:\n" +
                "  1) Weapon\n" +
                "  2) Armor\n" +
                "  0) Cancel\n"
        );

        String line = input.readLine("Choose: ");
        if (line == null) return false;

        line = line.trim();
        if ("0".equals(line)) return false;

        if ("1".equals(line)) {
            List<Weapon> weapons = ui.getWeapons(hero.getInventory());
            if (weapons.isEmpty()) {
                System.out.println("No weapons to equip.");
                return false;
            }
            Weapon w = ui.pickWeapon(weapons);
            if (w == null) return false;

            hero.equipWeapon(w);
            System.out.println("Equipped weapon: " + w.getName());
            return true;
        }

        if ("2".equals(line)) {
            List<Armor> armors = ui.getArmors(hero.getInventory());
            if (armors.isEmpty()) {
                System.out.println("No armors to equip.");
                return false;
            }
            Armor a = ui.pickArmor(armors);
            if (a == null) return false;

            hero.equipArmor(a);
            System.out.println("Equipped armor: " + a.getName());
            return true;
        }

        System.out.println("Invalid choice.");
        return false;
    }

    public boolean teleport(Hero hero) {
        int[] myPos = movement.findHero(hero);
        if (myPos == null) return false;

        int myLane = board.getLane(myPos[1]);

        List<Hero> others = getOtherAliveHeroesOnBoard(hero);
        if (others.isEmpty()) {
            System.out.println("No other heroes to teleport to.");
            return false;
        }

        Hero target = ui.pickHero(others);
        if (target == null) return false;

        int[] targetPos = movement.findHero(target);
        if (targetPos == null) return false;

        int targetLane = board.getLane(targetPos[1]);
        if (targetLane == myLane) {
            System.out.println("Teleport must be to a different lane. Choose a hero in another lane.");
            return false;
        }

        List<int[]> candidates = new ArrayList<int[]>();
        int r0 = targetPos[0];
        int c0 = targetPos[1];
        int[][] dirs = { {-1,0}, {1,0}, {0,-1}, {0,1} };

        for (int i = 0; i < dirs.length; i++) {
            int r = r0 + dirs[i][0];
            int c = c0 + dirs[i][1];
            if (!board.inBounds(r, c)) continue;
            if (board.getLane(c) == -1) continue;
            if (r < r0) continue;

            if (movement.canTeleportHeroTo(hero, r, c)) {
                candidates.add(new int[]{r, c});
            }
        }

        if (candidates.isEmpty()) {
            System.out.println("No valid teleport destination near that hero.");
            return false;
        }

        int[] chosen = ui.pickPosition(candidates);
        if (chosen == null) return false;

        movement.teleportHeroTo(hero, chosen[0], chosen[1]);
        System.out.println(hero.getName() + " teleported!");
        return true;
    }

    public boolean recall(Hero hero) {
        Integer lane = homeLane.get(hero);
        if (lane == null) {
            System.out.println("Recall failed: home lane unknown.");
            return false;
        }

        int[] cols = board.getNexusColumnsForLane(lane);
        int r = ValorBoard.ROWS - 1;

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

    public boolean removeObstacle(Hero hero, ValorInput input) {
        int[] pos = movement.findHero(hero);
        if (pos == null) return false;

        System.out.println(
                "Remove obstacle direction:\n" +
                "  W = north, A = west, S = south, D = east\n" +
                "  0 = cancel\n"
        );

        String line = input.readLine("Direction: ");
        if (line == null) return false;

        line = line.trim().toUpperCase();
        if ("0".equals(line) || line.isEmpty()) return false;

        int dr = 0, dc = 0;
        char d = line.charAt(0);
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

    public boolean move(Hero hero, ValorDirection dir) {
        boolean moved = movement.moveHero(hero, dir);
        if (!moved) System.out.println("Cannot move there!");
        return moved;
    }

    public void bindHomeLaneIfMissing(Hero hero) {
        if (hero == null) return;
        if (homeLane.containsKey(hero)) return;

        int[] pos = movement.findHero(hero);
        if (pos == null) return;

        int lane = board.getLane(pos[1]);
        if (lane != -1) homeLane.put(hero, lane);
    }

    /**
     * Market action:
     * - allowed only if hero is on HERO NEXUS row.
     * - opens ValorMarketController and counts as turn action.
     */
    public boolean openMarket(Hero hero) {
        if (hero == null) return false;

        int[] pos = movement.findHero(hero);
        if (pos == null) return false;

        if (!board.isHeroesNexus(pos[0], pos[1])) {
            System.out.println("You can only open the Market while on your Nexus.");
            return false;
        }

        if (market == null || scanner == null) {
            System.out.println("Market is not wired yet.");
            return false;
        }

        new legends.valor.game.ValorMarketController(market, scanner).openForHero(hero);
        return true; // market counts as your action
    }

    // ---------------- internals ----------------

    private void cleanupDeadMonsters() {
        laneMonsters.removeIf(m -> m == null || m.getHP() <= 0);
    }

    private List<Hero> getOtherAliveHeroesOnBoard(Hero self) {
        List<Hero> out = new ArrayList<Hero>();
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
}