package legends.valor.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import legends.characters.Monster;
import legends.valor.world.ValorBoard;
import legends.valor.world.ValorDirection;
import legends.valor.world.ValorMovement;

/**
 * Encapsulates the "brain" for monster movement in Legends of Valor.
 *
 * Behaviour:
 *  1. Try to move SOUTH (towards heroes' Nexus)
 *  2. If blocked, try to sidestep WEST or EAST within the same lane
 *  3. If still blocked, optionally step NORTH (back) to jiggle
 */
public class ValorMonsterAI {

    private final ValorBoard board;
    private final ValorMovement movement;

    public ValorMonsterAI(ValorBoard board, ValorMovement movement) {
        this.board = board;
        this.movement = movement;
    }

    /** Advance a single monster one step according to the AI rules. */
    public void advanceMonster(Monster monster) {
        if (monster == null) return;

        // 1) Try straight south first
        if (movement.moveMonster(monster, ValorDirection.SOUTH)) {
            return;
        }

        // 2) If blocked, attempt sideways movement inside the same lane
        int[] pos = movement.findMonster(monster);
        if (pos == null) return;

        int col = pos[1];
        int lane = board.getLane(col);
        if (lane == -1) return; // not on a lane (shouldn't happen for lane monsters)

        List<ValorDirection> sides = new ArrayList<>();
        sides.add(ValorDirection.WEST);
        sides.add(ValorDirection.EAST);
        Collections.shuffle(sides);   // little randomness so they don't all pick same side

        for (ValorDirection dir : sides) {
            if (movement.moveMonster(monster, dir)) {
                return;
            }
        }

        // 3) Still blocked – tiny jiggle backwards
        movement.moveMonster(monster, ValorDirection.NORTH);
    }
}