package legends.valor.game;

import java.util.Scanner;

import legends.game.Game;
import legends.leaderboard.LeaderboardService;
import legends.persistence.SaveManager;
import legends.stats.GameStats;
import legends.valor.ui.ValorEndScreenRenderer;

/**
 * Facade / entry-point for Legends of Valor.
 * Delegates:
 *  - match gameplay to ValorMatch
 *  - post-game flow to ValorPostGameController
 */
public class ValorGame implements Game {

    private final Scanner in = new Scanner(System.in);

    private final SaveManager saveManager = new SaveManager("saves");
    private final LeaderboardService leaderboard = new LeaderboardService("saves");

    @Override
    public void run() {
        while (true) {
            ValorMatch match = new ValorMatch(in);
            ValorMatch.Outcome outcome = match.play();

            if (outcome == ValorMatch.Outcome.QUIT) {
                System.out.println("Leaving Legends of Valor...");
                return;
            }

            // Post-game flow (leaderboard + summary + save/load UI)
            ValorEndScreenRenderer renderer = new ValorEndScreenRenderer();
            ValorPostGameController post =
                    new ValorPostGameController(in, saveManager, leaderboard, renderer);

            GameStats stats = match.getGameStats();
            int roundsPlayed = match.getRoundsPlayed();
            post.handle(outcome, stats, roundsPlayed);

            if (!askPlayAgain()) {
                System.out.println("Returning to main menu...");
                return;
            }
        }
    }

    private boolean askPlayAgain() {
        while (true) {
            System.out.print("\nPlay Legends of Valor again? (Y/N): ");
            String line = in.nextLine().trim().toUpperCase();
            if (line.startsWith("Y")) return true;
            if (line.startsWith("N")) return false;
            System.out.println("Please enter Y or N.");
        }
    }
}