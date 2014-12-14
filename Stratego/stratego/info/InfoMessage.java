package stratego.info;

import java.util.List;

import org.json.JSONException;

import stratego.AppContext;
import stratego.ResponseMessage;
import stratego.model.HighScore;

public class InfoMessage extends ResponseMessage
{

    private List<String[]> highscores = null;
    private List<String> users = null;
    
    public InfoMessage()
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
            message.put("highscores", highscores);
            message.put("users", users);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return message.toString();
    }
    
    public void setHighScores()
    {
        this.highscores = HighScore.getHighScores();
    }
    
    public void setUsers()
    {
        this.users = AppContext.getOnlineUsers();
    }
}
