package legends.persistence;

import legends.stats.GameRecord;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class SaveManager {

    private final Path saveDir;
    private final Path lastGameFile;

    public SaveManager(String folderName) {
        this.saveDir = Path.of(folderName);
        this.lastGameFile = saveDir.resolve("last_game.ser");
        ensureDir();
    }

    private void ensureDir() {
        try {
            Files.createDirectories(saveDir);
        } catch (IOException e) {
            System.out.println("[SaveManager] Could not create save directory: " + saveDir);
        }
    }

    public boolean saveLastGame(GameRecord record) {
        if (record == null) {
            System.out.println("[SaveManager] Nothing to save (record is null).");
            return false;
        }

        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(lastGameFile.toFile()))
        )) {
            out.writeObject(record);
            out.flush();
            return true;
        } catch (NotSerializableException nse) {
            System.out.println("[SaveManager] SAVE FAILED: GameRecord (or something inside it) is not Serializable.");
            System.out.println("[SaveManager] Tip: make GameRecord and nested HeroRecord implement java.io.Serializable.");
            return false;
        } catch (IOException e) {
            System.out.println("[SaveManager] SAVE FAILED: " + e.getMessage());
            return false;
        }
    }

    public GameRecord loadLastGame() {
        if (!Files.exists(lastGameFile)) {
            return null;
        }

        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(lastGameFile.toFile()))
        )) {
            Object obj = in.readObject();
            return (GameRecord) obj;
        } catch (ClassNotFoundException e) {
            System.out.println("[SaveManager] LOAD FAILED: Class mismatch (did you rename/move classes?)");
            return null;
        } catch (IOException e) {
            System.out.println("[SaveManager] LOAD FAILED: " + e.getMessage());
            return null;
        }
    }

    public Path getLastGameFilePath() {
        return lastGameFile;
    }
}