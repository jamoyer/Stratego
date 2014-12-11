package stratego.model;

import java.util.HashMap;

public enum TileSymbol
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

    // symbol map is used for easy conversion of characters to TileSymbols
    private static HashMap<Character, TileSymbol> _symbolMap = null;

    private TileSymbol(final char symbol)
    {
        this.symbol = symbol;
    }

    /**
     * Returns the character associated with this symbol.
     * 
     * @return
     */
    public char getSymbol()
    {
        return this.symbol;
    }

    /**
     * Returns the singleton instance of the symbolMap.
     * 
     * @return
     */
    private static HashMap<Character, TileSymbol> getSymbolMap()
    {
        if (_symbolMap == null)
        {
            synchronized (TileSymbol.class)
            {
                if (_symbolMap == null)
                {
                    _symbolMap = new HashMap<Character, TileSymbol>(TileSymbol.values().length);
                    for (TileSymbol e : TileSymbol.values())
                    {
                        _symbolMap.put(e.getSymbol(), e);
                    }
                }
            }
        }
        return _symbolMap;
    }

    /**
     * Returns the tile corresponding to the character value.
     * 
     * @param value
     * @return
     */
    public static TileSymbol getSymbolByChar(final char value)
    {
        return getSymbolMap().get(value);
    }
}
