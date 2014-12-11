package stratego.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stratego.model.Field;
import stratego.model.Position;
import stratego.model.ResponseMessage;
import stratego.user.Validator;

public class GameControlThread extends Thread
{
    private static final String CLASS_LOG = "GameControlThread: ";
    private final AsyncContext context;

    public GameControlThread(final AsyncContext context)
    {
        super();
        this.context = context;
        context.setTimeout(0);
    }

    @Override
    public void run()
    {
        final HttpServletResponse response = (HttpServletResponse) context.getResponse();
        final HttpServletRequest request = (HttpServletRequest) context.getRequest();
        context.getResponse().setContentType("application/json");
        context.getResponse().setCharacterEncoding("UTF-8");

        GameInstanceController gameController = GameInstanceController.getInstanceController();

        PrintWriter output = null;
        try
        {
            output = response.getWriter();
        }
        catch (IOException e1)
        {
            logMsg("Error getting response's PrintWriter.");
            e1.printStackTrace();
            return;
        }
        ResponseMessage rspMsg = new ResponseMessage();
        final String user = (String) request.getSession().getAttribute("user");

        JSONObject requestParams = null;
        try
        {
            requestParams = new JSONObject(request.getParameter("data"));
        }
        catch (JSONException e1)
        {
            logMsg("Request is incorrect format.");
            e1.printStackTrace();
            rspMsg.setSuccessful(false);
            rspMsg.setLogMsg("Request is incorrect format.");
            output.print(rspMsg.getMessage());
            output.flush();
            AppContext.removeContext(user);
            context.complete();
            return;
        }

        // check that actionType is set
        String actionType = null;
        try
        {
            actionType = requestParams.getString("actionType");
        }
        catch (JSONException e1)
        {
            e1.printStackTrace();
        }
        if (Validator.emptyString(actionType))
        {
            logMsg("No Action Type.");
            rspMsg.setSuccessful(false);
            rspMsg.setLogMsg("No Action Type.");
            output.print(rspMsg.getMessage());
            output.flush();
            AppContext.removeContext(user);
            context.complete();
            return;
        }

        switch (actionType)
        {
            case "newGame":
                logMsg("New Game Request by user: " + user);
                gameController.newGame(rspMsg, user);
                break;

            case "setPositions":
                logMsg("Set Positions Request by user: " + user);

                // get starting positions
                String positions = null;
                try
                {
                    positions = requestParams.getString("positions");
                }
                catch (JSONException e1)
                {
                    e1.printStackTrace();
                }
                if (positions == null || Validator.emptyString(positions))
                {
                    logMsg("Positions not set.");
                    rspMsg.setSuccessful(false);
                    rspMsg.setLogMsg("Positions not set correctly.");
                    break;
                }

                char[][] field = convertStringToField(positions);
                if (field == null)
                {
                    logMsg("Field from positions is null.");
                    rspMsg.setSuccessful(false);
                    rspMsg.setLogMsg("Positions not set correctly.");
                    break;
                }

                gameController.setPositions(output, rspMsg, user, field);

                AppContext.removeContext(user);
                context.complete();
                return;

            case "moveUnit":
                String s = null;
                try
                {
                    s = requestParams.getString("source");
                }
                catch (JSONException e2)
                {
                    e2.printStackTrace();
                }
                if (s == null || Validator.emptyString(s))
                {
                    logMsg("source not set.");
                    rspMsg.setSuccessful(false);
                    rspMsg.setLogMsg("Source not set correctly.");
                    break;
                }

                String d = null;
                try
                {
                    d = requestParams.getString("destination");
                }
                catch (JSONException e1)
                {
                    e1.printStackTrace();
                }
                if (d == null || Validator.emptyString(d))
                {
                    logMsg("Destination not set.");
                    rspMsg.setSuccessful(false);
                    rspMsg.setLogMsg("Destination not set correctly.");
                    break;
                }

                Position source = null;
                Position destination = null;
                try
                {
                    JSONObject jsonSource = new JSONObject(s);
                    source = new Position(jsonSource.getInt("row"), jsonSource.getInt("col"));
                    JSONObject jsonDestination = new JSONObject(d);
                    destination = new Position(jsonDestination.getInt("row"), jsonDestination.getInt("col"));
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    logMsg("Unable to parse JSON.");
                    rspMsg.setSuccessful(false);
                    rspMsg.setLogMsg("Unable to parse JSON");
                    break;
                }

                gameController.moveUnit(output, rspMsg, user, source, destination);

                AppContext.removeContext(user);
                context.complete();
                return;

            case "quitGame":
                break;

            case "getCurrentGame":
                break;

            default:
                rspMsg.setSuccessful(false);
                rspMsg.setLogMsg("Invalid Action Type.");
        }

        // prints the json response message
        output.print(rspMsg.getMessage());
        output.flush();
        AppContext.removeContext(user);
        context.complete();
    }

    private char[][] convertStringToField(final String positions)
    {
        char[][] field = new char[Field.getStartingPlayerRowCount()][Field.getColumnCount()];
        try
        {
            JSONArray jsonField = new JSONArray(positions);
            if (jsonField.length() != Field.getStartingPlayerRowCount())
            {
                logMsg("Positions are not the correct size.");
                return null;
            }
            for (int row = 0; row < jsonField.length(); row++)
            {
                JSONArray jsonFieldRow = jsonField.getJSONArray(row);
                if (jsonFieldRow.length() != Field.getColumnCount())
                {
                    logMsg("Positions are not the correct size.");
                    return null;
                }
                for (int col = 0; col < jsonFieldRow.length(); col++)
                {
                    field[row][col] = jsonFieldRow.getString(col).charAt(0);
                }
            }
            return field;
        }
        catch (Exception e)
        {
            logMsg("Error converting positions to char[][] field.");
            e.printStackTrace();
            return null;
        }
    }

    private void logMsg(final String msg)
    {
        System.out.println(CLASS_LOG + msg);
    }
}
