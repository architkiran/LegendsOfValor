package legends.valor.turn;

import java.util.ArrayList;
import java.util.List;

import legends.characters.Hero;
import legends.characters.Monster;
import legends.valor.combat.ValorCombat;
import legends.valor.game.ValorMonsterAI;

import static legends.ui.ConsoleUI.*;

public class MonsterTurnController {

    private final ValorCombat combat;
    private final ValorMonsterAI ai;

    public MonsterTurnController(ValorCombat combat, ValorMonsterAI ai) {
        this.combat = combat;
        this.ai = ai;
    }

    public void monstersPhase(List<Monster> laneMonsters) {
        if (laneMonsters == null || laneMonsters.isEmpty()) return;

        System.out.println();
        System.out.println(RED + "Monsters advance toward your Nexus..." + RESET);

        for (Monster m : new ArrayList<>(laneMonsters)) {
            if (m == null || m.getHP() <= 0) continue;

            List<Hero> heroesInRange = combat.getHeroesInRange(m);
            if (!heroesInRange.isEmpty()) {
                combat.monsterAttack(m, heroesInRange.get(0));
            } else {
                ai.advanceMonster(m);
            }
        }
        combat.flushLogs();
    }
}