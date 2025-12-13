package legends.valor.turn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import legends.characters.Hero;
import legends.characters.Monster;
import legends.characters.Party;
import legends.valor.combat.ValorCombat;
import legends.valor.game.ValorMatch;
import legends.valor.game.ValorMonsterAI;
import legends.valor.world.ValorBoard;
import legends.valor.world.ValorMovement;

public class ValorTurnManager {

    private final ValorBoard board;
    private final ValorMovement movement;
    private final ValorCombat combat;

    private final Party party;
    private final List<Monster> laneMonsters;

    private final HeroTurnController heroTurnController;
    private final MonsterTurnController monsterTurnController;

    // hero -> home lane for respawn/recall
    private final Map<Hero, Integer> homeLane = new HashMap<>();

    public ValorTurnManager(ValorBoard board,
                            ValorMovement movement,
                            ValorCombat combat,
                            Party party,
                            List<Monster> laneMonsters,
                            ValorInput input) {
        this.board = board;
        this.movement = movement;
        this.combat = combat;
        this.party = party;
        this.laneMonsters = laneMonsters;

        // IMPORTANT: your HeroTurnController constructor is 5 args
        this.heroTurnController = new HeroTurnController(board, movement, combat, laneMonsters, input, homeLane);
        this.monsterTurnController = new MonsterTurnController(combat, new ValorMonsterAI(board, movement));}

    /**
     * @return Outcome if match ended this round, else null to continue.
     */
    public ValorMatch.Outcome playOneRound() {
        List<Hero> heroes = party.getHeroes();
        if (heroes == null || heroes.isEmpty()) return ValorMatch.Outcome.QUIT;

        // HERO PHASE
        for (int i = 0; i < heroes.size(); i++) {
            Hero h = heroes.get(i);
            if (h == null) continue;

            bindHomeLaneIfMissing(h);

            if (h.getHP() <= 0) continue;

            boolean ok = heroTurnController.handleHeroTurn(h, i + 1);
            if (!ok) return ValorMatch.Outcome.QUIT;

            if (board.heroesReachedEnemyNexus()) {
                return ValorMatch.Outcome.HERO_WIN;
            }
        }

        // MONSTER PHASE
        monsterTurnController.monstersPhase(laneMonsters);

        if (board.monstersReachedHeroesNexus()) {
            return ValorMatch.Outcome.MONSTER_WIN;
        }

        // END OF ROUND: respawn dead heroes (full HP/MP by level formula) + back to nexus
        respawnDeadHeroes(heroes);

        return null;
    }

    // =========================================================
    // Helpers
    // =========================================================

    private void bindHomeLaneIfMissing(Hero h) {
        if (h == null) return;
        if (homeLane.containsKey(h)) return;

        int[] pos = movement.findHero(h);
        if (pos == null) return;          // hero not placed yet
        int lane = board.getLane(pos[1]); // lane by column
        if (lane >= 0) homeLane.put(h, lane);
    }

    /**
     * Respawn rule (based on your project rule):
     *   maxHP = level * 100
     *   maxMP = level * 50
     *
     * When HP<=0 at round end:
     * - Set HP to maxHP
     * - Set MP to maxMP
     * - Teleport to hero's home nexus lane spawn
     */
    private void respawnDeadHeroes(List<Hero> heroes) {
        for (Hero h : heroes) {
            if (h == null) continue;
            if (h.getHP() > 0) continue;

            int lvl = h.getLevel();
            double maxHP = lvl * 100.0;
            double maxMP = lvl * 50.0;

            h.setHP(maxHP);
            h.setMP(maxMP);

            teleportHeroToHomeNexus(h);
        }
    }

    private void teleportHeroToHomeNexus(Hero hero) {
        if (hero == null) return;

        // Remove from current tile if still on board
        int[] cur = movement.findHero(hero);
        if (cur != null) {
            board.getTile(cur[0], cur[1]).removeHero();
        }

        Integer lane = homeLane.get(hero);
        if (lane == null) lane = 1; // fallback mid lane

        // Heroes spawn in left nexus cell of their lane
        int[] spawn = board.getHeroSpawnCell(lane);
        int r = spawn[0];
        int c = spawn[1];

        // If occupied, try the other nexus column
        if (!board.canHeroEnter(r, c)) {
            int[] cols = board.getNexusColumnsForLane(lane);
            if (cols != null && cols.length == 2 && board.canHeroEnter(r, cols[1])) {
                c = cols[1];
            }
        }

        // Place if legal
        if (board.canHeroEnter(r, c)) {
            board.getTile(r, c).placeHero(hero);
        }
    }
}
