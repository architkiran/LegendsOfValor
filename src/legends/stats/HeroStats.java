package legends.stats;

import legends.characters.Hero;

public class HeroStats {

    private final Hero hero;

    private int monstersKilled = 0;
    private int timesFainted   = 0;

    private double damageDealt = 0;
    private double damageTaken = 0;

    private int goldGained = 0;
    private int xpGained   = 0;

    public HeroStats(Hero hero) {
        this.hero = hero;
    }

    public Hero getHero() { return hero; }

    public String getHeroName() {
        return (hero == null) ? "Unknown" : hero.getName();
    }

    // If your Hero class doesn't have getLevel(), you can delete this method.
    public int getHeroLevel() {
        try {
            return hero.getLevel();
        } catch (Exception e) {
            return 0;
        }
    }

    // --- Update methods ---

    public void addKill() { monstersKilled++; }

    public void addFaint() { timesFainted++; }

    public void addDamageDealt(double amount) { damageDealt += amount; }

    public void addDamageTaken(double amount) { damageTaken += amount; }

    public void addGoldGained(int amount) { goldGained += amount; }

    public void addXpGained(int amount) { xpGained += amount; }

    // --- Getters ---

    public int getMonstersKilled() { return monstersKilled; }

    public int getTimesFainted() { return timesFainted; }

    public double getDamageDealt() { return damageDealt; }

    public double getDamageTaken() { return damageTaken; }

    public int getGoldGained() { return goldGained; }

    public int getXpGained() { return xpGained; }
}