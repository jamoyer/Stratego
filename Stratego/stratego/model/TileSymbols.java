package stratego.model;

public enum TileSymbols
{
    OBSTACLE('%'),
    EMPTY(' '),
    ENEMY_COVERED('E'),
    ENEMY_FLAG('!'),
    ENEMY_BOMB('*'),
    ENEMY_SPY('s'),
    ENEMY_SCOUT('i'),
    ENEMY_MINER('h'),
    ENEMY_SERGEANT('g'),
    ENEMY_LIEUTENANT('f'),
    ENEMY_CAPTAIN('e'),
    ENEMY_MAJOR('d'),
    ENEMY_COLONEL('c'),
    ENEMY_GENERAL('b'),
    ENEMY_MARSHALL('a'),
    FLAG('F'),
    BOMB('B'),
    SPY('S'),
    SCOUT('9'),
    MINER('8'),
    SERGEANT('7'),
    LIEUTENANT('6'),
    CAPTAIN('5'),
    MAJOR('4'),
    COLONEL('3'),
    GENERAL('2'),
    MARSHALL('1');

    private final char symbol;

    private TileSymbols(final char symbol)
    {
        this.symbol = symbol;
    }

    public char getSymbol()
    {
        return this.symbol;
    }
}
