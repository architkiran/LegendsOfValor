package legends.valor.game;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import legends.characters.Hero;
import legends.characters.Monster;
import legends.characters.Party;
import legends.data.DataLoader;
import legends.items.Item;
import legends.market.Market;
import legends.stats.GameStats;
import legends.valor.combat.ValorCombat;
import legends.valor.world.ValorBoard;
import legends.valor.world.ValorMovement;

public class ValorMatchSetup {

    public boolean setup(ValorMatch match) {
        // 1) LOAD DATA
        DataLoader loader = new DataLoader();
        List<Item> items = loader.loadAllItems();
        Market market = new Market(items);
        DataLoader.globalMonsters = loader.loadAllMonsters();

        // 2) HERO SELECTION (EXACTLY 3)
        System.out.println();
        System.out.println("Now choose your heroes for Legends of Valor...");
        legends.game.HeroSelection selector = new legends.game.HeroSelection(loader, 3, 3);
        Party party = selector.selectHeroes();

        if (party == null || party.getHeroes().isEmpty()) {
            System.out.println("No heroes selected. Exiting Legends of Valor mode.");
            return false;
        }

        // 3) BUILD BOARD & SYSTEMS
        ValorBoard board = new ValorBoard();
        ValorMovement movement = new ValorMovement(board);

        GameStats stats = new GameStats(GameStats.GameMode.LEGENDS_OF_VALOR, party.getHeroes());
        ValorCombat combat = new ValorCombat(board, stats);

        // 4) LANE SELECTION + SPAWN
        // (uses a local Scanner; HeroSelection also uses its own Scanner, so this is consistent)
        Scanner in = new Scanner(System.in);

        ValorLaneSelector laneSelector = new ValorLaneSelector(in);
        Map<Hero, Integer> lanes = laneSelector.chooseLanes(party);

        ValorSpawner spawner = new ValorSpawner(board);
        spawner.placeHeroesOnBoard(party, lanes);

        List<Monster> laneMonsters = spawner.spawnLaneMonsters(party);

        // 5) PUSH INTO MATCH STATE
        match.setMarket(market);
        match.setParty(party);
        match.setBoard(board);
        match.setMovement(movement);
        match.setGameStats(stats);
        match.setCombat(combat);
        match.setLaneMonsters(laneMonsters);

        return true;
    }
}