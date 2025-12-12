package legends.leaderboard;

import legends.stats.GameStats;

import java.io.Serializable;
import java.time.LocalDateTime;

public class LeaderboardEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    public final LocalDateTime endedAt;
    public final GameStats.GameMode mode;
    public final GameStats.GameResult result;
    public final int score;
    public final String summaryLine;

    public LeaderboardEntry(LocalDateTime endedAt, GameStats.GameMode mode, GameStats.GameResult result,
                            int score, String summaryLine) {
        this.endedAt = endedAt;
        this.mode = mode;
        this.result = result;
        this.score = score;
        this.summaryLine = summaryLine;
    }
}