package stratego.controller;

import stratego.AppContext;
import stratego.model.GameEnd;
import stratego.model.GameInstance;
import stratego.model.PlayerPosition;
import stratego.user.Validator;

public class GameModerator implements Runnable
{
    private static final String CLASS_LOG = "GameModerator: ";

    private static final int MODERATOR_THREAD_SLEEP_SECONDS = 5;
    
    // 3 minutes allowed before
    private static final int LOGOUT_TIME_OUT = 180;

    // 3 minutes allowed for each players turn before game dies
    private static final int RAGE_TIME_OUT_SECONDS = 180;

    // 30 seconds allowed between pings before time out occurs
    private static final int DISCONNECT_TIME_OUT_SECONDS = 30;

    private static final int TIME_TO_WAIT_BEFORE_ENDING_A_FINISHED_GAME = 10;

    private void logMsg(final String msg)
    {
        System.out.println(CLASS_LOG + msg);
    }

    @Override
    public void run()
    {
        while (true)
        {
            long now = Validator.currentTimeSeconds();
            for (GameInstance game : AppContext.getGames())
            {
                // only check games that have two players set
                if (game.getTopPlayer() != null)
                {
                    // declare winners for time out scenarios
                    if (game.getTurnTimer() != null && (now - game.getTurnTimer()) > RAGE_TIME_OUT_SECONDS)
                    {
                        game.setWinner(game.getTurn(), now, GameEnd.Rage);
                        logMsg("GameInstanceModerator: " + game.getWinnerName()
                                + " has won a game due to opponent not finishing their turn.");
                    }
                    else if ((now - game.checkPlayerLastResponseTime(PlayerPosition.TOP_PLAYER)) > DISCONNECT_TIME_OUT_SECONDS)
                    {
                        game.setWinner(PlayerPosition.BOTTOM_PLAYER, now, GameEnd.Timeout);
                        logMsg("GameInstanceModerator: " + game.getWinnerName()
                                + " has won a game due to opponent time out.");
                        AppContext.removeUser(game.getLoserName());
                    }
                    else if ((now - game.checkPlayerLastResponseTime(PlayerPosition.BOTTOM_PLAYER)) > DISCONNECT_TIME_OUT_SECONDS)
                    {
                        game.setWinner(PlayerPosition.TOP_PLAYER, now, GameEnd.Timeout);
                        logMsg("GameInstanceModerator: " + game.getWinnerName()
                                + " has won a game due to opponent time out.");
                        AppContext.removeUser(game.getLoserName());
                    }
                }

                // allows users to get the final result of the board before
                // the game is destroyed
                if (game.getEndTime() != null && (now - game.getEndTime()) > TIME_TO_WAIT_BEFORE_ENDING_A_FINISHED_GAME)
                {
                    if (game.getTopPlayer() != null)
                    {
                        AppContext.removeGame(game.getTopPlayer());
                    }
                    if (game.getBottomPlayer() != null)
                    {
                        AppContext.removeGame(game.getBottomPlayer());
                    }
                }
            }
            for (String user : AppContext.getOnlineUsers())
            {
                if(now-AppContext.getUser(user)>LOGOUT_TIME_OUT)
                {
                    AppContext.removeUser(user);
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
}
