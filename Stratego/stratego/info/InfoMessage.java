package stratego.info;

import org.json.JSONException;
import org.json.JSONObject;

public class InfoMessage
{
    private JSONObject message = new JSONObject();
    private boolean isSuccessful = true;
    private String logMsg = null;

    public InfoMessage()
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
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return message.toString();
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
}
