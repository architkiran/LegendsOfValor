/**
 * File: HeroTurnController.java
 * Package: legends.valor.turn
 *
 * Purpose:
 *   Controls the interactive turn flow for a single hero in Legends of Valor.
 *
 * Responsibilities:
 *   - Render the board and hero turn menu for the active hero
 *   - Read and interpret a single command input from the player
 *   - Delegate action execution to HeroActionService (combat, movement, equipment, market)
 *   - Return control signals to the match loop (continue turn flow vs quit match)
 */
package legends.valor.turn;

import legends.characters.Hero;
import legends.characters.Monster;
import legends.market.Market;
import legends.valor.combat.ValorCombat;
import legends.valor.world.ValorBoard;
import legends.valor.world.ValorDirection;
import legends.valor.world.ValorMovement;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class HeroTurnController {

    // Board is rendered each turn and used to infer lane information from hero position
    private final ValorBoard board;

    // Movement provides hero location lookup used for UI context
    private final ValorMovement movement;

    // Input abstraction used for command prompts (console or other implementations)
    private final ValorInput input;

    // Facade over hero actions, delegating to specialized action classes
    private final HeroActionService actions;

    // Renders the turn menu and available command options for the hero
    private final HeroTurnMenuView menuView;

    public HeroTurnController(ValorBoard board,
                              ValorMovement movement,
                              ValorCombat combat,
                              List<Monster> laneMonsters,
                              ValorInput input,
                              Map<Hero, Integer> homeLane,
                              Market market,
                              Scanner scanner) {
        this.board = board;
        this.movement = movement;
        this.input = input;

        // UI helper centralizes selection prompts used by action classes
        HeroTurnUIHelper ui = new HeroTurnUIHelper(input);
        this.actions = new HeroActionService(board, movement, combat, laneMonsters, ui, homeLane, market, scanner);
        this.menuView = new HeroTurnMenuView();
    }

    /**
     * Runs the interactive command loop for a single hero turn.
     *
     * @return false if the player chooses to quit the match; true otherwise
     */
    public boolean handleHeroTurn(Hero hero, int heroNumber) {
        if (hero == null) return true;

        // Ensure recall/teleport rules have a home lane binding for this hero
        actions.bindHomeLaneIfMissing(hero);

        while (true) {
            System.out.println();
            board.print();

            // Position and lane are used to show contextual info in the menu
            int[] pos = movement.findHero(hero);
            int lane = (pos == null) ? -1 : board.getLane(pos[1]);

            menuView.renderTurnMenu(heroNumber, hero, pos, lane);

            char cmd = readCommand();
            if (cmd == 0) continue;

            switch (cmd) {
                case 'Q':
                    return false;

                case 'N':
                    System.out.println(hero.getName() + " waits this turn.");
                    return true;

                case 'F':
                    if (actions.attack(hero)) return true;
                    break;

                case 'C':
                    if (actions.castSpell(hero)) return true;
                    break;

                case 'P':
                    if (actions.usePotion(hero)) return true;
                    break;

                case 'E':
                    if (actions.equip(hero, input)) return true;
                    break;

                case 'T':
                    if (actions.teleport(hero)) return true;
                    break;

                case 'R':
                    if (actions.recall(hero)) return true;
                    break;

                case 'O':
                    if (actions.removeObstacle(hero, input)) return true;
                    break;

                case 'M':
                    // Market is intended as a non-consuming action in this flow
                    if (!actions.openMarket(hero)) {
                        System.out.println("Market could not be opened.");
                    }
                    break;

                case 'W':
                    if (actions.move(hero, ValorDirection.NORTH)) return true;
                    break;

                case 'S':
                    if (actions.move(hero, ValorDirection.SOUTH)) return true;
                    break;

                case 'A':
                    if (actions.move(hero, ValorDirection.WEST)) return true;
                    break;

                case 'D':
                    if (actions.move(hero, ValorDirection.EAST)) return true;
                    break;

                default:
                    System.out.println("Unknown command.");
            }
        }
    }

    /**
     * Reads a single command character from the player.
     *
     * @return uppercase command char, or 0 if input is empty/invalid
     */
    private char readCommand() {
        String cmdLine = input.readLine("Enter command: ");
        if (cmdLine == null) return 0;

        cmdLine = cmdLine.trim();
        if (cmdLine.isEmpty()) return 0;

        return Character.toUpperCase(cmdLine.charAt(0));
    }
}
