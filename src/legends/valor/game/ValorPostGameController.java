package legends.valor.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import legends.characters.Hero;
import legends.leaderboard.LeaderboardEntry;
import legends.leaderboard.LeaderboardService;
import legends.persistence.SaveManager;
import legends.stats.GameRecord;
import legends.stats.GameStats;
import legends.stats.HeroStats;
import legends.ui.ConsoleUI;
import legends.valor.ui.ValorEndScreenRenderer;

import static legends.ui.ConsoleUI.*;

public class ValorPostGameController {

    private final Scanner in;
    private final SaveManager saveManager;
    private final LeaderboardService leaderboard;
    private final ValorEndScreenRenderer renderer;

    public ValorPostGameController(Scanner in,
                                   SaveManager saveManager,
                                   LeaderboardService leaderboard,
                                   ValorEndScreenRenderer renderer) {
        this.in = in;
        this.saveManager = saveManager;
        this.leaderboard = leaderboard;
        this.renderer = renderer;
    }

    public void handle(ValorMatch.Outcome outcome, GameStats gameStats, int roundsPlayed) {
        if (gameStats == null) {
            ConsoleUI.boxed("POST GAME", List.of(RED + "No stats available for this match." + RESET));
            return;
        }

        GameStats.GameResult result =
                (outcome == ValorMatch.Outcome.HERO_WIN) ? GameStats.GameResult.HEROES_WIN :
                (outcome == ValorMatch.Outcome.MONSTER_WIN) ? GameStats.GameResult.MONSTERS_WIN :
                GameStats.GameResult.QUIT;

        gameStats.markEnded(result);
        GameRecord record = buildRecordFromStats(gameStats, roundsPlayed);

        // update leaderboard (safe)
        try { leaderboard.add(record); } catch (Exception ignored) {}

        List<LeaderboardEntry> top10;
        try { top10 = leaderboard.top(10); }
        catch (Exception e) { top10 = new ArrayList<>(); }

        renderer.renderEndScreen(record, top10);

        // save/load prompt
        while (true) {
            System.out.print("\n" + CYAN + "[S]" + RESET + " Save  "
                    + CYAN + "[L]" + RESET + " Load  "
                    + CYAN + "[ENTER]" + RESET + " Continue : ");

            String line = in.nextLine().trim().toUpperCase();
            if (line.isEmpty()) break;

            if (line.startsWith("S")) {
                try {
                    saveManager.saveLastGame(record);
                    ConsoleUI.boxed("SAVED", List.of(GREEN + "Saved last match to disk." + RESET));
                } catch (Exception e) {
                    ConsoleUI.boxed("SAVE FAILED", List.of(RED + e.getMessage() + RESET));
                }
            } else if (line.startsWith("L")) {
                try {
                    GameRecord loaded = saveManager.loadLastGame();
                    if (loaded == null) {
                        ConsoleUI.boxed("LOAD", List.of(RED + "No saved match found yet." + RESET));
                    } else {
                        renderer.renderLoadedMatch(loaded);
                    }
                } catch (Exception e) {
                    ConsoleUI.boxed("LOAD FAILED", List.of(RED + e.getMessage() + RESET));
                }
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    private GameRecord buildRecordFromStats(GameStats stats, int roundsPlayed) {
        GameRecord rec = new GameRecord(
                stats.getMode(),
                stats.getResult(),
                stats.getStartedAt(),
                stats.getEndedAt(),
                roundsPlayed
        );

        for (HeroStats hs : stats.getHeroStats()) {
            Hero h = hs.getHero();
            rec.heroes.add(new GameRecord.HeroRecord(
                    h.getName(),
                    h.getLevel(),
                    hs.getMonstersKilled(),
                    hs.getTimesFainted(),
                    hs.getDamageDealt(),
                    hs.getDamageTaken(),
                    hs.getGoldGained(),
                    hs.getXpGained()
            ));
        }
        return rec;
    }
}