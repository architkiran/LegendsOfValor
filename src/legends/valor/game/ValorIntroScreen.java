package legends.valor.game;

import java.util.Scanner;

import static legends.ui.ConsoleUI.*;

public class ValorIntroScreen {

    private final Scanner in;

    public ValorIntroScreen(Scanner in) {
        this.in = in;
    }

    public void show() {
        System.out.println();
        System.out.println(MAGENTA + "════════════════════════ LEGENDS OF VALOR ════════════════════════" + RESET);
        System.out.println();

        System.out.println("Welcome to " + BOLD + "Legends of Valor" + RESET + ", a 3-lane tactical battle between");
        System.out.println("your party of Heroes and waves of Monsters.\n");

        System.out.println(BOLD + "Goal" + RESET);
        System.out.println(" - Enter the enemy (top) Nexus to win.");
        System.out.println(" - If any Monster reaches your (bottom) Nexus, you lose.\n");

        System.out.println(BOLD + "Team Setup" + RESET);
        System.out.println(" - You must select " + BOLD + "exactly 3 Heroes" + RESET + " (one per lane).\n");

        System.out.println(BOLD + "Terrain Bonuses (Heroes only)" + RESET);
        System.out.println(" - " + GREEN + "Bush   " + RESET + "→ +10% Dexterity");
        System.out.println(" - " + CYAN  + "Cave   " + RESET + "→ +10% Agility");
        System.out.println(" - " + YELLOW+ "Koulou " + RESET + "→ +10% Strength\n");

        System.out.println(BOLD + "Turn Order" + RESET);
        System.out.println(" - Each round:");
        System.out.println("     1) Hero 1 acts, Hero 2 acts, Hero 3 acts");
        System.out.println("     2) Monster 1 acts, Monster 2 acts, Monster 3 acts\n");

        System.out.println(BOLD + "Attack Range" + RESET);
        System.out.println(" - Same tile OR one tile N/S/E/W from attacker.\n");

        System.out.println(BOLD + "Controls" + RESET);
        System.out.println(" - W/A/S/D = move");
        System.out.println(" - F = basic attack");
        System.out.println(" - N = wait/skip");
        System.out.println(" - Q = quit to menu\n");

        System.out.println("Press " + BOLD + "ENTER" + RESET + " to begin...");
        in.nextLine();
    }
}