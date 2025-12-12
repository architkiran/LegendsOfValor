package legends.valor.game;

import java.util.ArrayList;
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
 *  - Spawn monsters on top Nexus, 1 per lane
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
            if (lane >= 3) break;   // only 3 lanes

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
     * Generates monsters based on party strength and places
     * up to one monster per lane on the monsters' Nexus (top row).
     *
     * @return list of lane monsters that were spawned.
     */
    public List<Monster> spawnLaneMonsters(Party party) {
        List<Monster> laneMonsters = new ArrayList<>();

        List<Monster> generated = MonsterFactory.generateMonstersForParty(party);
        if (generated == null || generated.isEmpty()) {
            System.out.println("No monsters generated for Legends of Valor.");
            return laneMonsters;
        }

        int lane = 0;
        for (Monster m : generated) {
            if (lane >= 3) break;   // max 3 lanes

            int[] spawn = board.getMonsterSpawnCell(lane);   // [row, col]
            if (spawn == null || spawn.length < 2) {
                lane++;
                continue;
            }

            board.getTile(spawn[0], spawn[1]).placeMonster(m);
            laneMonsters.add(m);

            System.out.println(
                m.getName() + " spawned in lane " + lane +
                " at (" + spawn[0] + "," + spawn[1] + ")"
            );

            lane++;
        }

        return laneMonsters;
    }
}