package legends.valor.turn;

import java.util.List;
import java.util.Scanner;

import legends.characters.Hero;
import legends.characters.Monster;
import legends.valor.combat.ValorCombat;
import legends.valor.world.ValorDirection;
import legends.valor.world.ValorMovement;

/**
 * Console-based input for Legends of Valor.
 * Implements ValorInput so it can be used by controllers,
 * and also provides convenience helpers for turn actions.
 */
public class ConsoleValorInput implements ValorInput {

    public enum ActionResult { TURN_TAKEN, INVALID, QUIT }

    private final Scanner in;

    public ConsoleValorInput(Scanner in) {
        this.in = in;
    }

    @Override
    public String readLine(String prompt) {
        System.out.print(prompt);
        return in.nextLine();
    }

    /**
     * One-shot helper if you want the "ActionResult" style loop.
     * Not required by controllers, but safe to keep.
     */
    public ActionResult takeHeroTurn(Hero hero, ValorMovement movement, ValorCombat combat, List<Monster> laneMonsters) {
        String line = readLine("Enter command: ").trim().toUpperCase();
        if (line.isEmpty()) return ActionResult.INVALID;

        char cmd = line.charAt(0);
        if (cmd == 'Q') return ActionResult.QUIT;

        switch (cmd) {
            case 'W': return movement.moveHero(hero, ValorDirection.NORTH) ? ActionResult.TURN_TAKEN : ActionResult.INVALID;
            case 'S': return movement.moveHero(hero, ValorDirection.SOUTH) ? ActionResult.TURN_TAKEN : ActionResult.INVALID;
            case 'A': return movement.moveHero(hero, ValorDirection.WEST)  ? ActionResult.TURN_TAKEN : ActionResult.INVALID;
            case 'D': return movement.moveHero(hero, ValorDirection.EAST)  ? ActionResult.TURN_TAKEN : ActionResult.INVALID;
            case 'N':
                System.out.println(hero.getName() + " waits this turn.");
                return ActionResult.TURN_TAKEN;
            case 'F':
                return handleAttack(hero, combat, laneMonsters) ? ActionResult.TURN_TAKEN : ActionResult.INVALID;
            default:
                return ActionResult.INVALID;
        }
    }

    private boolean handleAttack(Hero hero, ValorCombat combat, List<Monster> laneMonsters) {
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
                int choice = Integer.parseInt(readLine("Choose target (number): ").trim());
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