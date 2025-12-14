package legends.valor.world.terrain;

import legends.valor.world.ValorCellType;

public class TerrainFactory {

    private TerrainFactory() {
        // prevent instantiation
    }

    /**
     * Creates a Terrain instance only for bonus-bearing cell types.
     * Returns null for all other cell types.
     */
    public static Terrain create(ValorCellType type) {
        if (type == null) {
            return null;
        }

        switch (type) {
            case BUSH:
                return new BushTerrain();
            case CAVE:
                return new CaveTerrain();
            case KOULOU:
                return new KoulouTerrain();
            default:
                return null; // Plain, Inaccessible, Obstacle, Nexus
        }
    }
}