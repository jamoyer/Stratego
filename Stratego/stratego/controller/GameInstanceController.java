package stratego.controller;

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
                            else if ((now - game
                                    .checkPlayerLastResponsetime(PlayerPosition.BOTTOM_PLAYER)) > TIME_OUT_SECONDS)
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
            System.out.println("GameInstanceController: user: " + user + " is joining user: "
                    + game.getBottomPlayer() + "'s game.");
            game.setTopPlayer(user);
            _gameInstances.put(user, game);
        }
        else
        {
            System.out
                    .println("GameInstanceController: user: " + user + " is creating a new game.");
            game = new GameInstance(user, Validator.currentTimeSeconds());
            _gameInstances.put(user, game);
            int waitingTime = 0;

            // I'm not sure if this is the correct way to go or if we should
            // just have the front end keep asking if there is a second player,
            // what this is doing right now is tying up a thread until a second
            // player joins.

            System.out.println("GameInstanceController: user: " + user
                    + " is waiting for another player.");
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
                    rspMsg.setErrorMsg("Game timed out after "
                            + MAX_QUEUE_TIME_TO_START_GAME_SECONDS
                            + " seconds. There are probably no other players.");
                    return;
                }
            }
        }

        // sanity check: game should be good to go at this point and definitely
        // should not be null, or have a missing player.
        if (game == null || Validator.emptyString(game.getBottomPlayer())
                || Validator.emptyString(game.getTopPlayer()))
        {
            System.out.println("GameInstanceController: user: " + user
                    + "'s game failed the sanity check.");
            rspMsg.setSuccessful(false);
            rspMsg.setErrorMsg("Game was null or had a missing player after setup.");
            return;
        }

        // Hurray, both players have joined!
        // Create response so players can choose starting positions
        System.out.println("GameInstanceController: user: " + user
                + "'s game is all good to go, sending response.");
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
    }

    /**
     * Given a user and a grid of unit positions, this method will attempt to
     * set the user's positions on the field.
     * 
     * @param rspMsg
     * @param user
     * @param positions
     */
    public void setPositions(ResponseMessage rspMsg, final String user, final char[][] positions)
    {
        GameInstance game = getGameByUser(user);

        // update the response time

        // don't let players reset their starting positions
        if (game.checkPlayerHasSetPositions(user))
        {
            System.out.println("GameInstanceController: user: " + user
                    + " has already set starting positions.");
            rspMsg.setSuccessful(false);
            rspMsg.setErrorMsg("Unable to set positions: Starting positions already set.");
            return;
        }

        // shouldn't be necessary to check this but if we can't be too careful.
        if (positions.length != Field.getStartingPlayerRowCount()
                || positions[0].length != Field.getColumnCount())
        {
            System.out.println("GameInstanceController: user: " + user
                    + "'s positions are not the right size.");
            rspMsg.setSuccessful(false);
            rspMsg.setErrorMsg("Unable to set positions: Positions are not the right size.");
            return;
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
                    rspMsg.setSuccessful(false);
                    rspMsg.setErrorMsg("Unable to set positions: " + symbol
                            + " does not correlate to a valid unit type.");
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

                game.getField().setUnitAt(pos, unit);
            }
        }

        // show that we have set our positions
        System.out.println("GameInstanceController: user: " + user + "'s positions are set.");
        game.setPlayerHasStartingPositions(userPos);

        // wait for opponent to set their positions
        while (!game.checkPlayerHasSetPositions(GameInstance.negatePosition(userPos)))
        {
            System.out.println("GameInstanceController: " + user + " is waiting for "
                    + game.getOpponent(userPos) + " to set starting positions.");

            // need to pick a player to go first if it hasn't been done already
            if (game.getCurrentTurn() == null)
            {
                game.setCurrentTurn(_rand.nextBoolean());
            }

            // make sure other player didn't leave
            if (game.getWinner() != null)
            {
                // TODO: win game
                System.out.println("GameInstanceController: " + game.getWinnerName()
                        + " has won a game.");
                rspMsg.setSuccessful(false);
                rspMsg.setErrorMsg("Game Over.");
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
        System.out.println("GameInstanceController: user: " + user
                + "'s positions are set and opponent: " + game.getOpponent(userPos)
                + "'s positions are set. Waiting for moves.");
        rspMsg.setSuccessful(true);
        rspMsg.setTurn(game.getCurrentTurn());
        rspMsg.setField(game.getFieldSymbolsByPlayer(userPos));
        return;
    }
}
