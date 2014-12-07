package stratego.model;

import java.util.HashMap;

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
    FLAG(TileSymbol.FLAG, -1, 1),
    BOMB(TileSymbol.BOMB, -1, 6),
    SPY(TileSymbol.SPY, 10, 1),
    SCOUT(TileSymbol.SCOUT, 9, 8),
    MINER(TileSymbol.MINER, 8, 5),
    SERGEANT(TileSymbol.SERGEANT, 7, 4),
    LIEUTENANT(TileSymbol.LIEUTENANT, 6, 4),
    CAPTAIN(TileSymbol.CAPTAIN, 5, 4),
    MAJOR(TileSymbol.MAJOR, 4, 3),
    COLONEL(TileSymbol.COLONEL, 3, 2),
    GENERAL(TileSymbol.GENERAL, 2, 1),
    MARSHALL(TileSymbol.MARSHALL, 1, 1);

    private final TileSymbol symbol;
    private final int numUnits;
    private final int rank;

    // symbol map is used for easy conversion of characters to UnitTypes
    private static HashMap<TileSymbol, UnitType> _unitMap = null;

    private UnitType(final TileSymbol symbol, final int rank, final int numUnits)
    {
        this.symbol = symbol;
        this.numUnits = numUnits;
        this.rank = rank;
    }

    public TileSymbol getSymbol()
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

    /**
     * Returns the singleton instance of the symbolMap.
     * 
     * @return
     */
    private static HashMap<TileSymbol, UnitType> getUnitMap()
    {
        if (_unitMap == null)
        {
            synchronized (UnitType.class)
            {
                if (_unitMap == null)
                {
                    _unitMap = new HashMap<TileSymbol, UnitType>(UnitType.values().length);
                    for (UnitType e : UnitType.values())
                    {
                        _unitMap.put(e.getSymbol(), e);
                    }
                }
            }
        }
        return _unitMap;
    }

    /**
     * Returns the unit corresponding to the character value.
     * 
     * @param value
     * @return
     */
    public static UnitType getUnitByChar(final char value)
    {
        return getUnitMap().get(TileSymbol.getSymbolByChar(value));
    }

    /**
     * Returns the unit corresponding to the tile.
     * 
     * @param value
     * @return
     */
    public static UnitType getUnitByTile(final TileSymbol tile)
    {
        return getUnitMap().get(tile);
    }
}
