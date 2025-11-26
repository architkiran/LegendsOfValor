/**
 * BattleState handles all turn-based combat logic in the game.
 *
 * This class is responsible for:
 * - Rendering the battle screen (heroes, monsters, actions)
 * - Running sequential hero turns
 * - Running monster turns
 * - Handling attacks, spells, potions, equipment changes
 * - Checking for end-of-battle conditions
 * - Awarding rewards and regenerating stats between rounds
 *
 * It represents one full battle instance and is swapped into the game state
 * when combat begins.
 */

package legends.game;

import legends.characters.*;
import legends.items.*;
import legends.ui.BarUtils;

import java.util.*;

public class BattleState implements GameState {

    private LegendsGame game;
    private Party party;
    private List<Monster> monsters;
    private Scanner in = new Scanner(System.in);

    // Turn order of heroes for the current round
    private List<Hero> turnOrder;   
    private int turnIndex;           // index of current acting hero

    public BattleState(LegendsGame game, List<Monster> monsters) {
        this.game = game;
        this.party = game.getParty();
        this.monsters = monsters;

        this.turnOrder = new ArrayList<>();
        startNewRound();   // begin round 1 immediately
    }

    // ROUND MANAGEMENT

    // Establishes turn order for heroes at the start of a new round
    private void startNewRound() {
        turnOrder = party.getAliveHeroes();
        turnIndex = 0;

        if (!turnOrder.isEmpty()) {
            System.out.println("\n\u001B[94m--- NEW ROUND ---\u001B[0m");
        }
    }

    // Returns which hero is currently acting this turn
    private Hero currentHero() {
        if (turnOrder == null || turnOrder.isEmpty()) return null;
        if (turnIndex < 0 || turnIndex >= turnOrder.size()) return null;
        return turnOrder.get(turnIndex);
    }

    // True if every monster is dead
    private boolean areAllMonstersDead() {
        if (monsters == null || monsters.isEmpty()) return true;
        return monsters.stream().allMatch(m -> m.getHP() <= 0);
    }

    // RENDERING

    @Override
    public void render() {

        // Draws the full battle UI: hero stats, monster stats, and menu actions
        System.out.println("\n████████████████ 🛡️  BATTLE  🗡️ ████████████████\n");

        final int BOX_WIDTH = 52;  // width used for the ASCII UI

        // HEROES BOX
        System.out.println("╔" + repeat("═", BOX_WIDTH) + "╗");
        System.out.println("║" + center("HEROES", BOX_WIDTH) + "║");
        System.out.println("╠" + repeat("═", BOX_WIDTH) + "╣");

        for (Hero h : party.getHeroes()) {

            double maxHP = h.getLevel() * 100;
            double maxMP = h.getLevel() * 50;

            // health bar & mana bar visual display
            String hpBar = "HP: " + BarUtils.makeBar(h.getHP(), maxHP, 12)
                    + " (" + (int) h.getHP() + "/" + (int) maxHP + ")";

            String mpBar = "MP: " + BarUtils.makeBar(h.getMP(), maxMP, 12)
                    + " (" + (int) h.getMP() + "/" + (int) maxMP + ")";

            System.out.println("║ " + pad(h.getName(), BOX_WIDTH - 2) + " ║");
            System.out.println("║ " + pad(hpBar, BOX_WIDTH + 7) + " ║");
            System.out.println("║" + repeat(" ", BOX_WIDTH) + "║");
            System.out.println("║ " + pad(mpBar, BOX_WIDTH + 7) + " ║");
            System.out.println("║" + repeat(" ", BOX_WIDTH) + "║");
        }

        System.out.println("╚" + repeat("═", BOX_WIDTH) + "╝\n");

        // MONSTERS BOX
        System.out.println("╔" + repeat("═", BOX_WIDTH) + "╗");
        System.out.println("║" + center("MONSTERS", BOX_WIDTH) + "║");
        System.out.println("╠" + repeat("═", BOX_WIDTH) + "╣");

        for (Monster m : monsters) {

            double maxHP = m.getLevel() * 100;

            // Monster HP bar
            String hpBar = "HP: " + BarUtils.makeBar(m.getHP(), maxHP, 12)
                    + " (" + (int) m.getHP() + "/" + (int) maxHP + ")";

            System.out.println("║ " + pad(m.getName(), BOX_WIDTH - 2) + " ║");
            System.out.println("║ " + pad(hpBar, BOX_WIDTH + 7) + " ║");
            System.out.println("║" + repeat(" ", BOX_WIDTH) + "║");
        }

        System.out.println("╚" + repeat("═", BOX_WIDTH) + "╝\n");

        // ACTION MENU
        Hero acting = currentHero();

        System.out.println("╔" + repeat("═", BOX_WIDTH) + "╗");
        System.out.println("║" + center("ACTIONS", BOX_WIDTH) + "║");
        System.out.println("╠" + repeat("═", BOX_WIDTH) + "╣");

        // shows whose turn it currently is
        String turnLine = (acting != null)
                ? "Turn: " + acting.getName()
                : "Turn: (no hero)";
        System.out.println("║ " + pad(turnLine, BOX_WIDTH - 2) + " ║");

        System.out.println("╠" + repeat("─", BOX_WIDTH) + "╣");

        System.out.println("║ 1. Attack" + pad("", BOX_WIDTH - 10) + "║");
        System.out.println("║ 2. Cast Spell" + pad("", BOX_WIDTH - 14) + "║");
        System.out.println("║ 3. Use Potion" + pad("", BOX_WIDTH - 14) + "║");
        System.out.println("║ 4. Change Equipment" + pad("", BOX_WIDTH - 20) + "║");
        System.out.println("║ 5. Show Party Stats" + pad("", BOX_WIDTH - 20) + "║");
        System.out.println("║ Q. Flee" + pad("", BOX_WIDTH - 8) + "║");

        System.out.println("╚" + repeat("═", BOX_WIDTH) + "╝");

        // prompt user input
        if (acting != null)
            System.out.print("\nChoose action for " + acting.getName() + ": ");
        else
            System.out.print("\nChoose action: ");
    }

