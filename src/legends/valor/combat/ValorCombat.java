package legends.valor.combat;

import legends.valor.world.ValorBoard;
import legends.valor.world.ValorTile;
import legends.characters.Hero;
import legends.characters.Monster;
import legends.stats.GameStats;
import legends.stats.HeroStats;

import java.util.ArrayList;
import java.util.List;

import legends.items.Spell;

/**
 * Pure combat helper for Legends of Valor.
 *
 * Responsibilities:
 *  - Find which monsters are in attack range of a hero
 *  - Find which heroes are in attack range of a monster
 *  - Apply basic attack damage using the SAME formulas as Monsters & Heroes:
 *
 *    Hero → Monster:
 *      - Monster may dodge with getDodgeChance()
 *      - Damage = round( hero.getAttackDamage() )
 *
 *    Monster → Hero:
 *      - Hero may dodge with getDodgeChance()
 *      - Damage = round( monster.getDamage() * 0.3 )
 *
 *  - Remove dead monsters/heroes from their tiles
 *
 * NOTE: Gold / XP rewards and respawn are handled by the game layer, not here.
 */
public class ValorCombat {

    private final ValorBoard board;
    private final GameStats gameStats;

    public ValorCombat(ValorBoard board, GameStats gameStats) {
        this.board = board;
        this.gameStats = gameStats;
    }

    // =========================================================
    //  RANGE QUERIES
    //  Attack range = current tile + N/S/E/W neighbors
    // =========================================================

    private static final int[][] NEIGHBOR_OFFSETS = {
            { 0,  0},   // same tile
            {-1,  0},   // north
            { 1,  0},   // south
            { 0, -1},   // west
            { 0,  1}    // east
    };

    /** All monsters that a given hero can attack this turn, based on range. */
    public List<Monster> getMonstersInRange(Hero hero) {
        List<Monster> result = new ArrayList<>();

        int[] pos = findHero(hero);
        if (pos == null) return result;

        int r0 = pos[0];
        int c0 = pos[1];

        for (int[] d : NEIGHBOR_OFFSETS) {
            int r = r0 + d[0];
            int c = c0 + d[1];
            if (!board.inBounds(r, c)) continue;

            ValorTile tile = board.getTile(r, c);
            Monster m = tile.getMonster();
            if (m != null && m.getHP() > 0 && !result.contains(m)) {
                result.add(m);
            }
        }

        return result;
    }

    /** All heroes that a given monster can attack this turn, based on range. */
    public List<Hero> getHeroesInRange(Monster monster) {
        List<Hero> result = new ArrayList<>();

        int[] pos = findMonster(monster);
        if (pos == null) return result;

        int r0 = pos[0];
        int c0 = pos[1];

        for (int[] d : NEIGHBOR_OFFSETS) {
            int r = r0 + d[0];
            int c = c0 + d[1];
            if (!board.inBounds(r, c)) continue;

            ValorTile tile = board.getTile(r, c);
            Hero h = tile.getHero();
            if (h != null && h.getHP() > 0 && !result.contains(h)) {
                result.add(h);
            }
        }

        return result;
    }

    // =========================================================
    //  ATTACK RESOLUTION
    // =========================================================

    /** @return true if the monster DIES as a result, false otherwise. */
    public boolean heroAttack(Hero hero, Monster monster) {
        if (monster.getHP() <= 0) {
            System.out.println(monster.getName() + " is already defeated.");
            return false;
        }

        if (Math.random() < monster.getDodgeChance()) {
            System.out.println(monster.getName() + " dodged the attack!");
            return false;
        }

        int dmg = (int) Math.round(hero.getAttackDamage());

        HeroStats hs = safeHeroStats(hero);
        if (hs != null) hs.addDamageDealt(dmg);

        monster.takeDamage(dmg);
        System.out.println(hero.getName() + " hits " + monster.getName()
                + " for " + dmg + " damage!");

        if (monster.getHP() <= 0) {
            System.out.println(monster.getName() + " has been slain!");
            if (hs != null) hs.addKill();
            removeMonsterFromBoard(monster);
            return true;
        }

        return false;
    }

