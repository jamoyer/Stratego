package stratego;

import org.json.JSONObject;

public abstract class ResponseMessage
{
    protected JSONObject message = new JSONObject();
    protected boolean isSuccessful = true;
    protected String logMsg = null;

    public ResponseMessage()
    {
    }

    /**
     * Returns the JSON string representation of this response message.
     * 
     * @return
     */
    public abstract String getMessage();

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
