package legends.valor.world;

import legends.world.Tile;
import legends.characters.Hero;
import legends.characters.Monster;

/**
 * A single tile on the Legends of Valor board.
 *
 * Responsibilities:
 *  - Hold a ValorCellType (terrain classification).
 *  - Track occupancy:
 *      * at most one Hero
 *      * at most one Monster
 *  - Report accessibility
 *  - Provide future extension points for tile effects (Buffs)
 *
 *  NOTE:
 *    This class intentionally stays lightweight.
 *    Any per-tile combat/bonus logic belongs in the game rules layer,
 *    but hooks may be added here if needed.
 */
public class ValorTile extends Tile {

    private final ValorCellType type;

    private Hero hero;         // at most one hero on this tile
    private Monster monster;   // at most one monster on this tile

    public ValorTile(ValorCellType type) {
        this.type = type;
    }

    // =========================================================
    //  BASIC PROPERTIES
    // =========================================================

    public ValorCellType getType() {
        return type;
    }

    @Override
    public boolean isAccessible() {
        return type.isAccessible();
    }

    @Override
    public String getSymbol() {
        return type.getSymbol();
    }

    // =========================================================
    //  OCCUPANCY
    // =========================================================

    public boolean hasHero() {
        return hero != null;
    }

    public boolean hasMonster() {
        return monster != null;
    }

    public Hero getHero() {
        return hero;
    }

    public Monster getMonster() {
        return monster;
    }

    // =========================================================
    //  HERO PLACEMENT
    // =========================================================

    public void placeHero(Hero h) {
        if (hero != null) {
            throw new IllegalStateException("Tile already contains a hero.");
        }
        this.hero = h;

        // Future extension point:
        // applyHeroEnterBonus(h);
    }

    public void removeHero() {
        if (hero != null) {
            // Future extension point:
            // removeHeroBonus(hero);
        }
        this.hero = null;
    }

    // =========================================================
    //  MONSTER PLACEMENT
    // =========================================================

    public void placeMonster(Monster m) {
        if (monster != null) {
            throw new IllegalStateException("Tile already contains a monster.");
        }
        this.monster = m;

        // Monsters currently have no tile-dependent bonuses.
    }

    public void removeMonster() {
        this.monster = null;
    }

    // =========================================================
    //  ACCESSIBILITY FOR MOVEMENT
    // =========================================================
    //
    // NOTE:
    //   Heroes and monsters CAN share a tile.
    //   The game logic decides combat when that occurs.
    //

    public boolean isEmptyForHero() {
        // tile cannot be INACCESSIBLE and cannot have another hero
        return isAccessible() && !hasHero();
    }

    public boolean isEmptyForMonster() {
        // tile cannot be INACCESSIBLE and cannot have another monster
        return isAccessible() && !hasMonster();
    }

    // =========================================================
    //  OPTIONAL EXTENSION HOOKS (not enabled yet)
    // =========================================================
    //
    // These can be activated once Hero stats (DEX/AGI/STR) are ready:
    //
    // private void applyHeroEnterBonus(Hero h) {
    //     switch (type) {
    //         case BUSH:   h.tempBoostDex(); break;
    //         case CAVE:   h.tempBoostAgi(); break;
    //         case KOULOU: h.tempBoostStr(); break;
    //         default: break;
    //     }
    // }
    //
    // private void removeHeroBonus(Hero h) {
    //     h.clearTileBonuses();
    // }
    //
}