    // Helper rendering functions 

    private String pad(String text, int width) {
        if (text.length() >= width) return text;
        return text + " ".repeat(width - text.length());
    }

    private String repeat(String s, int count) {
        return s.repeat(count);
    }

    private String center(String text, int width) {
        int pad = (width - text.length()) / 2;
        return " ".repeat(pad) + text + " ".repeat(width - pad - text.length());
    }

    // INPUT HANDLING

    @Override
    public void handleInput(String input) {

        if (input == null) return;
        input = input.trim().toUpperCase();

        Hero actingHero = currentHero();
        if (actingHero == null) {
            System.out.println("No available hero to act.");
            return;
        }

        boolean turnConsumed = false; // only advance if action actually happened

        switch (input) {
            case "1":
                turnConsumed = doAttack(actingHero);
                break;
            case "2":
                turnConsumed = doCastSpell(actingHero);
                break;
            case "3":
                turnConsumed = doUsePotion(actingHero);
                break;
            case "4":
                turnConsumed = doChangeEquipment(actingHero);
                break;
            case "5":
                // showing stats doesn't count as using your turn
                party.printStats();
                return;
            case "Q":
                System.out.println("You fled the battle!");
                game.setState(new ExplorationState(game.getParty(), game.getMap(), game));
                return;
            default:
                System.out.println("Invalid input!");
                return;
        }

        if (!turnConsumed) return; // cancelled or invalid action → retry

        // After hero action, check immediate win/lose
        if (areAllMonstersDead() || party.allDead()) {
            checkBattleEnd();
            return;
        }

        // Next hero's turn
        if (turnIndex < turnOrder.size() - 1) {
            turnIndex++;
            return;
        }

        // All heroes acted → monsters' turn
        monsterTurn();
        checkBattleEnd();
    }

    @Override
    public void update(LegendsGame game) {
        // No continuous update — everything driven by player commands
    }

    @Override
    public boolean isFinished() { return false; }

    // PLAYER ACTIONS

    // Handles normal physical attacks
    private boolean doAttack(Hero hero) {

        Monster monster = chooseMonster();
        if (monster == null) return false;
        if (monster.getHP() <= 0) {
            System.out.println("That monster is already down.");
            return false;
        }

        // dodge check
        if (Math.random() < monster.getDodgeChance()) {
            System.out.println(monster.getName() + " dodged the attack!");
            return true;
        }

        // calculate hero physical damage
        double raw = hero.getAttackDamage();
        int dmg = (int)Math.round(raw);

        monster.takeDamage(dmg);

        System.out.println("\u001B[92m" + hero.getName() + " hit "
                + monster.getName() + " for " + dmg + " damage!\u001B[0m");

        return true;
    }

    // SPELL CASTING

    private boolean doCastSpell(Hero hero) {

        Spell spell = chooseSpell(hero);
        if (spell == null) return false;

        if (!hero.canCast(spell)) {
            System.out.println("\u001B[91mNot enough mana!\u001B[0m");
            return false;
        }

        Monster monster = chooseMonster();
        if (monster == null) return false;

        hero.spendMana(spell.getManaCost());

        // monster dodge
        if (Math.random() < monster.getDodgeChance()) {
            System.out.println(monster.getName() + " dodged the spell!");
            return true;
        }

        // compute spell damage scaling with dexterity
        double base = spell.getDamage();
        double raw = base + (hero.getDexterity() / 10000.0) * base;
        int dmg = (int)Math.round(raw);

        // apply spell-specific debuff
        applySpellEffect(spell, monster);

        monster.takeDamage(dmg);

        System.out.println(hero.getName() + " casts " + spell.getName()
                + " on " + monster.getName()
                + " for " + dmg + " damage!");

        return true;
    }

