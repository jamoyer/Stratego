package stratego.model;

public class GameInstance
{
    private static final String CLASS_LOG = "GameInstance: ";

    private String topPlayer = null;
    private long topPlayerLastResponseTime;
    private boolean topPlayerPositionsSet = false;

    private String bottomPlayer = null;
    private long bottomPlayerLastResponseTime;
    private boolean bottomPlayerPositionsSet = false;

    private final long initiatedTime; // seconds
    private long endTime; // seconds

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

    public char[][] getExposedFieldByPlayer(final String user)
    {
        return this.getExposedFieldByPlayer(this.getPlayerPosition(user));
    }

    /**
     * Returns the grid of characters to display for a given player.
     * 
     * @param userPos
     * @return
     */
    public char[][] getExposedFieldByPlayer(final PlayerPosition userPos)
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
                    grid[row][col] = unit.getEnemyType().getSymbol();
                    continue;
                }
            }
        }
        return grid;
    }

    public String getWinnerName()
    {
        if (this.winner == null)
        {
            return null;
        }
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

    public void setWinner(final PlayerPosition position, final long currentTimeSeconds)
    {
        logMsg(getWinnerName() + " has won a game!");
        this.winner = position;
        this.endTime = currentTimeSeconds;
        this.currentTurn = null;
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

    public boolean isValidMove(ResponseMessage rspMsg, final String user, final Position source,
            final Position destination, final boolean loggingOn, final boolean turnMatters)
    {
        if (rspMsg == null)
        {
            return false;
        }
        // make sure source position is valid
        if (!Field.checkPositionIsWithinBounds(source))
        {
            if (loggingOn)
            {
                logMsg(user + " entered an invalid source position.");
            }

            rspMsg.setLogMsg("Unable to move unit: Source position is out of bounds.");
            return false;
        }

        // make sure destination position is valid
        if (!Field.checkPositionIsWithinBounds(destination))
        {
            if (loggingOn)
            {
                logMsg(user + " entered an invalid destination position.");
            }
            rspMsg.setLogMsg("Unable to move unit: Destination position is out of bounds.");
            return false;
        }

        // make sure the game is still on
        if (this.getWinner() != null)
        {
            if (loggingOn)
            {
                logMsg(user + " cannot move unit because game is over");
            }
            rspMsg.setLogMsg("Unable to move unit: game is over.");
            return false;
        }

        PlayerPosition userPos = this.getPlayerPosition(user);

        // don't let players move units if it isn't their turn
        if (turnMatters && (userPos == null || !userPos.equals(this.getTurn())))
        {
            if (loggingOn)
            {
                logMsg(user + " cannot move because it is not their turn.");
            }
            rspMsg.setLogMsg("Unable to move unit, it is not " + user + "'s turn.");
            return false;
        }

        // cannot move diagonally
        if (destination.getRow() != source.getRow() && destination.getColumn() != source.getColumn())
        {
            if (loggingOn)
            {
                logMsg(user + " tried to move diagonally.");
            }
            rspMsg.setLogMsg("Unable to move unit, units cannot move diagonally");
            return false;
        }

        Unit sourceUnit = this.getField().getUnitAt(source);

        // make sure there is a unit at source
        if (sourceUnit == null)
        {
            if (loggingOn)
            {
                logMsg(user + " tile at source is not a unit.");
            }
            rspMsg.setLogMsg("Unable to move unit, tile at source is not a unit.");
            return false;
        }

        // make sure the unit belongs to the user
        if (!userPos.equals(sourceUnit.getPlayer()))
        {
            if (loggingOn)
            {
                logMsg(user + " does not own the unit at source");
            }
            rspMsg.setLogMsg("Unable to move unit, unit does not belong to the user.");
            return false;
        }

        // bombs and flags are not allowed to move
        if (sourceUnit.getType().equals(UnitType.BOMB) || sourceUnit.getType().equals(UnitType.FLAG))
        {
            if (loggingOn)
            {
                logMsg(user + " attempted to move an unmoveable unit.");
            }
            rspMsg.setLogMsg("Unable to move unit, unit cannot move.");
            return false;
        }

        Unit destUnit = this.getField().getUnitAt(destination);

        // cannot move into an allied unit
        if (destUnit != null && destUnit.getPlayer().equals(userPos))
        {
            if (loggingOn)
            {
                logMsg(user + " attempted to move into an allied unit.");
            }
            rspMsg.setLogMsg("Unable to move unit, destination is an allied unit.");
            return false;
        }

        // cannot move into an obstacle
        if (destUnit == null && this.getField().isObstacle(destination))
        {
            if (loggingOn)
            {
                logMsg(user + " attempted to move into an obstacle.");
            }
            rspMsg.setLogMsg("Unable to move unit, destination is an obstacle.");
            return false;
        }

        // unless the unit is a scout, it cannot move more than one tile away
        // from the source
        if (!sourceUnit.getType().equals(UnitType.SCOUT))
        {
            //@formatter:off
            if(((source.getRow() == destination.getRow()) && 
                (source.getColumn()+1 != destination.getColumn()) && 
                (source.getColumn()-1 != destination.getColumn())) || 
                    ((source.getColumn() == destination.getColumn()) && 
                    (source.getRow()-1 != destination.getRow()) && 
                    (source.getRow()+1 != destination.getRow())))
            //@formatter:on
            {
                if (loggingOn)
                {
                    logMsg(user + " attempted to move a non-scout unit more than one tile away.");
                }
                rspMsg.setLogMsg("Unable to move unit, unit cannot move more than one tile.");
                return false;
            }
        }

        // nothing wrong about this move, it is a valid move
        return true;
    }

    private void logMsg(final String msg)
    {
        System.out.println(CLASS_LOG + msg);
    }

    /**
     * Checks for a win by verifying that both players can still move. Sets the
     * winner if there is a win. Does not check that both players have their
     * flags. That is done when players move their pieces.
     * 
     * @param settingPositions
     * 
     * @return true if there is a winner, otherwise false
     */
    public boolean checkForWin(final long currentTimeSeconds)
    {
        // check if the winner is already set
        if (getWinner() != null)
        {
            return true;
        }

        boolean topCanMove = false;
        boolean bottomCanMove = false;

        // loop through the entire grid
        for (int row = 0; row < Field.getRowCount(); row++)
        {
            for (int col = 0; col < Field.getColumnCount(); col++)
            {
                Position pos = new Position(row, col);
                Unit unit = field.getUnitAt(pos);

                // only check units that can move
                if (unit != null && !unit.getType().equals(UnitType.FLAG) && !unit.getType().equals(UnitType.BOMB))
                {
                    // only do the unit if the player has not passed yet.
                    if ((!topCanMove && unit.getPlayer().equals(PlayerPosition.TOP_PLAYER))
                            || (!bottomCanMove && unit.getPlayer().equals(PlayerPosition.BOTTOM_PLAYER)))
                    {
                        // check if the unit is allowed to move into any
                        // adjacent squares

                        // this also works for scouts because if they can't move
                        // into an adjacent square they definitely won't be able
                        // to move more than that
                        for (int i = row - 1; i <= row + 1; i++)
                        {
                            for (int j = col - 1; j <= col + 1; j++)
                            {
                                // skip the position of the unit, we know that's
                                // invalid
                                if (i == row && j == col)
                                {
                                    continue;
                                }

                                // actually check if the unit can move into that
                                // tile
                                if (isValidMove(new ResponseMessage(), this.getPlayer(unit.getPlayer()), pos,
                                                new Position(i, j), false, false))
                                {
                                    if (unit.getPlayer().equals(PlayerPosition.BOTTOM_PLAYER))
                                    {
                                        bottomCanMove = true;
                                    }
                                    else
                                    {
                                        topCanMove = true;
                                    }

                                    if (bottomCanMove && topCanMove)
                                    {
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // checks for both players being unable to move, in this case the loser
        // is whoever's turn is next/ the winner is whoever just had a turn
        if (!bottomCanMove && !topCanMove)
        {
            if (this.currentTurn.equals(PlayerPosition.BOTTOM_PLAYER))
            {
                setWinner(PlayerPosition.BOTTOM_PLAYER, currentTimeSeconds);
            }
            else
            {
                setWinner(PlayerPosition.TOP_PLAYER, currentTimeSeconds);
            }
        }

        // otherwise there is just a normal win
        else if (bottomCanMove)
        {
            setWinner(PlayerPosition.BOTTOM_PLAYER, currentTimeSeconds);
        }
        else
        {
            setWinner(PlayerPosition.TOP_PLAYER, currentTimeSeconds);
        }
        return true;
    }

    public long getEndTime()
    {
        return endTime;
    }
}
