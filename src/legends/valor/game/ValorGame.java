package legends.valor.game;

import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;

import legends.game.Game;
import legends.characters.Hero;
import legends.characters.Monster;
import legends.characters.Party;
import legends.data.DataLoader;
import legends.data.MonsterFactory;
import legends.items.Item;
import legends.market.Market;
import legends.valor.world.ValorBoard;
import legends.valor.world.ValorDirection;
import legends.valor.world.ValorMovement;
import legends.valor.combat.ValorCombat;

/**
 * Legends of Valor main game controller.
 *
 * Responsibilities:
 *  - Load data (heroes, monsters, items)
 *  - Let the player select 1–3 heroes
 *  - Place heroes on the Legends of Valor lane board
 *  - Spawn lane monsters at the top Nexus
 *  - Run "rounds": ALL heroes act, then ALL monsters act.
 */
public class ValorGame implements Game {

    // What happened at the end of a single match?
    private enum GameOutcome {
        HERO_WIN,
        MONSTER_WIN,
        QUIT        // player pressed Q or aborted hero selection
    }

    private final Scanner in = new Scanner(System.in);

    private ValorBoard board;
    private ValorMovement movement;
    private ValorCombat combat;

    private Party party;
    private Market market;
    private List<Monster> laneMonsters;

    public ValorGame() {
        // Actual objects will be created per-match in playSingleGame().
        this.board = null;
        this.movement = null;
        this.combat = null;
        this.laneMonsters = new ArrayList<>();
    }

    // =========================================================
    //  PUBLIC ENTRYPOINT
    // =========================================================

    @Override
    public void run() {
        while (true) {
            GameOutcome outcome = playSingleGame();

            // If player quit (Q or no heroes), just go back to main menu.
            if (outcome == GameOutcome.QUIT) {
                System.out.println("Leaving Legends of Valor...");
                return;
            }

            // HERO_WIN or MONSTER_WIN → ask if they want to play another match.
            if (!askPlayAgain()) {
                System.out.println("Returning to main menu...");
                return;
            }
        }
    }

    /**
     * Plays one complete Legends of Valor match:
     *  - Show intro, load data
     *  - Hero selection
     *  - Build board and place heroes & monsters
     *  - Run round loop until win/lose/quit
     */
    private GameOutcome playSingleGame() {
        showIntroScreen();

        // =======================
        // 1. LOAD GAME DATA
        // =======================
        DataLoader loader = new DataLoader();

        // Items for possible future Nexus markets
        List<Item> items = loader.loadAllItems();
        this.market = new Market(items);

        // Make monsters globally available (same pattern as LegendsGame)
        DataLoader.globalMonsters = loader.loadAllMonsters();

        // =======================
        // 2. HERO SELECTION
        // =======================
        System.out.println();
        System.out.println("Now choose your heroes for Legends of Valor...");
        legends.game.HeroSelection selector =
        new legends.game.HeroSelection(loader, 3, 3);   // exactly 3 heroes
        this.party = selector.selectHeroes();

        if (party == null || party.getHeroes().isEmpty()) {
            System.out.println("No heroes selected. Exiting Legends of Valor mode.");
            return GameOutcome.QUIT;
        }

        // =======================
        // 3. BUILD BOARD & SPAWN
        // =======================
        this.board = new ValorBoard();
        this.movement = new ValorMovement(board);
        this.combat = new ValorCombat(board);
        this.laneMonsters = new ArrayList<>();

        placeHeroesOnBoard();
        spawnMonstersOnBoard();

        // =======================
        // 4. MAIN ROUND LOOP
        // =======================
        return roundLoop();
    }

    // ----------------------------------------------------------
    // INTRO
    // ----------------------------------------------------------

