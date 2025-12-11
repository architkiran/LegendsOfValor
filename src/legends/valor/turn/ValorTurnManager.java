package legends.valor.turn;

import legends.characters.Hero;
import legends.characters.Monster;
import legends.characters.Party;
import legends.valor.world.ValorBoard;
import legends.valor.world.ValorMovement;

import java.util.List;

/**
 * Orchestrates the round-based flow:
 *   Hero 1 → Hero 2 → Hero 3 → all monsters → repeat.
 */
public class ValorTurnManager {

    private final ValorBoard board;
    private final ValorMovement movement;
    private final Party party;
    private final List<Monster> laneMonsters;

    private final HeroTurnController heroController;
    private final MonsterTurnController monsterController;

    public ValorTurnManager(ValorBoard board,
                            ValorMovement movement,
                            Party party,
                            List<Monster> laneMonsters) {

        this.board = board;
        this.movement = movement;
        this.party = party;
        this.laneMonsters = laneMonsters;

        ValorInput input = new ConsoleValorInput();
        this.heroController = new HeroTurnController(board, movement, party, laneMonsters, input);
        this.monsterController = new MonsterTurnController(movement);
    }

    /**
     * Main loop for Legends of Valor.
     * Returns when the player quits (Q).
     */
    public void run() {
        List<Hero> heroes = party.getHeroes();
        if (heroes.isEmpty()) {
            System.out.println("Party has no heroes, cannot move.");
            return;
        }

        while (true) {
            // ----- HERO PHASE -----
            for (int i = 0; i < heroes.size(); i++) {
                Hero h = heroes.get(i);

                boolean continueGame = heroController.handleHeroTurn(h, i + 1);
                if (!continueGame) {
                    System.out.println("Exiting Legends of Valor...");
                    return;
                }
            }

            // ----- MONSTER PHASE -----
            monsterController.advanceMonsters(laneMonsters);
        }
    }
}