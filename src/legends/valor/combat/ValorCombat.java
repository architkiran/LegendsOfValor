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
import legends.items.SpellType;

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

    /**
     * All monsters that a given hero can attack this turn, based on range.
     */
    public List<Monster> getMonstersInRange(Hero hero) {
        List<Monster> result = new ArrayList<>();

        int[] pos = findHero(hero);
        if (pos == null) return result;

        int r0 = pos[0];
        int c0 = pos[1];

        for (int i = 0; i < NEIGHBOR_OFFSETS.length; i++) {
            int[] d = NEIGHBOR_OFFSETS[i];
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

    /**
     * All heroes that a given monster can attack this turn, based on range.
     */
    public List<Hero> getHeroesInRange(Monster monster) {
        List<Hero> result = new ArrayList<>();

        int[] pos = findMonster(monster);
        if (pos == null) return result;

        int r0 = pos[0];
        int c0 = pos[1];

        for (int i = 0; i < NEIGHBOR_OFFSETS.length; i++) {
            int[] d = NEIGHBOR_OFFSETS[i];
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

    /**
     * Hero performs a basic physical attack on a monster.
     *
     * @return true if the monster DIES as a result, false otherwise.
     */
    public boolean heroAttack(Hero hero, Monster monster) {
        if (monster.getHP() <= 0) {
            System.out.println(monster.getName() + " is already defeated.");
            return false;
        }

        // Dodge check (same as Monsters & Heroes)
        if (Math.random() < monster.getDodgeChance()) {
            System.out.println(monster.getName() + " dodged the attack!");
            return false;
        }

        // Damage formula copied from your BattleState:
        double raw = hero.getAttackDamage();
        int dmg = (int) Math.round(raw);

        // ✅ Stats: damage dealt by hero
        HeroStats hs = safeHeroStats(hero);
        if (hs != null) {
            hs.addDamageDealt(dmg);
        }

        monster.takeDamage(dmg);
        System.out.println(hero.getName() + " hits " + monster.getName()
                + " for " + dmg + " damage!");

        if (monster.getHP() <= 0) {
            System.out.println(monster.getName() + " has been slain!");

            // ✅ Stats: hero kill
            if (hs != null) {
                hs.addKill();
            }

            removeMonsterFromBoard(monster);
            return true;
        }

        return false;
    }

    /**
     * Monster performs a basic attack on a hero.
     *
     * @return true if the hero DIES as a result, false otherwise.
     */
    public boolean monsterAttack(Monster monster, Hero hero) {
        if (hero.getHP() <= 0) {
            return false;
        }

        // Hero dodge chance (same as Monsters & Heroes battle)
        if (Math.random() < hero.getDodgeChance()) {
            System.out.println(hero.getName() + " dodged " + monster.getName() + "'s attack!");
            return false;
        }

        // Damage formula copied from your BattleState:
        double base = monster.getDamage();
        int dmg = (int) Math.round(base * 0.3);

        // ✅ Stats: damage taken by hero
        HeroStats hs = safeHeroStats(hero);
        if (hs != null) {
            hs.addDamageTaken(dmg);
        }

        hero.takeDamage(dmg);
        System.out.println(monster.getName() + " hits " + hero.getName()
                + " for " + dmg + " damage!");

        if (hero.getHP() <= 0) {
            System.out.println(hero.getName() + " has fallen!");

            // ✅ Stats: hero fainted
            if (hs != null) {
                hs.addFaint();
            }

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
     * Rules:
     *  - Must have enough mana
     *  - Target must be in attack range (same tile or N/S/E/W)
     *  - Target may dodge using getDodgeChance()
     *  - Damage formula matches your BattleState:
     *      raw = base + (heroDexterity / 10000) * base
     *  - Apply 10% debuff based on spell type (also matches BattleState)
     *
     * @return true if a valid spell action happened, false otherwise.
     */
    public boolean heroCastSpell(Hero hero, Spell spell, Monster target) {
        if (hero == null || spell == null || target == null) return false;
        if (hero.getHP() <= 0 || target.getHP() <= 0) return false;

        // must be in range
        List<Monster> inRange = getMonstersInRange(hero);
        if (!inRange.contains(target)) {
            System.out.println("Target is not in range.");
            return false;
        }

        // mana check (Hero already has mp field; BattleState uses hero.canCast + spendMana)
        if (!hero.canCast(spell)) {                  // if you haven't added canCast yet, see note below
            System.out.println("Not enough mana!");
            return false;
        }

        hero.spendMana(spell.getManaCost());

        // monster dodge
        if (Math.random() < target.getDodgeChance()) {
            System.out.println(target.getName() + " dodged the spell!");
            return true; // action consumed
        }

        // compute damage (matches BattleState formula)
        double base = spell.getDamage();
        double raw = base + (hero.getDexterity() / 10000.0) * base;
        int dmg = (int) Math.round(raw);

        // apply debuff (10%)
        applySpellEffect(spell, target);

        // stats: damage dealt
        HeroStats hs = safeHeroStats(hero);
        if (hs != null) {
            hs.addDamageDealt(dmg);
        }

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
                // Fire lowers defense by 10%
                val = target.getDefense();
                target.setDefense(Math.max(0, val - val * 0.1));
                // or: target.applyFireDebuff(val * 0.1);  (Monster提供了这个API :contentReference[oaicite:3]{index=3})
                System.out.println("🔥 " + target.getName() + "'s defense reduced!");
            }
            case ICE -> {
                // Ice lowers damage by 10%
                val = target.getDamage();
                target.setDamage(Math.max(0, val - val * 0.1));
                // or: target.applyIceDebuff(val * 0.1);
                System.out.println("❄️ " + target.getName() + "'s damage reduced!");
            }
            case LIGHTNING -> {
                // Lightning lowers dodge chance by 10%
                val = target.getDodgeChance();
                target.setDodgeChance(Math.max(0, val - val * 0.1));
                // or: target.applyLightningDebuff(val * 0.1);
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
                if (tile.getHero() == hero) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }

    private int[] findMonster(Monster monster) {
        for (int r = 0; r < ValorBoard.ROWS; r++) {
            for (int c = 0; c < ValorBoard.COLS; c++) {
                ValorTile tile = board.getTile(r, c);
                if (tile.getMonster() == monster) {
                    return new int[]{r, c};
                }
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

    private void applySpellDebuff(Monster m, SpellType type) {
        if (m == null || type == null) return;

        // If your Monster has different APIs, tell me its methods and I'll adapt.
        switch (type) {
            case FIRE -> {
                // reduce damage
                m.setDamage(m.getDamage() * 0.9);
                System.out.println(m.getName() + "'s damage is reduced!");
            }
            case ICE -> {
                // reduce defense
                m.setDefense(m.getDefense() * 0.9);
                System.out.println(m.getName() + "'s defense is reduced!");
            }
            case LIGHTNING -> {
                // reduce dodge chance
                m.setDodgeChance(m.getDodgeChance() * 0.9);
                System.out.println(m.getName() + "'s dodge chance is reduced!");
            }
        }
    }

}