    private void showIntroScreen() {
    String RESET   = "\u001B[0m";
    String BOLD    = "\u001B[1m";
    String CYAN    = "\u001B[36m";
    String MAGENTA = "\u001B[35m";
    String YELLOW  = "\u001B[33m";
    String GREEN   = "\u001B[32m";

    System.out.println();
    System.out.println(MAGENTA + "════════════════════════ LEGENDS OF VALOR ════════════════════════" + RESET);
    System.out.println();

    System.out.println("Welcome to " + BOLD + "Legends of Valor" + RESET + ", a 3-lane tactical battle between");
    System.out.println("your party of Heroes and waves of Monsters.");
    System.out.println();

    System.out.println(BOLD + "Goal" + RESET);
    System.out.println(" - March your Heroes up the board and enter the enemy (top) Nexus.");
    System.out.println(" - If any Monster reaches your (bottom) Nexus first, you lose.");
    System.out.println();

    System.out.println(BOLD + "Your Heroes" + RESET);
    System.out.println(" - You choose 1–3 Heroes from the shared Legends roster.");
    System.out.println(" - Each Hero starts at the bottom Nexus, one per lane.");
    System.out.println(" - Special terrain gives a 10% bonus to the Hero standing on it:");
    System.out.println("     " + GREEN + "Bush   " + RESET + "→ +10% Dexterity (better hit chance / accuracy)");
    System.out.println("     " + CYAN  + "Cave   " + RESET + "→ +10% Agility (better dodge)");
    System.out.println("     " + YELLOW+ "Koulou " + RESET + "→ +10% Strength (more attack damage)");
    System.out.println();

    System.out.println(BOLD + "The Monsters" + RESET);
    System.out.println(" - Monsters spawn at the top Nexus, one per lane.");
    System.out.println(" - They only move " + BOLD + "down" + RESET + " toward your Nexus.");
    System.out.println(" - If a Hero is in attack range, a Monster attacks instead of moving.");
    System.out.println();

    System.out.println(BOLD + "Turn Order" + RESET);
    System.out.println(" - Each round:");
    System.out.println("     1. All Heroes act once (move or attack).");
    System.out.println("     2. All Monsters act once (move or attack).");
    System.out.println(" - Attacks can hit targets on the same tile or in a tile directly");
    System.out.println("   above, below, left, or right of the attacker.");
    System.out.println();

    System.out.println(BOLD + "Controls" + RESET);
    System.out.println(" - W = move up (toward enemy Nexus)");
    System.out.println(" - A = move left");
    System.out.println(" - S = move down");
    System.out.println(" - D = move right");
    System.out.println(" - F = basic attack (choose a monster in range)");
    System.out.println(" - N = wait / skip this Hero's action");
    System.out.println(" - Q = quit Legends of Valor and return to the menu");
    System.out.println();

    System.out.println("Press " + BOLD + "ENTER" + RESET + " to begin your march to the enemy Nexus...");
    in.nextLine();
}

    // ----------------------------------------------------------
    // HERO SPAWN LOGIC (LoV lanes)
    // ----------------------------------------------------------

    private void placeHeroesOnBoard() {
        int lane = 0;

        for (Hero h : party.getHeroes()) {
            if (lane >= 3) {
                break; // only 3 lanes in Legends of Valor
            }

            int[] spawn = board.getHeroSpawnCell(lane); // [row, col]
            if (spawn == null || spawn.length < 2) {
                System.out.println("No spawn cell found for lane " + lane);
                lane++;
                continue;
            }

            board.getTile(spawn[0], spawn[1]).placeHero(h);

            System.out.println(
                    h.getName() + " spawned in lane " + lane +
                            " at (" + spawn[0] + "," + spawn[1] + ")"
            );

            lane++;
        }
    }

    // ----------------------------------------------------------
    // MONSTER SPAWN LOGIC (top Nexus)
    // ----------------------------------------------------------

