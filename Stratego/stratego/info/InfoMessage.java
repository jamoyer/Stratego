package stratego.info;

import org.json.JSONException;

import stratego.ResponseMessage;

public class InfoMessage extends ResponseMessage
{

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
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return message.toString();
    }
}