    // POTIONS

    private boolean doUsePotion(Hero hero) {

        // gather potion items from inventory
        List<Potion> potions = new ArrayList<>();
        for (Item it : hero.getInventory().getItems()) {
            if (it instanceof Potion p) potions.add(p);
        }

        if (potions.isEmpty()) {
            System.out.println("No potions available!");
            return false;
        }

        // show list
        System.out.println("\nAvailable Potions:");
        for (int i = 0; i < potions.size(); i++) {
            Potion p = potions.get(i);
            System.out.println((i + 1) + ". " + p.getName()
                    + " | +" + p.getEffectAmount()
                    + " (" + p.getAttributes() + ")");
        }

        System.out.print("Choose potion: ");
        int idx = readIndex(potions.size());
        if (idx == -1) return false;

        Potion p = potions.get(idx);

        // apply potion effect then remove from inventory
        hero.usePotion(p);
        hero.getInventory().removeItem(p);

        System.out.println("\u001B[92m" + hero.getName()
                + " used " + p.getName() + "!\u001B[0m");

        return true;
    }

    // EQUIPMENT CHANGES

    private boolean doChangeEquipment(Hero hero) {
        System.out.println("\n1. Equip Weapon");
        System.out.println("2. Equip Armor");
        System.out.println("B. Back");
        System.out.print("Choose: ");

        String choice = in.nextLine().trim().toUpperCase();

        switch (choice) {
            case "1":
                equipWeapon(hero);
                return true;
            case "2":
                equipArmor(hero);
                return true;
            case "B":
                return false;   // does not use turn
            default:
                System.out.println("Invalid choice.");
                return false;
        }
    }

    private void equipWeapon(Hero hero) {
        // collect weapons
        List<Weapon> weapons = new ArrayList<>();
        for (Item it : hero.getInventory().getItems())
            if (it instanceof Weapon w) weapons.add(w);

        if (weapons.isEmpty()) {
            System.out.println("No weapons!");
            return;
        }

        // display weapon list
        System.out.println("\nAvailable Weapons:");
        for (int i = 0; i < weapons.size(); i++) {
            Weapon w = weapons.get(i);
            System.out.println((i + 1) + ". " + w.getName()
                    + " | DMG: " + w.getDamage()
                    + " | Hands: " + w.getHands());
        }

        System.out.print("Choose: ");
        int idx = readIndex(weapons.size());
        if (idx == -1) return;

        hero.equipWeapon(weapons.get(idx));
        System.out.println(hero.getName() + " equipped " + weapons.get(idx).getName());
    }

    private void equipArmor(Hero hero) {
        // collect armor
        List<Armor> armors = new ArrayList<>();
        for (Item it : hero.getInventory().getItems())
            if (it instanceof Armor a) armors.add(a);

        if (armors.isEmpty()) {
            System.out.println("No armor!");
            return;
        }

        System.out.println("\nAvailable Armor:");
        for (int i = 0; i < armors.size(); i++) {
            Armor a = armors.get(i);
            System.out.println((i + 1) + ". " + a.getName()
                    + " | Reduce: " + a.getReduction());
        }

        System.out.print("Choose: ");
        int idx = readIndex(armors.size());
        if (idx == -1) return;

        hero.equipArmor(armors.get(idx));
        System.out.println(hero.getName() + " equipped " + armors.get(idx).getName());
    }

    // MONSTER TURN

    private void monsterTurn() {
        System.out.println("\n\u001B[91m━━━━━━━━ MONSTERS’ TURN ━━━━━━━━\u001B[0m");

        for (Monster m : monsters) {
            if (m.getHP() <= 0) continue;

            // choose a hero
            Hero target = party.getRandomAliveHero();
            if (target == null) return;

            // hero dodge
            if (Math.random() < target.getDodgeChance()) {
                System.out.println(target.getName() + " dodged " + m.getName() + "'s attack!");
                continue;
            }

            // monster damage uses a fixed conversion percentage
            double base = m.getDamage();
            int dmg = (int)Math.round(base * 0.3);

            target.takeDamage(dmg);

            System.out.println(m.getName() + " hit "
                    + target.getName() + " for " + dmg + " damage!");
        }
    }

    // END OF BATTLE

