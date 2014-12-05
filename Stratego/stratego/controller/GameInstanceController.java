package stratego.controller;

import java.util.LinkedList;
import java.util.List;

import stratego.model.GameInstance;
import stratego.model.PlayerPosition;
import stratego.model.ResponseMessage;
import stratego.user.Validator;

public class GameInstanceController
{
    // 10 minutes is the max time to queue for a new game
    private static final long MAX_QUEUE_TIME_TO_START_GAME_SECONDS = 60;
    private static final long THREAD_SLEEP_SECONDS = 1;
    private static final long MODERATOR_THREAD_SLEEP_SECONDS = 5;
    // 3 minutes of inactivity allowed for each player before game dies
    private static final int TIME_OUT_SECONDS = 180;

    private final List<GameInstance> _gameInstances;

    private static GameInstanceController _instController = null;

    private GameInstanceController()
    {
        _gameInstances = new LinkedList<GameInstance>();

        // end games that have met their time limit
        Thread gameModerator = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    long now = Validator.currentTimeSeconds();
                    for (GameInstance game : _gameInstances)
                    {
                        // only check games that have two players set
                        if (game.getTopPlayer() != null)
                        {
                            if ((now - game.getBottomPlayerLastResponseTime()) > TIME_OUT_SECONDS)
                            {
                                game.setWinner(PlayerPosition.TOP_PLAYER);
                            }
                            else if ((now - game.getTopPlayerLastResponseTime()) > TIME_OUT_SECONDS)
                            {
                                game.setWinner(PlayerPosition.BOTTOM_PLAYER);
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
        gameModerator.start();
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
        for (GameInstance game : _gameInstances)
        {
            if (game.getBottomPlayer().equals(user)
                    || (game.getTopPlayer() != null && game.getTopPlayer().equals(user)))
            {
                System.out.println("GameInstanceController: user: " + user + "'s game found.");
                return game;
            }
        }
        System.out.println("GameInstanceController: user: " + user + "'s game was not found.");
        return null;
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
        for (GameInstance game : _gameInstances)
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
        }
        else
        {
            System.out
                    .println("GameInstanceController: user: " + user + " is creating a new game.");
            game = new GameInstance(user, Validator.currentTimeSeconds());
            _gameInstances.add(game);
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

        // Hurray, everything is set up!
        // Create response so players can choose starting positions
        System.out.println("GameInstanceController: user: " + user
                + "'s game is all good to go, sending response.");
        rspMsg.setSuccessful(true);
        if (!game.getBottomPlayer().equals(user))
        {
            rspMsg.setOpponent(game.getBottomPlayer());
            rspMsg.setPlayerPosition(PlayerPosition.TOP_PLAYER);
        }
        else
        {
            rspMsg.setOpponent(game.getTopPlayer());
            rspMsg.setPlayerPosition(PlayerPosition.BOTTOM_PLAYER);
        }
    }

    public void setPositions(ResponseMessage rspMsg, final String user, final char[][] positions)
    {
        // TODO: set player starting positions
    }
}
