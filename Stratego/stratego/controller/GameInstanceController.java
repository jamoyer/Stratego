package stratego.controller;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Random;

import stratego.model.Field;
import stratego.model.GameInstance;
import stratego.model.PlayerPosition;
import stratego.model.Position;
import stratego.model.ResponseMessage;
import stratego.model.Unit;
import stratego.model.UnitType;
import stratego.user.Validator;

public class GameInstanceController
{
    // 10 minutes is the max time to queue for a new game
    private static final long MAX_QUEUE_TIME_TO_START_GAME_SECONDS = 60;
    private static final long THREAD_SLEEP_SECONDS = 1;
    private static final long MODERATOR_THREAD_SLEEP_SECONDS = 5;
    private static final int INITIAL_INSTANCES_CAPACITY = 30;
    // 3 minutes of inactivity allowed for each player before game dies
    private static final int TIME_OUT_SECONDS = 180;

    private final HashMap<String, GameInstance> _gameInstances;

    private static GameInstanceController _instController = null;
    private static Random _rand = null;

    private GameInstanceController()
    {
        _gameInstances = new HashMap<String, GameInstance>(INITIAL_INSTANCES_CAPACITY);
        _rand = new Random();

        // end games that have met their time limit
        Thread gameModerator = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    long now = Validator.currentTimeSeconds();
                    for (GameInstance game : _gameInstances.values())
                    {
                        // only check games that have two players set
                        if (game.getTopPlayer() != null)
                        {

                            if ((now - game.checkPlayerLastResponsetime(PlayerPosition.TOP_PLAYER)) > TIME_OUT_SECONDS)
                            {
                                game.setWinner(PlayerPosition.BOTTOM_PLAYER);
                                System.out.println("GameInstanceModerator: " + game.getWinnerName()
                                        + " has won a game due to opponent time out.");
                            }
                            else if ((now - game.checkPlayerLastResponsetime(PlayerPosition.BOTTOM_PLAYER)) > TIME_OUT_SECONDS)
                            {
                                game.setWinner(PlayerPosition.TOP_PLAYER);
                                System.out.println("GameInstanceModerator: " + game.getWinnerName()
                                        + " has won a game due to opponent time out.");
                            }
                        }
                    }
                    try
                    {
                        Thread.sleep(MODERATOR_THREAD_SLEEP_SECONDS * 1000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
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
            System.out.println("Getting GameInstanceController");
            synchronized (GameInstanceController.class)
            {
                if (_instController == null)
                {
                    System.out.println("Instantiating GameInstanceController");
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
        System.out.println("GameInstanceController: looking for user: " + user + "'s game.");

        GameInstance game = _gameInstances.get(user);

        if (game != null)
        {
            System.out.println("GameInstanceController: user: " + user + "'s game found.");
        }
        else
        {
            System.out.println("GameInstanceController: user: " + user + "'s game was not found.");
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
        System.out.println("GameInstanceController: looking for joinable game.");
        for (GameInstance game : _gameInstances.values())
        {
            if (game.getTopPlayer() == null)
            {
                System.out.println("GameInstanceController: joinable game found.");
                return game;
            }
        }
        System.out.println("GameInstanceController: no joinable game found.");
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
    public void newGame(final ResponseMessage rspMsg, final String user)
    {
        System.out.println("GameInstanceController: processing newGame request");
        // check if the user is currently in a game
        if (getGameByUser(user) != null)
        {
            System.out.println("user: " + user + " already has a game running.");
            rspMsg.setSuccessful(false);
            rspMsg.setErrorMsg("Cannot start new game, a game is already in progress.");
            return;
        }

        // try to find an existing game that we can join
        GameInstance game = getJoinableGame();
        if (game != null)
        {
            System.out.println("GameInstanceController: user: " + user + " is joining user: " + game.getBottomPlayer()
                    + "'s game.");
            game.setTopPlayer(user);
            _gameInstances.put(user, game);
        }
        else
        {
            System.out.println("GameInstanceController: user: " + user + " is creating a new game.");
            game = new GameInstance(user, Validator.currentTimeSeconds());
            _gameInstances.put(user, game);
            int waitingTime = 0;

            // I'm not sure if this is the correct way to go or if we should
            // just have the front end keep asking if there is a second player,
            // what this is doing right now is tying up a thread until a second
            // player joins.

            System.out.println("GameInstanceController: user: " + user + " is waiting for another player.");
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
                    System.out.println("GameInstanceController: user: " + user
                            + "'s game has timed out while waiting for another player.");
                    _gameInstances.remove(game);
                    rspMsg.setSuccessful(false);
                    rspMsg.setErrorMsg("Game timed out after " + MAX_QUEUE_TIME_TO_START_GAME_SECONDS
                            + " seconds. There are probably no other players.");
                    return;
                }
            }
        }

        // sanity check: game should be good to go at this point and definitely
        // should not be null, or have a missing player.
        if (game == null || Validator.emptyString(game.getBottomPlayer()) || Validator.emptyString(game.getTopPlayer()))
        {
            System.out.println("GameInstanceController: user: " + user + "'s game failed the sanity check.");
            rspMsg.setSuccessful(false);
            rspMsg.setErrorMsg("Game was null or had a missing player after setup.");
            return;
        }

        // Hurray, both players have joined!
        // Create response so players can choose starting positions
        System.out.println("GameInstanceController: user: " + user + "'s game is all good to go, sending response.");
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
    public void setPositions(PrintWriter output, ResponseMessage rspMsg, final String user, final char[][] positions)
    {
        rspMsg.setSuccessful(false);
        GameInstance game = getGameByUser(user);
        // user has to be in a game
        if (game == null)
        {
            System.out.println("GameInstanceController: user: " + user + " is not in a game.");
            rspMsg.setErrorMsg("Unable to set positions: User is not in a game.");
            output.print(rspMsg.getMessage());
            return;
        }

        // update the response time
        game.setPlayerLastResponseTime(user, Validator.currentTimeSeconds());

        // don't let players reset their starting positions
        if (game.checkPlayerHasSetPositions(user))
        {
            System.out.println("GameInstanceController: user: " + user + " has already set starting positions.");
            rspMsg.setErrorMsg("Unable to set positions: Starting positions already set.");
            output.print(rspMsg.getMessage());
            return;
        }

        // shouldn't be necessary to check this but if we can't be too careful.
        if (positions.length != Field.getStartingPlayerRowCount() || positions[0].length != Field.getColumnCount())
        {
            System.out.println("GameInstanceController: user: " + user + "'s positions are not the right size.");
            rspMsg.setErrorMsg("Unable to set positions: Positions are not the right size.");
            output.print(rspMsg.getMessage());
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
                    System.out.println("GameInstanceController: " + symbol
                            + " does not correlate to a valid unit type.");
                    rspMsg.setErrorMsg("Unable to set positions: " + symbol
                            + " does not correlate to a valid unit type.");
                    output.print(rspMsg.getMessage());
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

        // make sure unit counts are correct
        for (Integer count : unitCounts.values())
        {
            if (count.intValue() != 0)
            {
                System.out.println("GameInstanceController: cannot set positions for " + user
                        + ", unit count is incorrect.");
                rspMsg.setErrorMsg("Unable to set positions: unit count is incorrect.");
                output.print(rspMsg.getMessage());
                return;
            }
        }

        // show that we have set our positions
        System.out.println("GameInstanceController: user: " + user + "'s positions are set.");
        game.setPlayerHasStartingPositions(userPos);

        // wait for opponent to set their positions
        while (!game.checkPlayerHasSetPositions(GameInstance.negatePosition(userPos)))
        {
            System.out.println("GameInstanceController: " + user + " is waiting for " + game.getOpponent(userPos)
                    + " to set starting positions.");

            // need to pick a player to go first if it hasn't been done already
            if (game.getTurn() == null)
            {
                if (_rand.nextBoolean())
                {
                    game.setTurn(PlayerPosition.BOTTOM_PLAYER);
                }
                else
                {
                    game.setTurn(PlayerPosition.TOP_PLAYER);
                }
            }

            // make sure other player didn't leave
            if (game.getWinner() != null)
            {
                // TODO: win game
                System.out.println("GameInstanceController: " + game.getWinnerName() + " has won a game.");
                rspMsg.setErrorMsg("Game Over.");
                output.print(rspMsg.getMessage());
                return;
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

        // Hurray, all starting positions are set, the combat may begin!
        System.out.println("GameInstanceController: user: " + user + "'s positions are set and opponent: "
                + game.getOpponent(userPos) + "'s positions are set. Waiting for moves.");
        rspMsg.setSuccessful(true);
        rspMsg.setTurn(game.getTurn());
        rspMsg.setField(game.getFieldSymbolsByPlayer(userPos));

        // prints the json response message
        output.print(rspMsg.getMessage());

        // the player that isn't going first needs to wait for the other player
        // to take their turn
        waitForPlayersTurn(output, rspMsg, game, userPos);
    }

    private void waitForPlayersTurn(final PrintWriter output, final ResponseMessage rspMsg, final GameInstance game,
            final PlayerPosition userPos)
    {
        if (!game.getTurn().equals(userPos))
        {
            // wait until the other player is done
            while (!game.getTurn().equals(userPos))
            {
                try
                {
                    System.out.println("GameInstanceController: user: " + game.getPlayer(userPos)
                            + " is waiting for user: " + game.getOpponent(userPos) + " to finish their turn.");

                    // x1000 because sleep takes milliseconds
                    Thread.sleep(THREAD_SLEEP_SECONDS * 1000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                char[][] revealGrid = game.getBattleRevealGrid();
                if (revealGrid != null)
                {
                    rspMsg.setSuccessful(true);
                    rspMsg.setField(revealGrid);

                    // prints the json response message
                    output.print(rspMsg.getMessage());
                    game.setBattleRevealGrid(null);
                }
            }

            // need to check if the game is still going
            if (game.getWinner() != null)
            {
                // TODO: win game stuff
            }

            // its our turn now, display the new grid
            rspMsg.setSuccessful(true);
            rspMsg.setTurn(game.getTurn());
            rspMsg.setField(game.getFieldSymbolsByPlayer(userPos));

            // prints the json response message
            output.print(rspMsg.getMessage());
        }
    }

    public void moveUnit(PrintWriter output, ResponseMessage rspMsg, final String user, final Position source,
            final Position destination)
    {
        rspMsg.setSuccessful(false);
        GameInstance game = getGameByUser(user);

        // user has to be in a game
        if (game == null)
        {
            System.out.println("GameInstanceController: user: " + user + " is not in a game.");
            rspMsg.setErrorMsg("Unable to move unit: User is not in a game.");
            output.print(rspMsg.getMessage());
            return;
        }

        // update the response time
        game.setPlayerLastResponseTime(user, Validator.currentTimeSeconds());

        // make sure source position is valid
        if (!Field.checkPositionIsWithinBounds(source))
        {
            System.out.println("GameInstanceController: user: " + user + " entered an invalid source position.");
            rspMsg.setErrorMsg("Unable to move unit: Source position is out of bounds.");
            output.print(rspMsg.getMessage());
            return;
        }

        // make sure destination position is valid
        if (!Field.checkPositionIsWithinBounds(destination))
        {
            System.out.println("GameInstanceController: user: " + user + " entered an invalid destination position.");
            rspMsg.setErrorMsg("Unable to move unit: Destination position is out of bounds.");
            output.print(rspMsg.getMessage());
            return;
        }

        // make sure the game is still on
        if (game.getWinner() != null)
        {
            System.out.println("GameInstanceController: user: " + user + " cannot move unit because game is over");
            rspMsg.setErrorMsg("Unable to move unit: game is over.");
            output.print(rspMsg.getMessage());
            return;
        }

        PlayerPosition userPos = game.getPlayerPosition(user);

        // don't let players move units if it isn't their turn
        if (userPos == null || !userPos.equals(game.getTurn()))
        {
            System.out.println("GameInstanceController: user: " + user + " cannot move because it is not their turn.");
            rspMsg.setErrorMsg("Unable to move unit, it is not " + user + "'s turn.");
            output.print(rspMsg.getMessage());
            return;
        }

        // cannot move diagonally
        if (destination.getRow() != source.getRow() && destination.getColumn() != source.getColumn())
        {
            System.out.println("GameInstanceController: user: " + user + " tried to move diagonally.");
            rspMsg.setErrorMsg("Unable to move unit, units cannot move diagonally");
            output.print(rspMsg.getMessage());
            return;
        }

        Unit sourceUnit = game.getField().getUnitAt(source);

        // make sure there is a unit at source
        if (sourceUnit == null)
        {
            System.out.println("GameInstanceController: user: " + user + " tile at source is not a unit.");
            rspMsg.setErrorMsg("Unable to move unit, tile at source is not a unit.");
            output.print(rspMsg.getMessage());
            return;
        }

        // make sure the unit belongs to the user
        if (!userPos.equals(sourceUnit.getPlayer()))
        {
            System.out.println("GameInstanceController: user: " + user + " does not own the unit at source");
            rspMsg.setErrorMsg("Unable to move unit, unit does not belong to the user.");
            output.print(rspMsg.getMessage());
            return;
        }

        // bombs and flags are not allowed to move
        if (sourceUnit.getType().equals(UnitType.BOMB) || sourceUnit.getType().equals(UnitType.FLAG))
        {
            System.out.println("GameInstanceController: user: " + user + " attempted to move an unmoveable unit.");
            rspMsg.setErrorMsg("Unable to move unit, unit cannot move.");
            output.print(rspMsg.getMessage());
            return;
        }

        Unit destUnit = game.getField().getUnitAt(destination);

        // cannot move into an allied unit
        if (destUnit != null && destUnit.getPlayer().equals(userPos))
        {
            System.out.println("GameInstanceController: user: " + user + " attempted to move into an allied unit.");
            rspMsg.setErrorMsg("Unable to move unit, destination is an allied unit.");
            output.print(rspMsg.getMessage());
            return;
        }

        // cannot move into an obstacle
        if (destUnit == null && game.getField().isObstacle(destination))
        {
            System.out.println("GameInstanceController: user: " + user + " attempted to move into an obstacle.");
            rspMsg.setErrorMsg("Unable to move unit, destination is an obstacle.");
            output.print(rspMsg.getMessage());
            return;
        }

        // unless the unit is a scout, it cannot move more than one tile away
        // from the source
        if (!sourceUnit.getType().equals(UnitType.SCOUT))
        {
            if ((destination.getRow() != source.getRow() + 1 || destination.getRow() != source.getRow() - 1
                    || destination.getColumn() != source.getColumn() + 1 || destination.getColumn() != source
                    .getColumn() - 1))
            {
                System.out.println("GameInstanceController: user: " + user
                        + " attempted to move a non-scout unit more than one tile away.");
                rspMsg.setErrorMsg("Unable to move unit, unit cannot move more than one tile.");
                output.print(rspMsg.getMessage());
                return;
            }
        }

        /*
         * At this point we know it is a valid move.
         */

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
            rspMsg.setSuccessful(true);
            rspMsg.setField(revealGrid);

            // prints the json response message
            output.print(rspMsg.getMessage());

            // reveal the units to the opponent
            revealGrid = game.getFieldSymbolsByPlayer(game.getOpponent(userPos));
            revealGrid[source.getRow()][source.getColumn()] = game.getField().getUnitAt(source).getEnemyType()
                    .getSymbol();
            game.setBattleRevealGrid(revealGrid);

            /*
             * Combat logic
             */

            // check for a tie
            if (sourceUnit.getType() == destUnit.getType())
            {
                // both die
                game.getField().setUnitAt(source, null);
                game.getField().setUnitAt(destination, null);
            }

            // check for spy and marshall
            else if ((sourceUnit.getType().equals(UnitType.SPY) || destUnit.getType().equals(UnitType.SPY))
                    && (sourceUnit.getType().equals(UnitType.MARSHALL) || destUnit.getType().equals(UnitType.MARSHALL)))
            {
                // attacker wins
                game.getField().setUnitAt(source, null);
                game.getField().setUnitAt(destination, sourceUnit);
            }

            // check for regular unit
            // (a unit with a rank of 1-10 or simply not a rank of -1)
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
                // TODO: win game stuff
            }
        }

        /*
         * user's turn is now over
         */

        game.setTurn(GameInstance.negatePosition(userPos));

        // display the result of this move
        rspMsg.setTurn(game.getTurn());
        rspMsg.setField(game.getFieldSymbolsByPlayer(userPos));
        output.print(rspMsg.getMessage());

        // wait for next user to be done
        waitForPlayersTurn(output, rspMsg, game, userPos);
    }
}
