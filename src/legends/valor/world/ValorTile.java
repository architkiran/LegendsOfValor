package legends.valor.world;

import legends.characters.Entity;
import legends.characters.Hero;
import legends.characters.Monster;
import legends.valor.world.terrain.Terrain;
import legends.valor.world.terrain.TerrainFactory;
import legends.world.Tile;

public class ValorTile extends Tile {

    private ValorCellType type;
    private Terrain terrain; // null if no terrain bonus

    private Hero hero;
    private Monster monster;

    public ValorTile(ValorCellType type) {
        setType(type); // keeps type + terrain in sync, handles null safely
    }

    public ValorCellType getType() {
        return type;
    }

    /**
     * Changes the tile type and refreshes its terrain behavior.
     * Null type is ignored (no-op) to keep callers safe.
     */
    public void setType(ValorCellType newType) {
        if (newType == null) return;
        this.type = newType;
        this.terrain = TerrainFactory.create(newType);
    }

    @Override
    public boolean isAccessible() {
        return type != null && type.isAccessible();
    }

    @Override
    public String getSymbol() {
        return (type == null) ? "?" : type.getSymbol();
    }

    // =========================================================
    // TERRAIN BONUS LIFECYCLE
    // =========================================================

    /** Call when an entity enters this tile. (Safe for null entity.) */
    public void onEnter(Entity entity) {
        if (terrain != null && entity != null) {
            terrain.onEnter(entity);
        }
    }

    /** Call when an entity leaves this tile. (Safe for null entity.) */
    public void onExit(Entity entity) {
        if (terrain != null && entity != null) {
            terrain.onExit(entity);
        }
    }

    // =========================================================
    // OCCUPANCY
    // =========================================================

    public boolean hasHero() { return hero != null; }
    public boolean hasMonster() { return monster != null; }

    public Hero getHero() { return hero; }
    public Monster getMonster() { return monster; }

    public void placeHero(Hero h) {
        if (h == null) return;
        if (hero != null) throw new IllegalStateException("Tile already contains a hero.");
        this.hero = h;
    }

    public void removeHero() { this.hero = null; }

    public void placeMonster(Monster m) {
        if (m == null) return;
        if (monster != null) throw new IllegalStateException("Tile already contains a monster.");
        this.monster = m;
    }

    public void removeMonster() { this.monster = null; }

    public boolean isEmptyForHero() {
        return isAccessible() && hero == null;
    }

    public boolean isEmptyForMonster() {
        return isAccessible() && monster == null;
    }
}