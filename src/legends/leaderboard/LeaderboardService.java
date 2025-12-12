package legends.leaderboard;

import legends.stats.GameRecord;
import legends.stats.GameStats;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class LeaderboardService {

    private final Path dir;
    private final Path filePath;

    public LeaderboardService(String folderName) {
        this.dir = Path.of(folderName);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            System.out.println("[Leaderboard] Could not create directory: " + dir);
        }
        this.filePath = dir.resolve("leaderboard.ser");
    }

    public void add(GameRecord record) {
        if (record == null) return;

        List<LeaderboardEntry> entries = loadAll();
        entries.add(toEntry(record));
        saveAll(entries);
    }

    public List<LeaderboardEntry> top(int n) {
        List<LeaderboardEntry> entries = loadAll();
        entries.sort(Comparator.comparingInt((LeaderboardEntry e) -> e.score).reversed());
        if (entries.size() <= n) return entries;
        return entries.subList(0, n);
    }

    // Optional helper (nice for debugging UI)
    public Path getFilePath() {
        return filePath;
    }

    private LeaderboardEntry toEntry(GameRecord r) {
        int totalKills = r.heroes.stream().mapToInt(h -> h.monstersKilled).sum();
        int totalDeaths = r.heroes.stream().mapToInt(h -> h.timesFainted).sum();

        int score = 0;
        if (r.result == GameStats.GameResult.HEROES_WIN) score += 1000;
        if (r.result == GameStats.GameResult.MONSTERS_WIN) score += 200;

        score += totalKills * 50;
        score -= totalDeaths * 30;
        score -= r.roundsPlayed * 5;

        String summary = r.result + " | rounds=" + r.roundsPlayed
                + " | kills=" + totalKills + " | deaths=" + totalDeaths;

        return new LeaderboardEntry(r.endedAt, r.mode, r.result, score, summary);
    }

    private List<LeaderboardEntry> loadAll() {
        if (!Files.exists(filePath)) return new ArrayList<>();

        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(filePath.toFile()))
        )) {
            Object obj = in.readObject();

            if (!(obj instanceof List<?>)) {
                System.out.println("[Leaderboard] Corrupt file (not a List). Resetting leaderboard.");
                return new ArrayList<>();
            }

            @SuppressWarnings("unchecked")
            List<LeaderboardEntry> list = (List<LeaderboardEntry>) obj;
            return list;

        } catch (InvalidClassException ice) {
            // Happens if you changed class structure since the file was written
            System.out.println("[Leaderboard] Old/incompatible leaderboard file. Delete it to reset: " + filePath);
            return new ArrayList<>();
        } catch (Exception e) {
            System.out.println("[Leaderboard] Failed to load leaderboard (" + e.getClass().getSimpleName() + "). Resetting.");
            return new ArrayList<>();
        }
    }

    private void saveAll(List<LeaderboardEntry> entries) {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(filePath.toFile()))
        )) {
            out.writeObject(entries);
            out.flush();
        } catch (NotSerializableException nse) {
            System.out.println("[Leaderboard] SAVE FAILED: Something is not Serializable.");
        } catch (IOException e) {
            System.out.println("[Leaderboard] SAVE FAILED: " + e.getMessage());
        }
    }
}