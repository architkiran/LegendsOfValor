package legends.valor.world.terrain;

import legends.characters.Entity;
import legends.characters.Hero;

public class CaveTerrain implements Terrain {

    private static final double BONUS = 0.10;

    @Override
    public boolean isAccessible() {
        return true;
    }

    @Override
    public void onEnter(Entity entity) {
        if (entity instanceof Hero) {
            Hero hero = (Hero) entity;
            double before = hero.getAgility();
            double after  = before * (1.0 + BONUS);
            double gained = after - before;

            hero.setAgility(after);

            System.out.printf(
                "[Terrain Bonus] %s entered Cave: Agility +%.2f (%.2f → %.2f)%n",
                hero.getName(), gained, before, after);
        }
    }

    @Override
    public void onExit(Entity entity) {
        if (entity instanceof Hero) {
            Hero hero = (Hero) entity;

            double before = hero.getAgility();
            double after  = before / (1.0 + BONUS);

            hero.setAgility(after);

            System.out.printf(
                "[Terrain Bonus] %s left Cave: Agility reverted (%.2f → %.2f)%n",
                hero.getName(), before, after);
        }
    }

    @Override
    public char getSymbol() {
        return 'C';
    }
}