    /** @return true if the hero DIES as a result, false otherwise. */
    public boolean monsterAttack(Monster monster, Hero hero) {
        if (hero.getHP() <= 0) return false;

        if (Math.random() < hero.getDodgeChance()) {
            System.out.println(hero.getName() + " dodged " + monster.getName() + "'s attack!");
            return false;
        }

        int dmg = (int) Math.round(monster.getDamage() * 0.3);

        HeroStats hs = safeHeroStats(hero);
        if (hs != null) hs.addDamageTaken(dmg);

        hero.takeDamage(dmg);
        System.out.println(monster.getName() + " hits " + hero.getName()
                + " for " + dmg + " damage!");

        if (hero.getHP() <= 0) {
            System.out.println(hero.getName() + " has fallen!");
            if (hs != null) hs.addFaint();
            removeHeroFromBoard(hero);
            return true;
        }

        return false;
    }

    // =========================================================
    //  SPELL CASTING (Valor)
    // =========================================================

    /**
     * Hero casts a spell on a target monster (must be in range).
     *
     * @return true if a valid spell action happened, false otherwise.
     */
    public boolean heroCastSpell(Hero hero, Spell spell, Monster target) {
        if (hero == null || spell == null || target == null) return false;
        if (hero.getHP() <= 0 || target.getHP() <= 0) return false;

        List<Monster> inRange = getMonstersInRange(hero);
        if (!inRange.contains(target)) {
            System.out.println("Target is not in range.");
            return false;
        }

        if (!hero.canCast(spell)) {
            System.out.println("Not enough mana!");
            return false;
        }

        hero.spendMana(spell.getManaCost());

        if (Math.random() < target.getDodgeChance()) {
            System.out.println(target.getName() + " dodged the spell!");
            return true; // action consumed
        }

        double base = spell.getDamage();
        double raw = base + (hero.getDexterity() / 10000.0) * base;
        int dmg = (int) Math.round(raw);

        applySpellEffect(spell, target);

        HeroStats hs = safeHeroStats(hero);
        if (hs != null) hs.addDamageDealt(dmg);

        target.takeDamage(dmg);
        System.out.println(hero.getName() + " casts " + spell.getName()
                + " on " + target.getName() + " for " + dmg + " damage!");

        if (target.getHP() <= 0) {
            System.out.println(target.getName() + " has been slain!");
            if (hs != null) hs.addKill();
            removeMonsterFromBoard(target);
        }

        return true;
    }

    private void applySpellEffect(Spell spell, Monster target) {
        if (spell == null || target == null) return;

        double val;
        switch (spell.getType()) {
            case FIRE -> {
                val = target.getDefense();
                target.setDefense(Math.max(0, val - val * 0.1));
                System.out.println("🔥 " + target.getName() + "'s defense reduced!");
            }
            case ICE -> {
                val = target.getDamage();
                target.setDamage(Math.max(0, val - val * 0.1));
                System.out.println("❄️ " + target.getName() + "'s damage reduced!");
            }
            case LIGHTNING -> {
                val = target.getDodgeChance();
                target.setDodgeChance(Math.max(0, val - val * 0.1));
                System.out.println("⚡ " + target.getName() + "'s dodge reduced!");
            }
        }
    }

    // =========================================================
    //  STATS HELPERS
    // =========================================================

    private HeroStats safeHeroStats(Hero hero) {
        if (gameStats == null || hero == null) return null;
        try {
            return gameStats.statsFor(hero);
        } catch (Exception e) {
            return null;
        }
    }

    // =========================================================
    //  BOARD LOOKUPS / CLEAN-UP
    // =========================================================

    private int[] findHero(Hero hero) {
        for (int r = 0; r < ValorBoard.ROWS; r++) {
            for (int c = 0; c < ValorBoard.COLS; c++) {
                ValorTile tile = board.getTile(r, c);
                if (tile.getHero() == hero) return new int[]{r, c};
            }
        }
        return null;
    }

    private int[] findMonster(Monster monster) {
        for (int r = 0; r < ValorBoard.ROWS; r++) {
            for (int c = 0; c < ValorBoard.COLS; c++) {
                ValorTile tile = board.getTile(r, c);
                if (tile.getMonster() == monster) return new int[]{r, c};
            }
        }
        return null;
    }

    private void removeHeroFromBoard(Hero hero) {
        int[] pos = findHero(hero);
        if (pos == null) return;
        board.getTile(pos[0], pos[1]).removeHero();
    }

    private void removeMonsterFromBoard(Monster monster) {
        int[] pos = findMonster(monster);
        if (pos == null) return;
        board.getTile(pos[0], pos[1]).removeMonster();
    }
}