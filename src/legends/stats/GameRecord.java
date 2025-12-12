package legends.stats;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GameRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    public static class HeroRecord implements Serializable {
        private static final long serialVersionUID = 1L;

        public final String heroName;
        public final int level;

        public final int monstersKilled;
        public final int timesFainted;
        public final double damageDealt;
        public final double damageTaken;

        public final int goldGained;
        public final int xpGained;

        public HeroRecord(String heroName, int level,
                          int monstersKilled, int timesFainted,
                          double damageDealt, double damageTaken,
                          int goldGained, int xpGained) {
            this.heroName = heroName;
            this.level = level;
            this.monstersKilled = monstersKilled;
            this.timesFainted = timesFainted;
            this.damageDealt = damageDealt;
            this.damageTaken = damageTaken;
            this.goldGained = goldGained;
            this.xpGained = xpGained;
        }
    }

    public final GameStats.GameMode mode;
    public final GameStats.GameResult result;

    public final LocalDateTime startedAt;
    public final LocalDateTime endedAt;

    public final int roundsPlayed;
    public final List<HeroRecord> heroes = new ArrayList<>();

    public GameRecord(GameStats.GameMode mode,
                      GameStats.GameResult result,
                      LocalDateTime startedAt,
                      LocalDateTime endedAt,
                      int roundsPlayed) {
        this.mode = mode;
        this.result = result;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.roundsPlayed = roundsPlayed;
    }
}