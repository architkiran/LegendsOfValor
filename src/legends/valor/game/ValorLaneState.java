package legends.valor.game;

import java.util.List;

import legends.characters.Hero;
import legends.characters.Monster;
import legends.characters.Party;
import legends.valor.world.ValorBoard;
import legends.valor.world.ValorDirection;
import legends.valor.world.ValorMovement;

/**
 * Main lane state:
 *  - Shows the Valor board
 *  - Lets heroes move with W/A/S/D
 *  - N cycles to next hero
 *  - After a successful hero move, monsters advance toward heroes' Nexus
 */
public class ValorLaneState implements ValorState {

    private final ValorGame game;
    private final ValorBoard board;
    private final ValorMovement movement;
    private final Party party;
    private final List<Monster> laneMonsters;
    private final ValorMonsterAI monsterAI;

    private int heroIndex = 0;
    private boolean finished = false;

    public ValorLaneState(ValorGame game,
                          ValorBoard board,
                          ValorMovement movement,
                          Party party,
                          List<Monster> laneMonsters,
                          ValorMonsterAI monsterAI) {
        this.game = game;
        this.board = board;
        this.movement = movement;
        this.party = party;
        this.laneMonsters = laneMonsters;
        this.monsterAI = monsterAI;
    }

    private Hero getActiveHero() {
        List<Hero> heroes = party.getHeroes();
        if (heroes.isEmpty())
            throw new IllegalStateException("No heroes in party.");
        if (heroIndex < 0 || heroIndex >= heroes.size())
            heroIndex = 0;
        return heroes.get(heroIndex);
    }

    @Override
    public void render() {
        board.print();   // your pretty white-grid LoV board

        Hero active = getActiveHero();
        System.out.println("Controls: W = up | A = left | S = down | D = right |");
        System.out.println("          N = next hero | Q = quit LoV");
        System.out.print("Enter command for " + active.getName() + ": ");
    }

    @Override
    public void handleInput(String input) {
        if (input == null) return;
        input = input.trim().toUpperCase();
        if (input.isEmpty()) return;

        Hero activeHero = getActiveHero();
        char cmd = input.charAt(0);

        switch (cmd) {
            case 'Q':
                finished = true;
                System.out.println("Exiting Legends of Valor...");
                return;

            case 'N':
                heroIndex = (heroIndex + 1) % party.getHeroes().size();
                return;

            case 'W': case 'A': case 'S': case 'D':
                boolean moved = handleHeroMovement(activeHero, cmd);
                if (!moved) {
                    System.out.println("Cannot move there!");
                } else {
                    doMonstersTurn();
                }
                return;

            default:
                System.out.println("Invalid command.");
        }
    }

    private boolean handleHeroMovement(Hero hero, char cmd) {
        return switch (cmd) {
            case 'W' -> movement.moveHero(hero, ValorDirection.NORTH);
            case 'S' -> movement.moveHero(hero, ValorDirection.SOUTH);
            case 'A' -> movement.moveHero(hero, ValorDirection.WEST);
            case 'D' -> movement.moveHero(hero, ValorDirection.EAST);
            default -> false;
        };
    }

    private void doMonstersTurn() {
        if (laneMonsters == null || laneMonsters.isEmpty()) return;

        System.out.println("\n\u001B[31mMonsters advance toward your Nexus...\u001B[0m\n");

        for (Monster m : laneMonsters) {
            if (m.getHP() <= 0) continue;   // dead monster, ignore
            monsterAI.advanceMonster(m);
        }
    }

    @Override
    public void update(ValorGame game) {
        // Future: check for win/lose, respawn, combat triggers, etc.
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}