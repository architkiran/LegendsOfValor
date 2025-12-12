package legends.valor.turn;

import java.util.List;

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

        this.heroTurnController = new HeroTurnController(board, movement, combat, laneMonsters, input);
        this.monsterTurnController = new MonsterTurnController(combat, new ValorMonsterAI(board, movement));
    }

    /**
     * @return Outcome if match ended this round, else null to continue.
     */
    public ValorMatch.Outcome playOneRound() {
        List<Hero> heroes = party.getHeroes();
        if (heroes.isEmpty()) return ValorMatch.Outcome.QUIT;

        // HERO PHASE
        for (int i = 0; i < heroes.size(); i++) {
            Hero h = heroes.get(i);
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

        return null;
    }
}