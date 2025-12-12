package legends.valor.ui;

import java.util.ArrayList;
import java.util.List;

import legends.leaderboard.LeaderboardEntry;
import legends.stats.GameRecord;
import legends.stats.GameStats;
import legends.ui.ConsoleUI;

import static legends.ui.ConsoleUI.*;

public class ValorEndScreenRenderer {

    public void renderEndScreen(GameRecord record, List<LeaderboardEntry> top10) {
        ConsoleUI.clearLineJunk();

        renderLeaderboard(top10);
        renderMatchSummary(record);
        renderPerHeroStats(record);

        ConsoleUI.boxed("POST-GAME OPTIONS",
                List.of(
                        CYAN + "[S]" + RESET + " Save last match",
                        CYAN + "[L]" + RESET + " Load last saved",
                        CYAN + "[ENTER]" + RESET + " Continue"
                )
        );
    }

    public void renderLoadedMatch(GameRecord loaded) {
        ConsoleUI.boxed("LOADED LAST SAVE", List.of("Showing loaded match summary below:"));
        renderMatchSummary(loaded);
        renderPerHeroStats(loaded);
    }

    private void renderLeaderboard(List<LeaderboardEntry> top10) {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{
                BOLD + "Rank" + RESET,
                BOLD + "Score" + RESET,
                BOLD + "Result" + RESET,
                BOLD + "Ended" + RESET,
                BOLD + "Summary" + RESET
        });

        if (top10 == null || top10.isEmpty()) {
            rows.add(new String[]{"-", "-", "-", "-", DIM + "No games recorded yet." + RESET});
        } else {
            for (int i = 0; i < top10.size(); i++) {
                LeaderboardEntry e = top10.get(i);
                rows.add(new String[]{
                        String.valueOf(i + 1),
                        String.valueOf(e.score),
                        colorResult(e.result),
                        String.valueOf(e.endedAt),
                        e.summaryLine
                });
            }
        }

        ConsoleUI.boxed("LEADERBOARD (Top 10)", ConsoleUI.table(rows));
    }

    private void renderMatchSummary(GameRecord r) {
        List<String[]> sumRows = new ArrayList<>();
        sumRows.add(new String[]{BOLD + "Mode" + RESET, String.valueOf(r.mode)});
        sumRows.add(new String[]{BOLD + "Result" + RESET, colorResult(r.result)});
        sumRows.add(new String[]{BOLD + "Rounds" + RESET, String.valueOf(r.roundsPlayed)});
        sumRows.add(new String[]{BOLD + "Start" + RESET, String.valueOf(r.startedAt)});
        sumRows.add(new String[]{BOLD + "End" + RESET, String.valueOf(r.endedAt)});

        int kills = 0, faints = 0, gold = 0, xp = 0;
        double dealt = 0, taken = 0;
        for (GameRecord.HeroRecord h : r.heroes) {
            kills += h.monstersKilled;
            faints += h.timesFainted;
            gold += h.goldGained;
            xp += h.xpGained;
            dealt += h.damageDealt;
            taken += h.damageTaken;
        }

        sumRows.add(new String[]{BOLD + "Total Kills" + RESET, String.valueOf(kills)});
        sumRows.add(new String[]{BOLD + "Total Faints" + RESET, String.valueOf(faints)});
        sumRows.add(new String[]{BOLD + "Dmg Dealt" + RESET, String.valueOf((int) dealt)});
        sumRows.add(new String[]{BOLD + "Dmg Taken" + RESET, String.valueOf((int) taken)});
        sumRows.add(new String[]{BOLD + "Gold Gained" + RESET, String.valueOf(gold)});
        sumRows.add(new String[]{BOLD + "XP Gained" + RESET, String.valueOf(xp)});

        ConsoleUI.boxed("MATCH SUMMARY", ConsoleUI.table(sumRows));
    }

    private void renderPerHeroStats(GameRecord record) {
        List<String[]> heroRows = new ArrayList<>();
        heroRows.add(new String[]{
                BOLD + "Hero" + RESET,
                BOLD + "Lv" + RESET,
                BOLD + "Kills" + RESET,
                BOLD + "Faints" + RESET,
                BOLD + "Dealt" + RESET,
                BOLD + "Taken" + RESET,
                BOLD + "Gold" + RESET,
                BOLD + "XP" + RESET
        });

        for (GameRecord.HeroRecord h : record.heroes) {
            heroRows.add(new String[]{
                    h.heroName,
                    String.valueOf(h.level),
                    String.valueOf(h.monstersKilled),
                    String.valueOf(h.timesFainted),
                    String.valueOf((int) h.damageDealt),
                    String.valueOf((int) h.damageTaken),
                    String.valueOf(h.goldGained),
                    String.valueOf(h.xpGained)
            });
        }

        ConsoleUI.boxed("PER-HERO STATS", ConsoleUI.table(heroRows));
    }

    private String colorResult(GameStats.GameResult r) {
        if (r == null) return "";
        switch (r) {
            case HEROES_WIN:   return GREEN + r + RESET;
            case MONSTERS_WIN: return RED + r + RESET;
            case QUIT:         return YELLOW + r + RESET;
            default:           return String.valueOf(r);
        }
    }
}