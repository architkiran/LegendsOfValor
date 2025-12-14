package legends.valor.world;

public enum ValorCellType {

    NEXUS('N'),
    INACCESSIBLE('X'),
    OBSTACLE('O'),
    PLAIN(' '),
    BUSH('B'),
    CAVE('C'),
    KOULOU('K');

    private final char symbol;

    ValorCellType(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }

    public boolean isAccessible() {
        return this != INACCESSIBLE && this != OBSTACLE;
    }

    public boolean hasTerrainBonus() {
        return this == BUSH || this == CAVE || this == KOULOU;
    }
}