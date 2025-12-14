package legends.valor.world;

import legends.world.Tile;
import legends.characters.Hero;
import legends.characters.Monster;
import legends.characters.Entity;
import legends.valor.world.terrain.Terrain;
import legends.valor.world.terrain.TerrainFactory;

public class ValorTile extends Tile {

    // ❗was: private final ValorCellType type;
    private ValorCellType type;

    // ✅ NEW: terrain behavior (null if no bonus)
    private Terrain terrain;

    private Hero hero;
    private Monster monster;

    public ValorTile(ValorCellType type) {
        this.type = type;

        // ✅ NEW: initialize terrain based on type
        this.terrain = TerrainFactory.create(type);
    }

    public ValorCellType getType() {
        return type;
    }

    public void setType(ValorCellType newType) {
        if (newType == null) return;
        this.type = newType;

        // ✅ NEW: keep terrain in sync if type changes
        this.terrain = TerrainFactory.create(newType);
    }

    @Override
    public boolean isAccessible() {
        return type.isAccessible();
    }

    @Override
    public String getSymbol() {
        return type.getSymbol();
    }

    /* ===============================
       TERRAIN BONUS LIFECYCLE
       =============================== */

    // ✅ NEW: call when an entity enters this tile
    public void onEnter(Entity entity) {
        if (terrain != null) {
            terrain.onEnter(entity);
        }
    }

    // ✅ NEW: call when an entity leaves this tile
    public void onExit(Entity entity) {
        if (terrain != null) {
            terrain.onExit(entity);
        }
    }

    public boolean hasHero() { return hero != null; }
    public boolean hasMonster() { return monster != null; }
    public Hero getHero() { return hero; }
    public Monster getMonster() { return monster; }

    public void placeHero(Hero h) {
        if (hero != null) throw new IllegalStateException("Tile already contains a hero.");
        this.hero = h;
    }

    public void removeHero() { this.hero = null; }

    public void placeMonster(Monster m) {
        if (monster != null) throw new IllegalStateException("Tile already contains a monster.");
        this.monster = m;
    }

    public void removeMonster() { this.monster = null; }

    public boolean isEmptyForHero() {
        return isAccessible() && !hasHero();
    }

    public boolean isEmptyForMonster() {
        return isAccessible() && !hasMonster();
    }
}