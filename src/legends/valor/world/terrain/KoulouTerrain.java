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
            double before = hero.getStrength();
            double after  = before * (1.0 + BONUS);
            double gained = after - before;

            hero.setStrength(after);

            System.out.printf(
                "[Terrain Bonus] %s entered Koulou: Strength +%.2f (%.2f → %.2f)%n",
                hero.getName(), gained, before, after);
        }
    }

    @Override
    public void onExit(Entity entity) {
        if (entity instanceof Hero) {
            Hero hero = (Hero) entity;
            double before = hero.getStrength();
            double after  = before / (1.0 + BONUS);
            
            hero.setStrength(after);

            System.out.printf(
                "[Terrain Bonus] %s left Koulou: Strength reverted (%.2f → %.2f)%n",
                hero.getName(), before, after);
        }
    }

    @Override
    public char getSymbol() {
        return 'K';
    }
}