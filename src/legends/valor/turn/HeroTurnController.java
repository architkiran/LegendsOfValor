package legends.valor.turn;

import legends.characters.Hero;
import legends.characters.Monster;
import legends.valor.combat.ValorCombat;
import legends.valor.world.ValorBoard;
import legends.valor.world.ValorDirection;
import legends.valor.world.ValorMovement;

import java.util.List;
import java.util.Map;

public class HeroTurnController {

    private final ValorBoard board;
    private final ValorMovement movement;
    private final ValorInput input;

    private final HeroTurnUIHelper ui;
    private final HeroActionService actions;

    public HeroTurnController(ValorBoard board,
                              ValorMovement movement,
                              ValorCombat combat,
                              List<Monster> laneMonsters,
                              ValorInput input,
                              Map<Hero, Integer> homeLane) {
        this.board = board;
        this.movement = movement;
        this.input = input;

        this.ui = new HeroTurnUIHelper(input);
        this.actions = new HeroActionService(board, movement, combat, laneMonsters, ui, homeLane);
    }

    /** @return false if player quits the match; true otherwise. */
    public boolean handleHeroTurn(Hero hero, int heroNumber) {
        actions.bindHomeLaneIfMissing(hero);

        while (true) {
            System.out.println();
            board.print();

            System.out.println("Hero " + heroNumber + " turn: " + hero.getName());
            System.out.println("""
Controls:
  Move: W/A/S/D
  Attack: F
  Cast Spell: C
  Use Potion: P
  Equip: E
  Teleport: T
  Recall: R
  Remove Obstacle: O
  Wait: N
  Quit: Q
""");

            String cmdLine = input.readLine("Enter command: ");
            if (cmdLine == null) continue;

            cmdLine = cmdLine.trim();
            if (cmdLine.isEmpty()) continue;

            char cmd = Character.toUpperCase(cmdLine.charAt(0));

            switch (cmd) {
                case 'Q' -> { return false; }

                case 'N' -> {
                    System.out.println(hero.getName() + " waits this turn.");
                    return true;
                }

                case 'F' -> { if (actions.attack(hero)) return true; }
                case 'C' -> { if (actions.castSpell(hero)) return true; }
                case 'P' -> { if (actions.usePotion(hero)) return true; }
                case 'E' -> { if (actions.equip(hero, input)) return true; }
                case 'T' -> { if (actions.teleport(hero)) return true; }
                case 'R' -> { if (actions.recall(hero)) return true; }
                case 'O' -> { if (actions.removeObstacle(hero, input)) return true; }

                case 'W' -> { if (actions.move(hero, ValorDirection.NORTH)) return true; }
                case 'S' -> { if (actions.move(hero, ValorDirection.SOUTH)) return true; }
                case 'A' -> { if (actions.move(hero, ValorDirection.WEST))  return true; }
                case 'D' -> { if (actions.move(hero, ValorDirection.EAST))  return true; }

                default -> System.out.println("Unknown command.");
            }
        }
    }
}