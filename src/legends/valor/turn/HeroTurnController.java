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

    private final ValorBoard board;
    private final ValorMovement movement;
    private final ValorInput input;

    private final HeroActionService actions;

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

        HeroTurnUIHelper ui = new HeroTurnUIHelper(input);
        this.actions = new HeroActionService(board, movement, combat, laneMonsters, ui, homeLane, market, scanner);
    }

    /** @return false if player quits the match; true otherwise. */
    public boolean handleHeroTurn(Hero hero, int heroNumber) {
        actions.bindHomeLaneIfMissing(hero);

        while (true) {
            System.out.println();
            board.print();

            System.out.println("Hero " + heroNumber + " turn: " + hero.getName());
            System.out.println(
                    "Controls:\n" +
                    "  Move: W/A/S/D\n" +
                    "  Attack: F\n" +
                    "  Cast Spell: C\n" +
                    "  Use Potion: P\n" +
                    "  Equip: E\n" +
                    "  Teleport: T\n" +
                    "  Recall: R\n" +
                    "  Remove Obstacle: O\n" +
                    "  Market: M\n" +
                    "  Wait: N\n" +
                    "  Quit: Q\n"
            );

            String cmdLine = input.readLine("Enter command: ");
            if (cmdLine == null) continue;

            cmdLine = cmdLine.trim();
            if (cmdLine.isEmpty()) continue;

            char cmd = Character.toUpperCase(cmdLine.charAt(0));

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
                    if (actions.openMarket(hero)) return true;
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
}