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
        for (int row = 0; row < ROWS; row++)
        {
            for (int col = 0; col < COLUMNS; col++)
            {
                Position pos = new Position(row, col);

                // make obstacles for stratego field
                if ((row == 4 || row == 5) && (col == 2 || col == 3 || col == 6 || col == 7))
                {
                    this.grid[row][col] = new Tile(pos, true);
                }
                else
                {
                    this.grid[row][col] = new Tile(pos, false);
                }
            }
        }
    }

    public static boolean checkPositionIsWithinBounds(final Position pos)
    {
        int row = pos.getRow();
        int col = pos.getColumn();
        return (row >= 0 && row <= ROWS - 1) && (col >= 0 && col <= COLUMNS - 1);
    }

    private Tile getTileAt(final Position pos)
    {
        return this.grid[pos.getRow()][pos.getColumn()];
    }

    public boolean isObstacle(final Position pos)
    {
        return this.getTileAt(pos).isObstacle();
    }

    public boolean isEmpty(final Position pos)
    {
        return (this.getUnitAt(pos) == null) && !this.isObstacle(pos);
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
