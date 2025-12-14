package legends.valor.world.terrain;

import legends.characters.Entity;
import legends.characters.Hero;

public class KoulouTerrain implements Terrain {

    private static final double BONUS = 0.10;

    @Override
    public boolean isAccessible() {
        return true;
    }

    @Override
    public void onEnter(Entity entity) {
        if (entity instanceof Hero) {
            Hero hero = (Hero) entity;
            double currentStrength = hero.getStrength();
            hero.setStrength(currentStrength * (1.0 + BONUS));
        }
    }

    @Override
    public void onExit(Entity entity) {
        if (entity instanceof Hero) {
            Hero hero = (Hero) entity;
            double currentStrength = hero.getStrength();
            hero.setStrength(currentStrength / (1.0 + BONUS));
        }
    }

    @Override
    public char getSymbol() {
        return 'K';
    }
}