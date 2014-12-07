package stratego.model;

public class GameInstance
{
    private String topPlayer = null;
    private long topPlayerLastResponseTime;
    private boolean topPlayerPositionsSet = false;

    private String bottomPlayer = null;
    private long bottomPlayerLastResponseTime;
    private boolean bottomPlayerPositionsSet = false;

    private final long initiatedTime; // seconds

    private Field field = null;
    private PlayerPosition winner = null;
    private PlayerPosition currentTurn = null;

    // a grid available for showing the players which units are fighting each
    // other during combat
    private char[][] battleRevealGrid = null;

    public GameInstance(final String player, final long currentTimeSeconds)
    {
        this.bottomPlayer = player;
        this.initiatedTime = currentTimeSeconds;
    }

    public void setBattleRevealGrid(final char[][] grid)
    {
        synchronized (this)
        {
            this.battleRevealGrid = grid;
        }
    }

    public char[][] getBattleRevealGrid()
    {
        synchronized (this)
        {
            return this.battleRevealGrid;
        }
    }

    public synchronized void setTurn(final PlayerPosition userPos)
    {
        this.currentTurn = userPos;
    }

    public PlayerPosition getTurn()
    {
        return this.currentTurn;
    }

    public long checkPlayerLastResponsetime(final String user)
    {
        return this.checkPlayerLastResponsetime(this.getPlayerPosition(user));
    }

    public long checkPlayerLastResponsetime(final PlayerPosition userPos)
    {
        if (userPos == null)
        {
            throw new IllegalArgumentException("userPos cannot be null");
        }

        if (userPos.equals(PlayerPosition.TOP_PLAYER))
        {
            return this.topPlayerLastResponseTime;
        }
        else
        {
            return this.bottomPlayerLastResponseTime;
        }
    }

    public void setPlayerLastResponseTime(final String user, final long responseTime)
    {
        this.setPlayerLastResponseTime(this.getPlayerPosition(user), responseTime);
    }

    public void setPlayerLastResponseTime(final PlayerPosition userPos, final long responseTime)
    {
        if (userPos == null)
        {
            return;
        }

        if (userPos.equals(PlayerPosition.TOP_PLAYER))
        {
            this.topPlayerLastResponseTime = responseTime;
            return;
        }

        if (userPos.equals(PlayerPosition.BOTTOM_PLAYER))
        {
            this.bottomPlayerLastResponseTime = responseTime;
            return;
        }
    }

    public void setPlayerStartingPositions(final String user)
    {
        this.setPlayerHasStartingPositions(this.getPlayerPosition(user));
    }

    public void setPlayerHasStartingPositions(final PlayerPosition userPos)
    {
        if (userPos == null)
        {
            return;
        }

        if (userPos.equals(PlayerPosition.TOP_PLAYER))
        {
            this.topPlayerPositionsSet = true;
            return;
        }

        if (userPos.equals(PlayerPosition.BOTTOM_PLAYER))
        {
            this.bottomPlayerPositionsSet = true;
            return;
        }
    }

    public boolean checkPlayerHasSetPositions(final String user)
    {
        return this.checkPlayerHasSetPositions(this.getPlayerPosition(user));
    }

    public boolean checkPlayerHasSetPositions(final PlayerPosition userPos)
    {
        if (userPos == null)
        {
            return false;
        }

        if (userPos.equals(PlayerPosition.TOP_PLAYER))
        {
            return this.topPlayerPositionsSet;
        }

        if (userPos.equals(PlayerPosition.BOTTOM_PLAYER))
        {
            return this.bottomPlayerPositionsSet;
        }

        return false;
    }

    public Field getField()
    {
        return field;
    }

    public char[][] getFieldSymbolsByPlayer(final String user)
    {
        return this.getFieldSymbolsByPlayer(this.getPlayerPosition(user));
    }