    private void checkBattleEnd() {
        boolean allMonstersDead = areAllMonstersDead();
        boolean allHeroesDead   = party.allDead();

        if (allMonstersDead) {
            System.out.println("\n\u001B[92mYOU WON THE BATTLE!\u001B[0m");
            rewardHeroes();
            game.setState(new ExplorationState(game.getParty(), game.getMap(), game));
            return;
        }

        if (allHeroesDead) {
            System.out.println("\n\u001B[91mYour party has fallen...\u001B[0m");
            System.exit(0);
        }

        // battle continues → regenerate and start next round
        regenerateBetweenRounds();
        startNewRound();
    }

    // HP/MP regeneration between rounds
    private void regenerateBetweenRounds() {
        System.out.println("\nSome of your heroes recover a bit of HP and mana...");

        for (Hero h : party.getHeroes()) {
            if (h.getHP() <= 0) continue;  // no regen for fainted heroes

            double maxHP = h.getLevel() * 100;
            double maxMP = h.getLevel() * 50;

            double newHP = Math.min(maxHP, h.getHP() * 1.1);
            double newMP = Math.min(maxMP, h.getMP() * 1.1);

            h.setHP(newHP);
            h.setMP(newMP);
        }
    }

    // award XP/gold and revive fallen heroes
    private void rewardHeroes() {

        int monsterLevel = 1;
        for (Monster m : monsters) {
            if (m.getLevel() > 0) {
                monsterLevel = m.getLevel();
                break;
            }
        }

        int numMonsters = party.getHeroes().size();
        int xpGain   = numMonsters * 2;
        int goldGain = monsterLevel * 100;

        System.out.println("\n--- Rewards ---");

        // reward alive heroes
        for (Hero h : party.getHeroes()) {
            if (h.getHP() > 0) {
                h.addExperience(xpGain);
                h.earnGold(goldGain);
                System.out.println(h.getName() + ": +" + xpGain + " XP, +" + goldGain + " Gold");
            }
        }

        // revive fainted heroes
        for (Hero h : party.getHeroes()) {
            if (h.getHP() <= 0) {
                double maxHP = h.getLevel() * 100;
                double maxMP = h.getLevel() * 50;

                h.setHP(maxHP * 0.5);
                h.setMP(maxMP * 0.5);

                System.out.println(h.getName()
                        + " is revived with half HP and MP (no XP or gold).");
            }
        }
    }

    // INPUT HELPERS

    private Monster chooseMonster() {
        List<Monster> alive = monsters.stream().filter(m -> m.getHP() > 0).toList();
        if (alive.isEmpty()) return null;

        System.out.println("\nChoose monster:");
        for (int i = 0; i < alive.size(); i++) {
            Monster m = alive.get(i);
            System.out.println((i + 1) + ". " + m.getName()
                    + " (HP: " + (int)m.getHP() + ")");
        }

        System.out.print("Enter number: ");
        int idx = readIndex(alive.size());
        return idx == -1 ? null : alive.get(idx);
    }

    private Spell chooseSpell(Hero hero) {
        List<Spell> spells = new ArrayList<>();
        for (Item it : hero.getInventory().getItems())
            if (it instanceof Spell s) spells.add(s);

        if (spells.isEmpty()) {
            System.out.println("No spells!");
            return null;
        }

        System.out.println("\nChoose Spell:");
        for (int i = 0; i < spells.size(); i++) {
            Spell s = spells.get(i);
            System.out.println((i + 1) + ". " + s.getName()
                    + " | DMG: " + (int)s.getDamage()
                    + " | Mana: " + (int)s.getManaCost()
                    + " | Type: " + s.getType());
        }

        System.out.print("Enter number: ");
        int idx = readIndex(spells.size());
        return idx == -1 ? null : spells.get(idx);
    }

    private void applySpellEffect(Spell spell, Monster target) {
        double val;
        switch (spell.getType()) {
            case FIRE:
                val = target.getDefense();
                target.setDefense(Math.max(0, val - val * 0.1));
                System.out.println("🔥 " + target.getName() + "'s defense reduced!");
                break;

            case ICE:
                val = target.getDamage();
                target.setDamage(Math.max(0, val - val * 0.1));
                System.out.println("❄️ " + target.getName() + "'s damage reduced!");
                break;

            case LIGHTNING:
                val = target.getDodgeChance();
                target.setDodgeChance(Math.max(0, val - val * 0.1));
                System.out.println("⚡ " + target.getName() + "'s dodge reduced!");
                break;
        }
    }

    private int readIndex(int size) {
        try {
            int idx = Integer.parseInt(in.nextLine().trim()) - 1;
            if (idx < 0 || idx >= size) return -1;
            return idx;
        } catch (Exception e) {
            return -1;
        }
    }
}