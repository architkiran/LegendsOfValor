package legends.stats;

import legends.characters.Hero;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class GameStats {

    public enum GameMode {
        MONSTERS_AND_HEROES,
        LEGENDS_OF_VALOR
    }

    public enum GameResult {
        HEROES_WIN,
        MONSTERS_WIN,
        QUIT
    }

    private final GameMode mode;
    private final LocalDateTime startedAt;
    private LocalDateTime endedAt;

    private GameResult result;

    // how many full rounds were completed (hero-phase + monster-phase)
    private int rounds = 0;

    // One HeroStats per hero
    private final Map<Hero, HeroStats> heroStats = new LinkedHashMap<>();

    public GameStats(GameMode mode, List<Hero> heroes) {
        this.mode = mode;
        this.startedAt = LocalDateTime.now();
        if (heroes != null) {
            for (Hero h : heroes) {
                heroStats.put(h, new HeroStats(h));
            }
        }
    }

    // ---------------------------
    // Lifecycle
    // ---------------------------

    public GameMode getMode() { return mode; }

    public void addRound() { rounds++; }

    public int getRounds() { return rounds; }

    public void markEnded(GameResult result) {
        this.result = result;
        this.endedAt = LocalDateTime.now();
    }

    public GameResult getResult() { return result; }

    public LocalDateTime getStartedAt() { return startedAt; }

    public LocalDateTime getEndedAt() { return endedAt; }

    public Duration getDuration() {
        if (endedAt == null) return Duration.ZERO;
        return Duration.between(startedAt, endedAt);
    }

    // ---------------------------
    // Hero stats access
    // ---------------------------

    public Collection<HeroStats> getHeroStats() {
        return heroStats.values();
    }

    public HeroStats statsFor(Hero hero) {
        return heroStats.get(hero);
    }

    // ---------------------------
    // Totals (for leaderboard rows)
    // ---------------------------

    public int totalKills() {
        int sum = 0;
        for (HeroStats hs : heroStats.values()) sum += hs.getMonstersKilled();
        return sum;
    }

    public int totalFaints() {
        int sum = 0;
        for (HeroStats hs : heroStats.values()) sum += hs.getTimesFainted();
        return sum;
    }

    public double totalDamageDealt() {
        double sum = 0;
        for (HeroStats hs : heroStats.values()) sum += hs.getDamageDealt();
        return sum;
    }

    public double totalDamageTaken() {
        double sum = 0;
        for (HeroStats hs : heroStats.values()) sum += hs.getDamageTaken();
        return sum;
    }

    public int totalGoldGained() {
        int sum = 0;
        for (HeroStats hs : heroStats.values()) sum += hs.getGoldGained();
        return sum;
    }

    public int totalXpGained() {
        int sum = 0;
        for (HeroStats hs : heroStats.values()) sum += hs.getXpGained();
        return sum;
    }

    /**
     * Simple score heuristic (you can tweak later):
     * - Win bonus, lose penalty
     * - Reward kills, damage dealt
     * - Penalize fainting + damage taken
     * - Small bonus for finishing faster (fewer rounds)
     */
    public int computeScore() {
        int score = 0;

        if (result == GameResult.HEROES_WIN) score += 1000;
        if (result == GameResult.MONSTERS_WIN) score -= 200;

        score += totalKills() * 200;
        score += (int) Math.round(totalDamageDealt() * 0.5);

        score -= totalFaints() * 150;
        score -= (int) Math.round(totalDamageTaken() * 0.2);

        score -= rounds * 10; // faster win => higher score

        return Math.max(score, 0);
    }
}