package stratego.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class should be used for all responses from calls to GameControl so that
 * responses are consistent. Set the message attributes as needed and then use
 * getMessage() to get the JSON response.
 * 
 * @author Jacob Moyer
 *
 */
public class ResponseMessage
{
    private JSONObject message = new JSONObject();
    private boolean isSuccessful = true;
    private String logMsg = null;
    private PlayerPosition position = null;
    private String opponent = null;
    private char[][] field = null;
    private boolean gameWon = false;
    private boolean gameLost = false;
    private PlayerPosition turn = null;
    private GameInstance game = null;
    private String opponentTheme = null;

    public ResponseMessage()
    {
    }

    /**
     * Returns the JSON string representation of this response message.
     * 
     * @return
     */
    public String getMessage()
    {
        try
        {
            message.put("isSuccessful", isSuccessful);
            message.put("logMsg", logMsg);
            message.put("field", field);
            if (game != null)
            {
                message.put("playerNum", ResponseMessage.convertPlayerPosToPlayerNum(position));
                message.put("opponent", game.getOpponent(position));
                message.put("opponentTheme", game.getOpponentTheme(position));
                message.put("gameWon", position.equals(game.getWinner()));
                message.put("gameLost", GameInstance.negatePosition(position).equals(game.getWinner()));
                message.put("currentTurn", ResponseMessage.convertPlayerPosToPlayerNum(game.getTurn()));
            }
            else
            {
                message.put("playerNum", ResponseMessage.convertPlayerPosToPlayerNum(position));
                message.put("opponent", opponent);
                message.put("opponentTheme", opponentTheme);
                message.put("gameWon", gameWon);
                message.put("gameLost", gameLost);
                message.put("currentTurn", ResponseMessage.convertPlayerPosToPlayerNum(turn));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return message.toString();
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

    /**
     * Whether or not this action worked.
     * 
     * @param isSuccessful
     */
    public void setSuccessful(boolean isSuccessful)
    {
        this.isSuccessful = isSuccessful;
    }

    /**
     * The message to return if the action did not work. Should be set if and
     * only if isSuccessful is false.
     * 
     * @param errorMsg
     */
    public void setLogMsg(String errorMsg)
    {
        this.logMsg = errorMsg;
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
