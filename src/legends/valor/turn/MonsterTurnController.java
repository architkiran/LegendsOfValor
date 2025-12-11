package legends.valor.turn;

import legends.characters.Monster;
import legends.valor.world.ValorMovement;
import legends.valor.world.ValorDirection;

import java.util.List;

/**
 * Controls how monsters advance each round in Legends of Valor.
 */
public class MonsterTurnController {

    private final ValorMovement movement;

    public MonsterTurnController(ValorMovement movement) {
        this.movement = movement;
    }

    /**
     * One "monsters' phase": each lane monster tries to advance once.
     */
    public void advanceMonsters(List<Monster> laneMonsters) {
        if (laneMonsters == null || laneMonsters.isEmpty()) return;

        System.out.println();
        System.out.println("\u001B[91mMonsters advance toward your Nexus...\u001B[0m");

        for (Monster m : laneMonsters) {
            if (m == null) continue;  // in case some lane has no monster

            // super simple AI: try SOUTH then sideways
            boolean moved =
                    tryMove(m, ValorDirection.SOUTH) ||
                    tryMove(m, ValorDirection.WEST)  ||
                    tryMove(m, ValorDirection.EAST);

            // if all three fail, the monster stays in place this round
        }
    }

    private boolean tryMove(Monster m, ValorDirection dir) {
        return movement.moveMonster(m, dir);
    }
}