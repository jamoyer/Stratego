package stratego.model;

/**
 * Enum for unit types. Each unit has a symbol which is sent to the front end to
 * be interpreted as a unit, a rank that helps determine victories in combat and
 * a number of units each player starts the game with.
 * 
 * @author Jacob Moyer
 *
 */
public enum UnitType
{
    FLAG('F', -1, 1),
    BOMB('B', -1, 6),
    SPY('S', -1, 1),
    SCOUT('9', 9, 8),
    MINER('8', 8, 5),
    SERGEANT('7', 7, 4),
    LIEUTENANT('6', 6, 4),
    CAPTAIN('5', 5, 4),
    MAJOR('4', 4, 3),
    COLONEL('3', 3, 2),
    GENERAL('2', 2, 1),
    MARSHALL('1', 1, 1);

    private final char symbol;
    private final int numUnits;
    private final int rank;

    private UnitType(final char symbol, final int rank, final int numUnits)
    {
        this.symbol = symbol;
        this.numUnits = numUnits;
        this.rank = rank;
    }

    public char getSymbol()
    {
        return this.symbol;
    }

    public int getNumUnits()
    {
        return this.numUnits;
    }

    public int getRank()
    {
        return this.rank;
    }
}
