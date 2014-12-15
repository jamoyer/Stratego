package stratego.controller;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Random;

import stratego.AppContext;
import stratego.model.Field;
import stratego.model.GameEnd;
import stratego.model.GameInstance;
import stratego.model.PlayerPosition;
import stratego.model.Position;
import stratego.model.Unit;
import stratego.model.UnitType;
import stratego.user.Validator;

public class GameInstanceController
{
    // 1 minute is the max time to queue for a new game
    private static final long MAX_QUEUE_TIME_TO_START_GAME_SECONDS = 60;
    private static final long THREAD_SLEEP_SECONDS = 1;

    private static GameInstanceController _instController = null;
    private static Random _rand = null;
    private static final String CLASS_LOG = "GameInstanceController: ";

    private void logMsg(final String msg)
    {
        System.out.println(CLASS_LOG + msg);
    }

    private GameInstanceController()
    {
        _rand = new Random();

        // end games that have met their time limit
        Thread gameModerator = new Thread(new GameModerator());
        // gameModerator.start();
    }

    /**
     * Get the singleton instance of GameInstanceController.
     * 
     * @return
     */
    public static GameInstanceController getInstanceController()
    {
        if (_instController == null)
        {
            System.out.println(CLASS_LOG + "Getting GameInstanceController");
            synchronized (GameInstanceController.class)
            {
                if (_instController == null)
                {
                    System.out.println(CLASS_LOG + "Instantiating GameInstanceController");
                    _instController = new GameInstanceController();
                }
            }
        }
        return _instController;
    }

    /**
     * Returns the game for the user
     * 
     * @param user
     * @return
     */
    public GameInstance getGameByUser(final String user)
    {
        logMsg("Looking for user: " + user + "'s game.");

        GameInstance game = AppContext.getGame(user);

        if (game != null)
        {
            logMsg(user + "'s game found.");
        }
        else
        {
            logMsg(user + "'s game was not found.");
        }

        return game;
    }

    /**
     * Returns a game without a second/top player and which is therefore
     * joinable.
     * 
     * @return
     */
    private GameInstance getJoinableGame()
    {
        logMsg("Looking for joinable game.");
        for (GameInstance game : AppContext.getGames())
        {
            if (game.getTopPlayer() == null)
            {
                logMsg("Joinable game found.");
                return game;
            }
        }
        logMsg("No joinable game found.");
        return null;
    }