    /**
     * Returns the grid of characters to display for a given player.
     * 
     * @param userPos
     * @return
     */
    public char[][] getFieldSymbolsByPlayer(final PlayerPosition userPos)
    {
        char grid[][] = new char[Field.getRowCount()][Field.getColumnCount()];
        for (int row = 0; row < Field.getRowCount(); row++)
        {
            for (int col = 0; col < Field.getColumnCount(); col++)
            {
                Position pos = new Position(row, col);
                if (this.field.isObstacle(pos))
                {
                    grid[row][col] = TileSymbol.OBSTACLE.getSymbol();
                    continue;
                }

                Unit unit = this.field.getUnitAt(pos);
                if (unit == null)
                {
                    grid[row][col] = TileSymbol.EMPTY.getSymbol();
                    continue;
                }
                if (unit.getPlayer().equals(userPos))
                {
                    grid[row][col] = unit.getType().getSymbol().getSymbol();
                    continue;
                }
                else
                {
                    grid[row][col] = TileSymbol.ENEMY_COVERED.getSymbol();
                    continue;
                }
            }
        }
        return grid;
    }

    public String getWinnerName()
    {
        switch (this.winner)
        {
            case BOTTOM_PLAYER:
                return this.bottomPlayer;
            case TOP_PLAYER:
                return this.topPlayer;
            default:
                return null;
        }
    }

    public PlayerPosition getWinner()
    {
        return this.winner;
    }

    public void setWinner(final PlayerPosition position)
    {
        this.winner = position;
    }

    public String getOpponent(final String user)
    {
        return this.getOpponent(this.getPlayerPosition(user));
    }

    public String getOpponent(final PlayerPosition userPos)
    {
        if (userPos == null)
        {
            return null;
        }

        if (userPos.equals(PlayerPosition.BOTTOM_PLAYER))
        {
            return this.topPlayer;
        }
        return this.bottomPlayer;
    }

    public PlayerPosition getPlayerPosition(final String user)
    {
        if (user == null)
        {
            return null;
        }

        if (user.equals(this.topPlayer))
        {
            return PlayerPosition.TOP_PLAYER;
        }

        if (user.equals(this.bottomPlayer))
        {
            return PlayerPosition.BOTTOM_PLAYER;
        }
        return null;
    }

    public String getPlayer(final PlayerPosition userPos)
    {
        if (userPos == null)
        {
            return null;
        }

        if (userPos.equals(PlayerPosition.TOP_PLAYER))
        {
            return this.topPlayer;
        }

        if (userPos.equals(PlayerPosition.BOTTOM_PLAYER))
        {
            return this.bottomPlayer;
        }
        return null;
    }

    public long getInitiatedTimeSeconds()
    {
        return this.initiatedTime;
    }

    public String getBottomPlayer()
    {
        return this.bottomPlayer;
    }

    /**
     * Setting the top player means that both players have joined and the game
     * is able to start.
     * 
     * @param player
     */
    public void setTopPlayer(final String player)
    {
        this.topPlayer = player;

        // now we know the game will start, create the new field.
        this.field = new Field();
    }

    public String getTopPlayer()
    {
        return this.topPlayer;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (obj == this)
        {
            return true;
        }
        if (!(obj instanceof GameInstance))
        {
            return false;
        }
        GameInstance other = (GameInstance) obj;
        if (!this.getBottomPlayer().equals(other.getBottomPlayer()))
        {
            return false;
        }
        if (!this.getTopPlayer().equals(other.getTopPlayer()))
        {
            return false;
        }
        if (this.getInitiatedTimeSeconds() != other.getInitiatedTimeSeconds())
        {
            return false;
        }
        return true;
    }

    public static PlayerPosition negatePosition(final PlayerPosition userPos)
    {
        if (userPos == null)
        {
            return null;
        }

        if (userPos.equals(PlayerPosition.BOTTOM_PLAYER))
        {
            return PlayerPosition.TOP_PLAYER;
        }
        return PlayerPosition.BOTTOM_PLAYER;
    }
}
