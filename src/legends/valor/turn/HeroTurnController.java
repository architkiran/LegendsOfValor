package legends.valor.turn;

import legends.characters.Hero;
import legends.characters.Party;
import legends.characters.Monster;
import legends.valor.world.ValorBoard;
import legends.valor.world.ValorDirection;
import legends.valor.world.ValorMovement;

import java.util.List;

/**
 * Handles a single hero's turn in Legends of Valor.
 */
public class HeroTurnController {

    private final ValorBoard board;
    private final ValorMovement movement;
    private final Party party;
    private final List<Monster> laneMonsters;
    private final ValorInput input;

    public HeroTurnController(ValorBoard board,
                              ValorMovement movement,
                              Party party,
                              List<Monster> laneMonsters,
                              ValorInput input) {
        this.board = board;
        this.movement = movement;
        this.party = party;
        this.laneMonsters = laneMonsters;
        this.input = input;
    }

    /**
     * Runs one interactive turn for the given hero.
     *
     * @param hero       hero whose turn it is
     * @param heroNumber 1-based index (Hero 1 / Hero 2 / Hero 3)
     * @return false if the player quits the game, true otherwise
     */
    public boolean handleHeroTurn(Hero hero, int heroNumber) {

        while (true) {
            System.out.println();
            // You can switch to board.print(party, laneMonsters) later if you
            // add a numbered renderer. For now we keep the simple print().
            board.print();

            System.out.println("Controls: W = up | A = left | S = down | D = right |");
            System.out.println("          N = skip hero | Q = quit LoV");
            String cmdLine = input.readLine(
                    "Enter command for Hero " + heroNumber + " (" + hero.getName() + "): "
            );

            if (cmdLine == null) return true;
            cmdLine = cmdLine.trim().toUpperCase();
            if (cmdLine.isEmpty()) continue;

            char cmd = cmdLine.charAt(0);

            // ----- quitting whole LoV mode -----
            if (cmd == 'Q') {
                return false; // tell the caller to stop the loop
            }

            // ----- skip hero turn -----
            if (cmd == 'N') {
                return true;  // turn consumed, but no movement
            }

            // ----- movement -----
            boolean moved = false;
            switch (cmd) {
                case 'W' -> moved = movement.moveHero(hero, ValorDirection.NORTH);
                case 'S' -> moved = movement.moveHero(hero, ValorDirection.SOUTH);
                case 'A' -> moved = movement.moveHero(hero, ValorDirection.WEST);
                case 'D' -> moved = movement.moveHero(hero, ValorDirection.EAST);
                default -> System.out.println("Invalid command.");
            }

            if (!moved) {
                System.out.println("Cannot move there!");
                // let the same hero try again (don’t advance turn yet)
                continue;
            }

            // valid move → turn finished
            return true;
        }
    }
}