    private void spawnMonstersOnBoard() {
        laneMonsters.clear();

        List<Monster> generated = MonsterFactory.generateMonstersForParty(party);
        if (generated == null || generated.isEmpty()) {
            System.out.println("No monsters generated for Legends of Valor.");
            return;
        }

        int lane = 0;
        for (Monster m : generated) {
            if (lane >= 3) break;   // max 3 lanes

            int[] spawn = board.getMonsterSpawnCell(lane); // [row, col]
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
    }

    // ----------------------------------------------------------
    // ROUND LOOP: ALL HEROES → MONSTERS
    // ----------------------------------------------------------

    private GameOutcome roundLoop() {
        if (party.getHeroes().isEmpty()) {
            System.out.println("Party has no heroes, cannot move.");
            return GameOutcome.QUIT;
        }

        while (true) {
            List<Hero> heroes = party.getHeroes();

            // ===== HERO PHASE =====
            for (int i = 0; i < heroes.size(); i++) {
                Hero activeHero = heroes.get(i);

                if (activeHero.getHP() <= 0) {
                    // later: respawn logic
                    continue;
                }

                boolean turnTaken = false;

                while (!turnTaken) {
                    System.out.println();
                    board.print();

                    System.out.println("Hero " + (i + 1) + " turn: " + activeHero.getName());
                    System.out.println("Controls: W = up | A = left | S = down | D = right | F = attack");
                    System.out.println("          N = wait/skip | Q = quit LoV");
                    System.out.print("Enter command for " + activeHero.getName() + ": ");

                    String line = in.nextLine().trim().toUpperCase();
                    if (line.isEmpty()) continue;

                    char cmd = line.charAt(0);

                    if (cmd == 'Q') {
                        System.out.println("Exiting Legends of Valor match...");
                        return GameOutcome.QUIT;
                    }

                    switch (cmd) {
                        case 'W':
                            if (movement.moveHero(activeHero, ValorDirection.NORTH)) {
                                turnTaken = true;
                            } else {
                                System.out.println("Cannot move there!");
                            }
                            break;
                        case 'S':
                            if (movement.moveHero(activeHero, ValorDirection.SOUTH)) {
                                turnTaken = true;
                            } else {
                                System.out.println("Cannot move there!");
                            }
                            break;
                        case 'A':
                            if (movement.moveHero(activeHero, ValorDirection.WEST)) {
                                turnTaken = true;
                            } else {
                                System.out.println("Cannot move there!");
                            }
                            break;
                        case 'D':
                            if (movement.moveHero(activeHero, ValorDirection.EAST)) {
                                turnTaken = true;
                            } else {
                                System.out.println("Cannot move there!");
                            }
                            break;
                        case 'F':
                            if (handleHeroAttack(activeHero)) {
                                turnTaken = true;
                            }
                            break;
                        case 'N':
                            System.out.println(activeHero.getName() + " waits this turn.");
                            turnTaken = true;
                            break;
                        default:
                            System.out.println("Invalid command.");
                    }
                }

                // After each hero action, check for heroes’ victory condition
                if (board.heroesReachedEnemyNexus()) {
                    System.out.println();
                    board.print();
                    System.out.println("\u001B[92mHeroes have reached the enemy Nexus! YOU WIN!\u001B[0m");
                    return GameOutcome.HERO_WIN;
                }
            }

            // ===== MONSTER PHASE =====
            monstersTurn();

            // After monsters phase, check for monsters’ victory
            if (board.monstersReachedHeroesNexus()) {
                System.out.println();
                board.print();
                System.out.println("\u001B[91mMonsters have reached your Nexus! YOU LOSE!\u001B[0m");
                return GameOutcome.MONSTER_WIN;
            }
        }
    }

    // ----------------------------------------------------------
    // HERO ATTACK / MONSTER TURN
    // ----------------------------------------------------------

    private boolean handleHeroAttack(Hero hero) {
        List<Monster> inRange = combat.getMonstersInRange(hero);

        if (inRange.isEmpty()) {
            System.out.println("No monsters in attack range!");
            return false;
        }

        Monster target;

        if (inRange.size() == 1) {
            target = inRange.get(0);
            System.out.println("Attacking " + target.getName() + "...");
        } else {
            System.out.println("Monsters in range:");
            for (int i = 0; i < inRange.size(); i++) {
                Monster m = inRange.get(i);
                System.out.println((i + 1) + ". " + m.getName()
                        + " (HP: " + (int) m.getHP() + ")");
            }
            System.out.print("Choose target (number): ");

            try {
                int choice = Integer.parseInt(in.nextLine().trim());
                if (choice < 1 || choice > inRange.size()) {
                    System.out.println("Invalid target.");
                    return false;
                }
                target = inRange.get(choice - 1);
            } catch (Exception e) {
                System.out.println("Invalid number.");
                return false;
            }
        }

        boolean killed = combat.heroAttack(hero, target);

        if (killed) {
            laneMonsters.remove(target);
            // later: gold/xp for all heroes according to PDF rules
        }

        return true;  // attack consumes turn
    }

    /**
     * Monsters obey LoV rules:
     *  - If any hero is in attack range → ATTACK (do not move)
     *  - Else → move one tile SOUTH if possible
     */
    private void monstersTurn() {
        System.out.println();
        System.out.println("\u001B[91mMonsters advance toward your Nexus...\u001B[0m");

        for (Monster m : new ArrayList<>(laneMonsters)) {
            if (m.getHP() <= 0) continue;

            // 1) If hero in attack range, attack instead of moving
            List<Hero> heroesInRange = combat.getHeroesInRange(m);
            if (!heroesInRange.isEmpty()) {
                Hero target = heroesInRange.get(0);
                combat.monsterAttack(m, target);
                continue;
            }

            // 2) Otherwise, try to move south (monsters only move down)
            boolean moved = movement.moveMonster(m, ValorDirection.SOUTH);
            if (!moved) {
                // if south is blocked, monster simply waits this round
            }
        }
    }

    // ----------------------------------------------------------
    // PLAY AGAIN PROMPT
    // ----------------------------------------------------------

    private boolean askPlayAgain() {
        while (true) {
            System.out.print("Play Legends of Valor again? (Y/N): ");
            String line = in.nextLine().trim().toUpperCase();

            if (line.startsWith("Y")) {
                return true;
            } else if (line.startsWith("N")) {
                return false;
            } else {
                System.out.println("Please enter Y or N.");
            }
        }
    }

    // ----------------------------------------------------------
    // Getters (for future LoV features)
    // ----------------------------------------------------------

    public ValorBoard getBoard() {
        return board;
    }

    public Party getParty() {
        return party;
    }

    public Market getMarket() {
        return market;
    }

    public List<Monster> getLaneMonsters() {
        return laneMonsters;
    }
}