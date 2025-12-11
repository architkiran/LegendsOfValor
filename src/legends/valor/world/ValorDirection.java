package legends.valor.world;

/**
 * Cardinal directions for movement in Legends of Valor.
 * North = towards monsters' Nexus (row -1)
 * South = towards heroes' Nexus   (row +1)
 */
public enum ValorDirection {

    NORTH(-1, 0),
    SOUTH(1, 0),
    WEST(0, -1),
    EAST(0, 1);

    private final int dRow;
    private final int dCol;

    ValorDirection(int dRow, int dCol) {
        this.dRow = dRow;
        this.dCol = dCol;
    }

    public int deltaRow() {
        return dRow;
    }

    public int deltaCol() {
        return dCol;
    }
}
