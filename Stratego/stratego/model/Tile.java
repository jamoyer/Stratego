package stratego.model;

/**
 * Represents a tile of the field. Tiles have a position on the field and if the
 * tile is not an obstacle then it may have a unit.
 * 
 * @author Jacob Moyer
 *
 */
public class Tile
{
    private Unit unit;
    private final Position pos;
    private final boolean obstacle;

    public Tile(final Position pos, final boolean obstacle)
    {
        this(pos, obstacle, null);
    }

    public Tile(final Position pos, final boolean obstacle, final Unit unit)
    {
        this.pos = pos;
        this.obstacle = obstacle;
        this.setUnit(unit);
    }

    public Unit getUnit()
    {
        return this.unit;
    }

    public final void setUnit(final Unit unit)
    {
        if (this.obstacle && unit != null)
        {
            throw new IllegalArgumentException("Only playable tiles may have a unit.");
        }
        this.unit = unit;
    }

    public Position getPosition()
    {
        return this.pos;
    }

    public boolean isObstacle()
    {
        return this.obstacle;
    }
}