    /**
     * Puts the user into a new game. If an error occurs the response message
     * will have isSuccessful = false and ErrorMsg set to an appropriate
     * message. If successful, the user's position and opponent's username will
     * be set in the response message.
     * 
     * @param rspMsg
     *            The response message for the user.
     * @param user
     *            The user's username.
     */
    public void newGame(final GameControlMessage rspMsg, final String user)
    {
        logMsg("Processing newGame request");
        // check if the user is currently in a game
        if (getGameByUser(user) != null)
        {
            logMsg(user + " already has a game running.");
            rspMsg.setSuccessful(false);
            rspMsg.setLogMsg("Cannot start new game, a game is already in progress.");
            return;
        }

        // try to find an existing game that we can join
        GameInstance game = getJoinableGame();
        if (game != null)
        {
            logMsg(user + " is joining " + game.getBottomPlayer() + "'s game.");
            game.setTopPlayer(user);
            AppContext.putGame(user, game);
        }
        else
        {
            logMsg(user + " is creating a new game.");
            game = new GameInstance(user, Validator.currentTimeSeconds());
            rspMsg.setGame(game, user);
            AppContext.putGame(user, game);
            int waitingTime = 0;

            // I'm not sure if this is the correct way to go or if we should
            // just have the front end keep asking if there is a second player,
            // what this is doing right now is tying up a thread until a second
            // player joins.

            logMsg(user + " is waiting for another player.");
            // sleep until the second player has joined the game
            while (Validator.emptyString(game.getTopPlayer()))
            {
                long time = Validator.currentTimeSeconds();
                try
                {
                    // x1000 because sleep takes milliseconds
                    Thread.sleep(THREAD_SLEEP_SECONDS * 1000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                waitingTime += Validator.currentTimeSeconds() - time;

                // check for time out
                if (waitingTime >= MAX_QUEUE_TIME_TO_START_GAME_SECONDS)
                {
                    logMsg(user + "'s game has timed out while waiting for another player.");
                    AppContext.removeGame(user);
                    rspMsg.setSuccessful(false);
                    rspMsg.setLogMsg("Game timed out after " + MAX_QUEUE_TIME_TO_START_GAME_SECONDS
                            + " seconds. There are probably no other players.");
                    return;
                }
            }
        }

        // sanity check: game should be good to go at this point and definitely
        // should not be null, or have a missing player.
        if (game == null || Validator.emptyString(game.getBottomPlayer()) || Validator.emptyString(game.getTopPlayer()))
        {
            logMsg(user + "'s game failed the sanity check.");
            rspMsg.setSuccessful(false);
            rspMsg.setLogMsg("Game was null or had a missing player after setup.");
            return;
        }

        // Hurray, both players have joined!
        // Create response so players can choose starting positions
        rspMsg.setGame(game, user);
        rspMsg.setField(game.getFieldSymbolsByPlayer(user));
        logMsg(user + "'s game is all good to go, sending response.");
        rspMsg.setSuccessful(true);
        PlayerPosition userPos = game.getPlayerPosition(user);
        rspMsg.setPlayerPosition(userPos);
        if (userPos.equals(PlayerPosition.TOP_PLAYER))
        {
            rspMsg.setOpponent(game.getBottomPlayer());
        }
        else
        {
            rspMsg.setOpponent(game.getTopPlayer());
        }

        // update the response time
        game.setPlayerLastResponseTime(user, Validator.currentTimeSeconds());
    }

    /**
     * Given a user and a grid of unit positions, this method will attempt to
     * set the user's positions on the field.
     * 
     * @param rspMsg
     * @param user
     * @param positions
     */
    public void setPositions(PrintWriter output, GameControlMessage rspMsg, final String user,
            final char[][] positions, String theme)
    {
        rspMsg.setSuccessful(false);
        GameInstance game = getGameByUser(user);
        rspMsg.setGame(game, user);

        // user has to be in a game
        if (game == null)
        {
            logMsg(user + " is not in a game.");
            rspMsg.setLogMsg("Unable to set positions: User is not in a game.");
            output.print(rspMsg.getMessage());
            output.flush();
            return;
        }

        // update the response time
        game.setPlayerLastResponseTime(user, Validator.currentTimeSeconds());

        // cannot set starting positions until second player has joined
        if (game.getTopPlayer() == null)
        {
            logMsg(user + " cannot set starting positions because there is no second player.");
            rspMsg.setLogMsg("Unable to set positions: No second player.");
            output.print(rspMsg.getMessage());
            output.flush();
            return;
        }

        // don't let players reset their starting positions
        if (game.checkPlayerHasSetPositions(user))
        {
            logMsg(user + " has already set starting positions.");
            rspMsg.setLogMsg("Unable to set positions: Starting positions already set.");
            output.print(rspMsg.getMessage());
            output.flush();
            return;
        }

        // shouldn't be necessary to check this but if we can't be too careful.
        if (positions.length != Field.getStartingPlayerRowCount() || positions[0].length != Field.getColumnCount())
        {
            logMsg(user + "'s positions are not the right size.");
            rspMsg.setLogMsg("Unable to set positions: Positions are not the right size.");
            output.print(rspMsg.getMessage());
            output.flush();
            return;
        }

        // set up a list of unitCounts so we can make sure that there are enough
        // units of each type
        HashMap<UnitType, Integer> unitCounts = new HashMap<UnitType, Integer>(UnitType.values().length);
        for (UnitType type : UnitType.values())
        {
            unitCounts.put(type, type.getNumUnits());
        }

        // loop through the given positions and set the player's units
        PlayerPosition userPos = game.getPlayerPosition(user);
        for (int row = 0; row < Field.getStartingPlayerRowCount(); row++)
        {
            int botPlayerRow = row + Field.getBottomPlayerRowOffset();
            for (int col = 0; col < Field.getColumnCount(); col++)
            {
                char symbol = positions[row][col];
                UnitType unitType = UnitType.getUnitByChar(symbol);
                if (unitType == null)
                {
                    logMsg(symbol + " does not correlate to a valid unit type.");
                    rspMsg.setLogMsg("Unable to set positions: " + symbol + " does not correlate to a valid unit type.");
                    output.print(rspMsg.getMessage());
                    output.flush();
                    return;
                }
                Unit unit = new Unit(unitType, userPos);

                Position pos = null;
                // set top player units at the top of the field and bottom
                // player units at the bottom
                if (userPos.equals(PlayerPosition.TOP_PLAYER))
                {
                    pos = new Position(row, col);
                }
                else
                {
                    pos = new Position(botPlayerRow, col);
                }

                // decrement unit count for this type
                unitCounts.put(unitType, unitCounts.get(unitType) - 1);

                // actually set the unit on the field
                game.getField().setUnitAt(pos, unit);
            }
        }

        logMsg("Setting theme to " + theme);
        game.setPlayerTheme(userPos, theme);

        // make sure unit counts are correct
        for (Integer count : unitCounts.values())
        {
            if (count.intValue() != 0)
            {
                logMsg("Cannot set positions for " + user + ", unit count is incorrect.");
                rspMsg.setLogMsg("Unable to set positions: unit count is incorrect.");
                output.print(rspMsg.getMessage());
                output.flush();
                return;
            }
        }

        // show that we have set our positions
        logMsg(user + "'s positions are set.");
        game.setPlayerHasStartingPositions(userPos);

        // wait for opponent to set their positions
        while (!game.checkPlayerHasSetPositions(GameInstance.negatePosition(userPos)))
        {
            logMsg(user + " is waiting for " + game.getOpponent(userPos) + " to set starting positions.");

            // need to pick a player to go first if it hasn't been done already
            if (game.getTurn() == null)
            {
                if (_rand.nextBoolean())
                {
                    game.setTurn(PlayerPosition.BOTTOM_PLAYER, Validator.currentTimeSeconds());
                }
                else
                {
                    game.setTurn(PlayerPosition.TOP_PLAYER, Validator.currentTimeSeconds());
                }
            }

            // make sure other player didn't leave
            if (game.getWinner() != null)
            {
                break;
            }

            // otherwise wait until the other player is ready
            try
            {
                // x1000 because sleep takes milliseconds
                Thread.sleep(THREAD_SLEEP_SECONDS * 1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        // need to check fow winners, either by a player leaving and timing out
        // or by a player blocking themselves in with bombs
        if (game.getWinner() != null || game.checkForWin(Validator.currentTimeSeconds()))
        {
            // reveal the units to the user
            char[][] revealGrid = game.getExposedFieldByPlayer(userPos);
            rspMsg.setField(revealGrid);

            // reveal the units to the opponent
            char[][] enemyView = game.getExposedFieldByPlayer(game.getOpponent(userPos));
            // wait until opponent has processed the previous revealGrid
            while (game.getBattleRevealGrid() != null)
            {
            }
            game.setBattleRevealGrid(enemyView);
        }
        else
        {
            rspMsg.setField(game.getFieldSymbolsByPlayer(userPos));
        }

        // display the result of this move
        rspMsg.setSuccessful(true);
        rspMsg.setGameWon(userPos.equals(game.getWinner()));
        rspMsg.setGameLost(userPos.equals(GameInstance.negatePosition(game.getWinner())));
        rspMsg.setTurn(game.getTurn());
        output.print(rspMsg.getMessage());
        output.flush();

        // only wait if the game is still going
        if (game.getWinner() == null)
        {
            // Hurray, all starting positions are set, the combat may begin!
            logMsg(user + "'s positions are set and opponent: " + game.getOpponent(userPos)
                    + "'s positions are set. Waiting for moves.");
            rspMsg.setOpponentTheme(game.getOpponentTheme(userPos));

            // the player that isn't going first needs to wait for the other
            // player to take their turn
            waitForPlayersTurn(output, rspMsg, game, userPos);
        }
    }

    private void waitForPlayersTurn(final PrintWriter output, final GameControlMessage rspMsg, final GameInstance game,
            final PlayerPosition userPos)
    {
        if (game.getTurn() == null || game.getTurn().equals(userPos))
        {
            return;
        }

        // wait until the other player is done
        while (game.getTurn() != null && !game.getTurn().equals(userPos))
        {
            // wait some time
            try
            {
                logMsg(game.getPlayer(userPos) + " is waiting for " + game.getOpponent(userPos)
                        + " to finish their turn.");

                // x1000 because sleep takes milliseconds
                Thread.sleep(THREAD_SLEEP_SECONDS * 1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            // display any message that has occured because of the other user
            char[][] revealGrid = game.getBattleRevealGrid();
            if (revealGrid != null)
            {
                rspMsg.setSuccessful(true);
                rspMsg.setField(revealGrid);
                rspMsg.setTurn(game.getTurn());
                rspMsg.setIsReveal(true);

                // prints the json response message
                output.print(rspMsg.getMessage());
                output.flush();
                rspMsg.setIsReveal(false);
                game.setBattleRevealGrid(null);
            }
        }

        // need to check if the game is still going
        if (game.getWinner() != null)
        {
            // display the winning results
            rspMsg.setField(game.getBattleRevealGrid());
            rspMsg.setGameWon(game.getWinner().equals(userPos));
            rspMsg.setGameLost(game.getWinner().equals(GameInstance.negatePosition(userPos)));
        }
        else
        {
            rspMsg.setField(game.getFieldSymbolsByPlayer(userPos));
        }

        // its our turn now, display the new grid
        rspMsg.setSuccessful(true);
        rspMsg.setTurn(game.getTurn());

        // prints the json response message
        output.print(rspMsg.getMessage());
        output.flush();
    }

    public void moveUnit(PrintWriter output, GameControlMessage rspMsg, final String user, final Position source,
            final Position destination)
    {
        rspMsg.setSuccessful(false);
        GameInstance game = getGameByUser(user);
        rspMsg.setGame(game, user);
        // user has to be in a game
        if (game == null)
        {
            logMsg(user + " is not in a game.");
            rspMsg.setLogMsg("Unable to move unit: User is not in a game.");
            output.print(rspMsg.getMessage());
            output.flush();
            return;
        }

        // update the response time
        game.setPlayerLastResponseTime(user, Validator.currentTimeSeconds());

        // check that this move is valid
        if (!game.isValidMove(rspMsg, user, source, destination, true, false))
        {
            logMsg("Invalid move for " + user);
            output.print(rspMsg.getMessage());
            output.flush();
            return;
        }

        PlayerPosition userPos = game.getPlayerPosition(user);
        Unit sourceUnit = game.getField().getUnitAt(source);
        Unit destUnit = game.getField().getUnitAt(destination);

        rspMsg.setSuccessful(true);

        // just move the unit if the destination is empty
        if (destUnit == null)
        {
            game.getField().setUnitAt(destination, sourceUnit);
            game.getField().setUnitAt(source, null);
        }

        // otherwise sourceUnit attacks destUnit
        else
        {
            // reveal the units to the user
            char[][] revealGrid = game.getFieldSymbolsByPlayer(user);
            revealGrid[destination.getRow()][destination.getColumn()] = game.getField().getUnitAt(destination)
                    .getEnemyType().getSymbol();
            rspMsg.setField(revealGrid);
            rspMsg.setIsReveal(true);

            // prints the json response message
            output.print(rspMsg.getMessage());
            output.flush();
            rspMsg.setIsReveal(false);

            // reveal the units to the opponent
            revealGrid = game.getFieldSymbolsByPlayer(game.getOpponent(userPos));
            revealGrid[source.getRow()][source.getColumn()] = game.getField().getUnitAt(source).getEnemyType()
                    .getSymbol();
            game.setBattleRevealGrid(revealGrid);

            /*
             * Start Combat logic
             */

            // check for a tie
            if (sourceUnit.getType().equals(destUnit.getType()))
            {
                // both die
                game.getField().setUnitAt(source, null);
                game.getField().setUnitAt(destination, null);
            }

            // check for spy and marshall
            else if ((sourceUnit.getType().equals(UnitType.SPY)) && destUnit.getType().equals(UnitType.MARSHALL))
            {
                // attacker wins
                game.getField().setUnitAt(source, null);
                game.getField().setUnitAt(destination, sourceUnit);
            }

            // check for regular unit
            // (a unit with a rank of 1-10 or simply not a rank of -1)
            // winner moves into losers tile after combat
            else if ((sourceUnit.getType().getRank() != -1) && (destUnit.getType().getRank() != -1))
            {
                // if attacker is a lower rank than enemy, enemy is defeated
                if (sourceUnit.getType().getRank() < destUnit.getType().getRank())
                {
                    game.getField().setUnitAt(source, null);
                    game.getField().setUnitAt(destination, sourceUnit);
                }
                // enemy has a higher rank and attacker is defeated
                else
                {
                    game.getField().setUnitAt(source, null);
                    // check if the enemy has moved more than one space to
                    // attack this unit (this happens with scounts)
                    if (source.getRow() == destination.getRow())
                    {
                        if (source.getColumn() <= destination.getColumn())
                        {
                            game.getField().setUnitAt(new Position(destination.getRow(), destination.getColumn() - 1),
                                                      destUnit);
                        }
                        else
                        {
                            game.getField().setUnitAt(new Position(destination.getRow(), destination.getColumn() + 1),
                                                      destUnit);
                        }
                    }
                    // check if the enemy has moved more than one space to
                    // attack this unit (this happens with scounts)
                    else if (source.getColumn() == destination.getColumn())
                    {
                        if (source.getRow() < destination.getRow())
                        {
                            game.getField().setUnitAt(new Position(destination.getRow() - 1, destination.getColumn()),
                                                      destUnit);
                        }
                        else
                        {
                            game.getField().setUnitAt(new Position(destination.getRow() + 1, destination.getColumn()),
                                                      destUnit);
                        }
                    }
                    game.getField().setUnitAt(destination, null);
                }
            }

            // bombs kill everything except the miners, who defuse them
            else if (destUnit.getType().equals(UnitType.BOMB))
            {
                if (sourceUnit.getType().equals(UnitType.MINER))
                {
                    game.getField().setUnitAt(source, null);
                    game.getField().setUnitAt(destination, sourceUnit);
                }
                else
                {
                    game.getField().setUnitAt(source, null);
                }
            }

            // check for flag capture
            else if (destUnit.getType().equals(UnitType.FLAG))
            {
                game.getField().setUnitAt(destination, sourceUnit);
                game.getField().setUnitAt(source, null);
                game.setWinner(userPos, Validator.currentTimeSeconds(), GameEnd.Standard);
            }
        }

        /*
         * End combat logic
         */

        if (game.checkForWin(Validator.currentTimeSeconds()))
        {
            // reveal the units to the user
            char[][] revealGrid = game.getExposedFieldByPlayer(userPos);
            rspMsg.setField(revealGrid);

            // reveal the units to the opponent
            char[][] enemyView = game.getExposedFieldByPlayer(game.getOpponent(userPos));
            // wait until opponent has processed the previous revealGrid
            while (game.getBattleRevealGrid() != null)
            {
            }
            game.setBattleRevealGrid(enemyView);
        }
        else
        {
            rspMsg.setField(game.getFieldSymbolsByPlayer(userPos));
            // user's turn is now over
            game.setTurn(GameInstance.negatePosition(userPos), Validator.currentTimeSeconds());
        }

        // display the result of this game
        rspMsg.setGameWon(userPos.equals(game.getWinner()));
        rspMsg.setGameLost(GameInstance.negatePosition(userPos).equals(game.getWinner()));
        rspMsg.setTurn(game.getTurn());
        output.print(rspMsg.getMessage());
        output.flush();

        // only wait if the game is still going
        if (game.getWinner() == null)
        {
            // wait for next user to be done
            waitForPlayersTurn(output, rspMsg, game, userPos);
        }
    }
}
