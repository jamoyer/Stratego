package stratego.model;

/**
 * Represents a unit in the battlefield. Units have a UnitType and are
 * controlled by either player 1 or 2.
 * 
 * @author root
 *
 */
public class Unit
{
    private final UnitType type;
    private final PlayerPosition player;

    public Unit(final UnitType type, final PlayerPosition player)
    {
        this.player = player;
        this.type = type;
    }

    public PlayerPosition getPlayer()
    {
        return this.player;
    }

    public UnitType getType()
    {
        return this.type;
    }

    public TileSymbol getEnemyType()
    {
        switch (this.type)
        {
            case BOMB:
                return TileSymbol.ENEMY_BOMB;
            case CAPTAIN:
                return TileSymbol.ENEMY_CAPTAIN;
            case COLONEL:
                return TileSymbol.ENEMY_COLONEL;
            case FLAG:
                return TileSymbol.ENEMY_FLAG;
            case GENERAL:
                return TileSymbol.ENEMY_GENERAL;
            case LIEUTENANT:
                return TileSymbol.ENEMY_LIEUTENANT;
            case MAJOR:
                return TileSymbol.ENEMY_MAJOR;
            case MARSHALL:
                return TileSymbol.ENEMY_MARSHALL;
            case MINER:
                return TileSymbol.ENEMY_MINER;
            case SCOUT:
                return TileSymbol.ENEMY_SCOUT;
            case SERGEANT:
                return TileSymbol.ENEMY_SERGEANT;
            case SPY:
                return TileSymbol.ENEMY_SPY;
            default:
                return null;
        }
    }
}
