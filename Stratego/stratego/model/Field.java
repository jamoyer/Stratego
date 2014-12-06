package stratego.model;

public class Field
{
    private static final int ROWS = 10;
    private static final int COLUMNS = 10;
    private static final int STARTING_PLAYER_ROWS = 4;
    private static final int BOTTOM_PLAYER_ROW_OFFSET = 6;

    private final Tile[][] grid;

    public Field()
    {
        this.grid = new Tile[ROWS][COLUMNS];

        // initialize new battlefield
        for (int i = 0; i < ROWS; i++)
        {
            for (int j = 0; j < COLUMNS; j++)
            {
                Position pos = new Position(i, j);

                // make obstacles for stratego field
                if ((i == 4 || i == 5) && (j == 2 || j == 3 || j == 6 || j == 7))
                {
                    this.grid[i][j] = new Tile(pos, true);
                }
                else
                {
                    this.grid[i][j] = new Tile(pos, false);
                }
            }
        }
    }

    private Tile getTileAt(final Position pos)
    {
        return this.grid[pos.getRow()][pos.getColumn()];
    }

    public boolean isObstacle(final Position pos)
    {
        return this.getTileAt(pos).isObstacle();
    }

    public Unit getUnitAt(final Position pos)
    {
        return this.getTileAt(pos).getUnit();
    }

    public void setUnitAt(final Position pos, final Unit unit)
    {
        this.getTileAt(pos).setUnit(unit);
    }

    public static int getRowCount()
    {
        return ROWS;
    }

    public static int getColumnCount()
    {
        return COLUMNS;
    }

    public static int getStartingPlayerRowCount()
    {
        return STARTING_PLAYER_ROWS;
    }

    public static int getBottomPlayerRowOffset()
    {
        return BOTTOM_PLAYER_ROW_OFFSET;
    }
}
