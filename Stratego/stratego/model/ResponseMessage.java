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
    private String errorMsg = null;
    private PlayerPosition position = null;
    private String opponent = null;
    private char[][] field = null;
    private boolean gameWon = false;
    private boolean gameLost = false;
    private PlayerPosition turn = null;

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
            message.put("errorMsg", errorMsg);
            if (position != null)
            {
                message.put("playerNum", position.getPlayerNumber());
            }
            else
            {
                message.put("playerNum", 0);
            }
            message.put("opponent", opponent);
            message.put("field", field);
            message.put("gameWon", gameWon);
            message.put("gameLost", gameLost);
            if (turn != null)
            {
                message.put("currentTurn", turn.getPlayerNumber());
            }
            else
            {
                message.put("currentTurn", 0);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return message.toString();
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
    public void setErrorMsg(String errorMsg)
    {
        this.errorMsg = errorMsg;
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
}
