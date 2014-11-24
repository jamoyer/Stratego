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
    FLAG(TileSymbols.FLAG, -1, 1),
    BOMB(TileSymbols.BOMB, -1, 6),
    SPY(TileSymbols.SPY, -1, 1),
    SCOUT(TileSymbols.SCOUT, 9, 8),
    MINER(TileSymbols.MINER, 8, 5),
    SERGEANT(TileSymbols.SERGEANT, 7, 4),
    LIEUTENANT(TileSymbols.LIEUTENANT, 6, 4),
    CAPTAIN(TileSymbols.CAPTAIN, 5, 4),
    MAJOR(TileSymbols.MAJOR, 4, 3),
    COLONEL(TileSymbols.COLONEL, 3, 2),
    GENERAL(TileSymbols.GENERAL, 2, 1),
    MARSHALL(TileSymbols.MARSHALL, 1, 1);

    private final TileSymbols symbol;
    private final int numUnits;
    private final int rank;

    private UnitType(final TileSymbols symbol, final int rank, final int numUnits)
    {
        this.symbol = symbol;
        this.numUnits = numUnits;
        this.rank = rank;
    }

    public TileSymbols getSymbol()
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
