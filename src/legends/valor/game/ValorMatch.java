package legends.valor.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import legends.characters.Hero;
import legends.characters.Monster;
import legends.characters.Party;
import legends.market.Market;
import legends.stats.GameStats;
import legends.valor.combat.ValorCombat;
import legends.valor.turn.ConsoleValorInput;
import legends.valor.turn.ValorTurnManager;
import legends.valor.world.ValorBoard;
import legends.valor.world.ValorMovement;

public class ValorMatch {

    public enum Outcome { HERO_WIN, MONSTER_WIN, QUIT }

    private final Scanner in;

    private ValorBoard board;
    private ValorMovement movement;
    private ValorCombat combat;

    private Party party;
    private Market market;
    private List<Monster> laneMonsters = new ArrayList<Monster>();

    private GameStats gameStats;
    private int roundsPlayed = 0;

    // ===== spawn tuning =====
    private static final int SPAWN_INTERVAL = 4; // you can change: easy6/med4/hard2

    // Spawner (create after setup)
    private ValorSpawner spawner;

    public ValorMatch(Scanner in) {
        this.in = in;
    }

    public Outcome play() {
        new ValorIntroScreen(in).show();

        ValorMatchSetup setup = new ValorMatchSetup();
        boolean ok = setup.setup(this);
        if (!ok) return Outcome.QUIT;

        // Create spawner after board is ready
        this.spawner = new ValorSpawner(board);

        // ✅ FIX: pass market + scanner into turn manager
        ValorTurnManager turnManager = new ValorTurnManager(
                board,
                movement,
                combat,
                party,
                laneMonsters,
                new ConsoleValorInput(in),
                market,
                in
        );

        while (true) {
            roundsPlayed++;

            renderRoundStatus();

            Outcome outcome = turnManager.playOneRound();
            if (outcome != null) return outcome;

            // ===== END OF ROUND: regen =====
            endOfRound(party.getHeroes());

            // ===== EVERY N ROUNDS: spawn 3 monsters (1 per lane) =====
            if (roundsPlayed % SPAWN_INTERVAL == 0) {
                List<Monster> spawned = spawner.spawnLaneMonsters(party);
                if (spawned != null && !spawned.isEmpty()) {
                    laneMonsters.addAll(spawned);
                }
            }
        }
    }

    /**
     * End-of-round regen rule:
     * Alive heroes recover 10% of MAX HP/MP, capped at max.
     * Using project convention: maxHP = level*100, maxMP = level*50.
     */
    private void endOfRound(List<Hero> heroes) {
        if (heroes == null) return;

        for (Hero h : heroes) {
            if (h == null) continue;
            if (h.getHP() <= 0) continue;

            double maxHP = h.getLevel() * 100.0;
            double maxMP = h.getLevel() * 50.0;

            h.setHP(Math.min(maxHP, h.getHP() + 0.10 * maxHP));
            h.setMP(Math.min(maxMP, h.getMP() + 0.10 * maxMP));
        }
    }

    // ===== getters =====
    public GameStats getGameStats() { return gameStats; }
    public int getRoundsPlayed() { return roundsPlayed; }

    // ===== setters for setup =====
    void setBoard(ValorBoard board) { this.board = board; }
    void setMovement(ValorMovement movement) { this.movement = movement; }
    void setCombat(ValorCombat combat) { this.combat = combat; }
    void setParty(Party party) { this.party = party; }
    void setMarket(Market market) { this.market = market; }
    void setLaneMonsters(List<Monster> laneMonsters) { this.laneMonsters = laneMonsters; }
    void setGameStats(GameStats gameStats) { this.gameStats = gameStats; }

    private void renderRoundStatus() {
        System.out.println();
        System.out.println("========== ROUND " + roundsPlayed + " STATUS ==========");

        System.out.println("--- Heroes on board ---");
        for (int r = 0; r < ValorBoard.ROWS; r++) {
            for (int c = 0; c < ValorBoard.COLS; c++) {
                Hero h = board.getTile(r, c).getHero();
                if (h != null) {
                    double maxHP = h.getLevel() * 100.0;
                    double maxMP = h.getLevel() * 50.0;
                    System.out.printf("%s L%d @(%d,%d)  HP %.0f/%.0f  MP %.0f/%.0f%n",
                            h.getName(), h.getLevel(), r, c,
                            h.getHP(), maxHP, h.getMP(), maxMP);
                }
            }
        }

        System.out.println("--- Monsters on board ---");
        for (int r = 0; r < ValorBoard.ROWS; r++) {
            for (int c = 0; c < ValorBoard.COLS; c++) {
                Monster m = board.getTile(r, c).getMonster();
                if (m != null) {
                    System.out.printf("%s L%d @(%d,%d)  HP %.0f%n",
                            m.getName(), m.getLevel(), r, c, m.getHP());
                }
            }
        }

        System.out.println("======================================");
        System.out.println();
    }
}