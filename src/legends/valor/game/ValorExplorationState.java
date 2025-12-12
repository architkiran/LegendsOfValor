package legends.valor.game;

import java.util.Scanner;

import legends.valor.world.ValorBoard;
import legends.valor.world.ValorMovement;
import legends.valor.world.ValorDirection;
import legends.characters.Hero;

/**
 * Handles player-controlled hero movement in Legends of Valor.
 *
 * This state:
 *  - Reads W/A/S/D
 *  - Moves active hero if legal
 *  - Reprints the board
 *  - Later will also handle:
 *        * teleport
 *        * recall
 *        * attack/action commands
 */
public class ValorExplorationState {

    private final ValorBoard board;
    private final ValorMovement movement;
    private final Scanner in;

    private final Hero activeHero;

    public ValorExplorationState(ValorBoard board, Hero activeHero) {
        this.board = board;
        this.movement = new ValorMovement(board);
        this.activeHero = activeHero;
        this.in = new Scanner(System.in);
    }

    public void run() {

        while (true) {

            board.print(); // show map

            System.out.println("Enter command (W/A/S/D to move, Q to quit): ");
            char cmd = in.nextLine().trim().toUpperCase().charAt(0);

            if (cmd == 'Q') {
                break;
            }

            boolean moved = false;

            switch (cmd) {
                case 'W': moved = movement.moveHero(activeHero, ValorDirection.NORTH); break;
                case 'S': moved = movement.moveHero(activeHero, ValorDirection.SOUTH); break;
                case 'A': moved = movement.moveHero(activeHero, ValorDirection.WEST); break;
                case 'D': moved = movement.moveHero(activeHero, ValorDirection.EAST); break;
                default:
                    System.out.println("Invalid command.");
            }

            if (!moved) {
                System.out.println("Illegal move!");
            }
        }
    }
}