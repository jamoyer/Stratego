package stratego.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stratego.AppContext;
import stratego.model.Field;
import stratego.model.GameEnd;
import stratego.model.GameInstance;
import stratego.model.Position;
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
        GameControlMessage rspMsg = new GameControlMessage();
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
                // get theme
                String theme = null;
                try
                {
                    positions = requestParams.getString("positions");
                    theme = requestParams.getString("theme");
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

                gameController.setPositions(output, rspMsg, user, field, theme);

                AppContext.removeContext(user);
                context.complete();
                return;

            case "moveUnit":
                Position source = getPositionFromRequest(rspMsg, "source", requestParams);
                Position destination = getPositionFromRequest(rspMsg, "destination", requestParams);
                if (source == null || destination == null)
                {
                    break;
                }

                gameController.moveUnit(output, rspMsg, user, source, destination);

                AppContext.removeContext(user);
                context.complete();
                return;

            case "quitGame":
                // need to end any games this user is in
                GameInstance gameToQuit = AppContext.getGame(user);
                if (gameToQuit != null)
                {
                    // the opponent wins by default
                    gameToQuit.setWinner(GameInstance.negatePosition(gameToQuit.getPlayerPosition(user)),
                                         Validator.currentTimeSeconds(), GameEnd.Rage);
                }
                rspMsg.setGame(gameToQuit, user);
                rspMsg.setLogMsg(user + " quit!");
                break;

            case "getCurrentGame":
                // need to get the game this user is in and set the rspmsg
                GameInstance currentGame = AppContext.getGame(user);
                if (currentGame != null)
                {
                    rspMsg.setSuccessful(true);
                    rspMsg.setGame(currentGame, user);
                    if (currentGame.getWinner() == null)
                    {
                        rspMsg.setField(currentGame.getFieldSymbolsByPlayer(user));
                    }
                    else
                    {
                        rspMsg.setField(currentGame.getExposedFieldByPlayer(user));
                    }
                }
                else
                {
                    rspMsg.setSuccessful(false);
                    rspMsg.setLogMsg("There is no game for " + user);
                }
                break;

            case "ping":
                /*
                 * Simply refreshes the response time so the user does not lose
                 * game due to time out. Also checks if the other user rage
                 * quit.
                 */
                AppContext.putUser(user);
                rspMsg.setPingResponse(true);
                GameInstance gameToPing = AppContext.getGame(user);
                if (gameToPing != null)
                {
                    // indicates that the game is still active
                    rspMsg.setSuccessful(true);

                    // check if the game has been won
                    if (gameToPing.getWinner() != null)
                    {
                        rspMsg.setField(gameToPing.getExposedFieldByPlayer(user));
                        rspMsg.setGame(gameToPing, user);
                    }
                    else
                    {
                        rspMsg.setLogMsg("Response time updated for " + user);
                        gameToPing.setPlayerLastResponseTime(user, Validator.currentTimeSeconds());
                    }
                }
                else
                {
                    // indicates that there is no active game
                    rspMsg.setSuccessful(false);
                }
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

    private Position getPositionFromRequest(final GameControlMessage rspMsg, final String pos, final JSONObject json)
    {
        String temp = null;
        try
        {
            temp = json.getString(pos);
        }
        catch (JSONException e2)
        {
            e2.printStackTrace();
        }
        if (temp == null || Validator.emptyString(temp))
        {
            logMsg(pos + " not set.");
            rspMsg.setSuccessful(false);
            rspMsg.setLogMsg(pos + " not set correctly.");
            return null;
        }

        Position returnPos = null;
        try
        {
            JSONObject jsonSource = new JSONObject(temp);
            returnPos = new Position(jsonSource.getInt("row"), jsonSource.getInt("col"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            logMsg("Unable to parse JSON.");
            rspMsg.setSuccessful(false);
            rspMsg.setLogMsg("Unable to parse JSON");
            return null;
        }
        return returnPos;
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
