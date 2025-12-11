package legends.valor.combat;

import legends.valor.world.ValorBoard;
import legends.valor.world.ValorTile;
import legends.characters.Hero;
import legends.characters.Monster;

import java.util.ArrayList;
import java.util.List;

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

    public ValorCombat(ValorBoard board) {
        this.board = board;
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
        //   raw = hero.getAttackDamage()
        //   dmg = round(raw)
        double raw = hero.getAttackDamage();
        int dmg = (int) Math.round(raw);

        monster.takeDamage(dmg);
        System.out.println(hero.getName() + " hits " + monster.getName()
                + " for " + dmg + " damage!");

        if (monster.getHP() <= 0) {
            System.out.println(monster.getName() + " has been slain!");
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
        //   base = monster.getDamage()
        //   dmg  = round(base * 0.3)
        double base = monster.getDamage();
        int dmg = (int) Math.round(base * 0.3);

        hero.takeDamage(dmg);
        System.out.println(monster.getName() + " hits " + hero.getName()
                + " for " + dmg + " damage!");

        if (hero.getHP() <= 0) {
            System.out.println(hero.getName() + " has fallen!");
            removeHeroFromBoard(hero);
            // (respawn logic will be handled elsewhere according to PDF rules)
            return true;
        }

        return false;
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
}