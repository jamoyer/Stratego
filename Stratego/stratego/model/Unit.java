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
    private final int player;

    public Unit(final UnitType type, final int player)
    {
        if (player != 1 || player != 2)
        {
            throw new IllegalArgumentException("Player must be 1 or 2.");
        }
        this.player = player;
        this.type = type;
    }

    public int getPlayer()
    {
        return this.player;
    }

    public UnitType getType()
    {
        return this.type;
    }
}
