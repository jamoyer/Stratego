package stratego.controller;

import org.json.JSONException;

import stratego.ResponseMessage;
import stratego.model.GameInstance;
import stratego.model.PlayerPosition;

/**
 * This class should be used for all responses from calls to GameControl so that
 * responses are consistent. Set the message attributes as needed and then use
 * getMessage() to get the JSON response.
 * 
 * @author Jacob Moyer
 *
 */
public class GameControlMessage extends ResponseMessage
{
    private boolean isPingResponse = false;
    private PlayerPosition position = null;
    private String opponent = null;
    private char[][] field = null;
    private boolean gameWon = false;
    private boolean gameLost = false;
    private PlayerPosition turn = null;
    private GameInstance game = null;
    private String opponentTheme = null;

    public GameControlMessage()
    {
    }

    /**
     * Returns the JSON string representation of this response message.
     * 
     * @return
     */
    @Override
    public String getMessage()
    {
        try
        {
            message.put("isSuccessful", isSuccessful);
            message.put("logMsg", logMsg);
            message.put("field", field);
            if (game != null)
            {
                message.put("playerNum", GameControlMessage.convertPlayerPosToPlayerNum(position));
                message.put("opponent", game.getOpponent(position));
                message.put("opponentTheme", game.getOpponentTheme(position));
                message.put("gameWon", position.equals(game.getWinner()));
                message.put("gameLost", GameInstance.negatePosition(position).equals(game.getWinner()));
                message.put("currentTurn", GameControlMessage.convertPlayerPosToPlayerNum(game.getTurn()));
            }
            else
            {
                message.put("playerNum", GameControlMessage.convertPlayerPosToPlayerNum(position));
                message.put("opponent", opponent);
                message.put("opponentTheme", opponentTheme);
                message.put("gameWon", gameWon);
                message.put("gameLost", gameLost);
                message.put("currentTurn", GameControlMessage.convertPlayerPosToPlayerNum(turn));
                message.put("isPingResponse", isPingResponse);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return message.toString();
    }

    public void setPingResponse(boolean isPingResponse)
    {
        this.isPingResponse = isPingResponse;
    }

    public void setGame(final GameInstance game, final String user)
    {
        if (game == null || user == null)
        {
            return;
        }
        this.game = game;
        this.position = game.getPlayerPosition(user);
    }

    private static int convertPlayerPosToPlayerNum(final PlayerPosition pos)
    {
        if (pos != null)
        {
            return pos.getPlayerNumber();
        }
        return 0;
    }

    /**
     * Which player's turn it currently is.
     * 
     * @param turn
     */
    public void setTurn(PlayerPosition turn)
    {
        this.turn = turn;
    }

    public String getLogMsg()
    {
        return this.logMsg;
    }

    /**
     * Either this message is for the top player or the bottom player of the
     * field.
     * 
     * @param position
     */
    public void setPlayerPosition(final PlayerPosition position)
    {
        this.position = position;
    }

    /**
     * The opponents username.
     * 
     * @param opponent
     */
    public void setOpponent(String opponent)
    {
        this.opponent = opponent;
    }

    /**
     * A double array of characters that will represent the field to a single
     * player.
     * 
     * @param field
     */
    public void setField(char[][] field)
    {
        this.field = field;
    }

    /**
     * Whether the player has won the game or not.
     * 
     * @param gameWon
     */
    public void setGameWon(boolean gameWon)
    {
        this.gameWon = gameWon;
    }

    /**
     * Whether the player has lost the game or not.
     * 
     * @param gameLost
     */
    public void setGameLost(boolean gameLost)
    {
        this.gameLost = gameLost;
    }

    public void setOpponentTheme(String theme)
    {
        this.opponentTheme = theme;
    }
}
