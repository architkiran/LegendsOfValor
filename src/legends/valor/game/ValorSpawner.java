package legends.valor.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import legends.characters.Hero;
import legends.characters.Monster;
import legends.characters.Party;
import legends.data.MonsterFactory;
import legends.valor.world.ValorBoard;
import legends.valor.world.ValorTile;

/**
 * Handles all spawn logic for LoV:
 *  - Place heroes on bottom Nexus, 1 per lane
 *  - Spawn monsters on top Nexus, 1 per lane (every few rounds)
 */
public class ValorSpawner {

    private final ValorBoard board;

    public ValorSpawner(ValorBoard board) {
        this.board = board;
    }

    /** Places up to 3 heroes (one per lane) on the heroes' Nexus row. */
    public void placeHeroesOnBoard(Party party) {
        int lane = 0;

        for (Hero h : party.getHeroes()) {
            if (lane >= 3) break;

            int[] spawn = board.getHeroSpawnCell(lane);   // [row, col]
            if (spawn == null || spawn.length < 2) {
                System.out.println("No spawn cell found for lane " + lane);
                lane++;
                continue;
            }

            ValorTile tile = board.getTile(spawn[0], spawn[1]);
            tile.placeHero(h);

            System.out.println(
                    h.getName() + " spawned in lane " + lane +
                            " at (" + spawn[0] + "," + spawn[1] + ")"
            );

            lane++;
        }
    }

    /**
     * Spawn up to 3 monsters total, ideally 1 per lane.
     * - Uses MonsterFactory.generateMonstersForParty(party)
     * - Shuffles the generated list so we don't always take the "first 3"
     * - Skips a lane if its monster nexus cell is already occupied
     */
    public List<Monster> spawnLaneMonsters(Party party) {
        List<Monster> laneMonsters = new ArrayList<>();

        List<Monster> generated = MonsterFactory.generateMonstersForParty(party);
        if (generated == null || generated.isEmpty()) {
            System.out.println("No monsters generated for Legends of Valor.");
            return laneMonsters;
        }

        // Shuffle so spawn is not always the same first-3
        Collections.shuffle(generated);

        // We try to place exactly one per lane (0,1,2), up to 3 monsters total.
        // If a lane's nexus cell already has a monster, we skip that lane.
        int genIdx = 0;
        for (int lane = 0; lane < 3; lane++) {

            int[] spawn = board.getMonsterSpawnCell(lane); // [row, col]
            if (spawn == null || spawn.length < 2) continue;

            ValorTile tile = board.getTile(spawn[0], spawn[1]);
            if (tile.getMonster() != null) {
                // lane already occupied -> no spawn this lane
                continue;
            }

            // Find next available monster from generated list
            while (genIdx < generated.size() && generated.get(genIdx) == null) {
                genIdx++;
            }
            if (genIdx >= generated.size()) break;

            Monster m = generated.get(genIdx++);
            tile.placeMonster(m);
            laneMonsters.add(m);

            System.out.println(
                    m.getName() + " spawned in lane " + lane +
                            " at (" + spawn[0] + "," + spawn[1] + ")"
            );

            // stop if we already spawned 3 total (safety)
            if (laneMonsters.size() >= 3) break;
        }

        return laneMonsters;
    }
}
