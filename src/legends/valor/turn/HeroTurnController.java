package legends.valor.turn;

import legends.characters.Hero;
import legends.characters.Monster;
import legends.valor.combat.ValorCombat;
import legends.valor.world.ValorBoard;
import legends.valor.world.ValorDirection;
import legends.valor.world.ValorMovement;

import java.util.List;

public class HeroTurnController {

    private final ValorBoard board;
    private final ValorMovement movement;
    private final ValorCombat combat;
    private final List<Monster> laneMonsters;
    private final ValorInput input;

    public HeroTurnController(ValorBoard board,
                              ValorMovement movement,
                              ValorCombat combat,
                              List<Monster> laneMonsters,
                              ValorInput input) {
        this.board = board;
        this.movement = movement;
        this.combat = combat;
        this.laneMonsters = laneMonsters;
        this.input = input;
    }

    /**
     * @return false if player quits the match; true otherwise
     */
    public boolean handleHeroTurn(Hero hero, int heroNumber) {
        while (true) {
            System.out.println();
            board.print();

            System.out.println("Hero " + heroNumber + " turn: " + hero.getName());
            System.out.println("Controls: W/A/S/D = move | F = attack | N = wait | Q = quit");

            String cmdLine = input.readLine("Enter command: ");
            if (cmdLine == null) continue;

            cmdLine = cmdLine.trim().toUpperCase();
            if (cmdLine.isEmpty()) continue;

            char cmd = cmdLine.charAt(0);

            if (cmd == 'Q') return false;
            if (cmd == 'N') {
                System.out.println(hero.getName() + " waits this turn.");
                return true;
            }

            if (cmd == 'F') {
                boolean ok = doAttack(hero);
                if (!ok) System.out.println("Attack failed.");
                else return true;
                continue;
            }

            boolean moved = switch (cmd) {
                case 'W' -> movement.moveHero(hero, ValorDirection.NORTH);
                case 'S' -> movement.moveHero(hero, ValorDirection.SOUTH);
                case 'A' -> movement.moveHero(hero, ValorDirection.WEST);
                case 'D' -> movement.moveHero(hero, ValorDirection.EAST);
                default -> false;
            };

            if (!moved) {
                System.out.println("Cannot move there!");
                continue;
            }

            return true;
        }
    }

    private boolean doAttack(Hero hero) {
        List<Monster> inRange = combat.getMonstersInRange(hero);
        if (inRange.isEmpty()) {
            System.out.println("No monsters in attack range!");
            return false;
        }

        Monster target;
        if (inRange.size() == 1) {
            target = inRange.get(0);
        } else {
            System.out.println("Monsters in range:");
            for (int i = 0; i < inRange.size(); i++) {
                Monster m = inRange.get(i);
                System.out.println((i + 1) + ". " + m.getName() + " (HP: " + (int) m.getHP() + ")");
            }
            try {
                int choice = Integer.parseInt(input.readLine("Choose target (number): ").trim());
                if (choice < 1 || choice > inRange.size()) return false;
                target = inRange.get(choice - 1);
            } catch (Exception e) {
                return false;
            }
        }

        boolean killed = combat.heroAttack(hero, target);
        if (killed) laneMonsters.remove(target);
        return true;
    }
}