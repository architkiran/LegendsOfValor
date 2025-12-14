package legends.valor.world.terrain;

import legends.characters.Entity;

public interface Terrain {
    boolean isAccessible();
    void onEnter(Entity entity);
    void onExit(Entity entity);
    char getSymbol();
}