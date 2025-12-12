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

/**
 * Represents ONE match instance of Legends of Valor.
 * Orchestrates setup + round loop; delegates everything else.
 */
public class ValorMatch {

    public enum Outcome { HERO_WIN, MONSTER_WIN, QUIT }

    private final Scanner in;

    // Match state (owned here)
    private ValorBoard board;
    private ValorMovement movement;
    private ValorCombat combat;

    private Party party;
    private Market market;
    private List<Monster> laneMonsters = new ArrayList<>();

    private GameStats gameStats;
    private int roundsPlayed = 0;

    public ValorMatch(Scanner in) {
        this.in = in;
    }

    public Outcome play() {
        new ValorIntroScreen(in).show();

        ValorMatchSetup setup = new ValorMatchSetup();
        boolean ok = setup.setup(this);
        if (!ok) return Outcome.QUIT;

        ValorTurnManager turnManager = new ValorTurnManager(
        board, movement, combat, party, laneMonsters,
        new ConsoleValorInput(in)
        );

        while (true) {
            roundsPlayed++;

            Outcome outcome = turnManager.playOneRound();
            if (outcome != null) return outcome;
        }
    }

    // ===== getters for ValorGame / PostGameController =====
    public GameStats getGameStats() { return gameStats; }
    public int getRoundsPlayed() { return roundsPlayed; }

    // ===== package-private setters for setup class =====
    void setBoard(ValorBoard board) { this.board = board; }
    void setMovement(ValorMovement movement) { this.movement = movement; }
    void setCombat(ValorCombat combat) { this.combat = combat; }
    void setParty(Party party) { this.party = party; }
    void setMarket(Market market) { this.market = market; }
    void setLaneMonsters(List<Monster> laneMonsters) { this.laneMonsters = laneMonsters; }
    void setGameStats(GameStats gameStats) { this.gameStats = gameStats; }
}