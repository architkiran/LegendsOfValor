package legends.stats;

public class StatsPrinter {

    public static void printGameSummary(GameStats stats) {
        System.out.println();
        System.out.println("═══════════════════════════════════════");
        System.out.println("           GAME SUMMARY");
        System.out.println("═══════════════════════════════════════");
        System.out.println("Mode   : " + stats.getMode());
        System.out.println("Result : " + stats.getResult());
        System.out.println("Started: " + stats.getStartedAt());
        System.out.println("Ended  : " + stats.getEndedAt());
        System.out.println();

        System.out.println("Per-Hero Performance:");
        System.out.println("───────────────────────────────────────");

        for (HeroStats hs : stats.getHeroStats()) {
            var h = hs.getHero();
            System.out.println("Hero: " + h.getName() + "  (Lvl " + h.getLevel() + ")");
            System.out.println("  Damage Dealt  : " + (int) hs.getDamageDealt());
            System.out.println("  Damage Taken  : " + (int) hs.getDamageTaken());
            System.out.println("  Monsters Killed: " + hs.getMonstersKilled());
            System.out.println("  Times Fainted : " + hs.getTimesFainted());
            System.out.println("  Gold Gained   : " + hs.getGoldGained());
            System.out.println("  XP Gained     : " + hs.getXpGained());
            System.out.println();
        }
    }
}