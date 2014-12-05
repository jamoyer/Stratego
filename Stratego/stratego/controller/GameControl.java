package stratego.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

import stratego.model.Field;
import stratego.model.ResponseMessage;
import stratego.user.Validator;

/**
 * Servlet implementation class ReqResHandler
 */
@SuppressWarnings("serial")
@WebServlet("/GameControl")
public class GameControl extends HttpServlet
{

    public GameControl()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        GameInstanceController gameController = GameInstanceController.getInstanceController();
        PrintWriter output = response.getWriter();
        ResponseMessage rspMsg = new ResponseMessage();

        // check that actionType is set
        final String actionType = request.getParameter("actionType");
        if (Validator.emptyString(actionType))
        {
            System.out.println("GameControl: No Action Type.");
            rspMsg.setSuccessful(false);
            rspMsg.setErrorMsg("No Action Type.");
            output.print(rspMsg.getMessage());
            return;
        }

        // check that user is logged in
        final String user = (String) request.getSession().getAttribute("user");
        if (Validator.emptyString(user))
        {
            System.out.println("GameControl: User not logged in.");
            rspMsg.setSuccessful(false);
            rspMsg.setErrorMsg("User not logged in.");
            output.print(rspMsg.getMessage());
            return;
        }

        switch (actionType)
        {
            case "newGame":
                System.out.println("New Game Request by user: " + user);
                gameController.newGame(rspMsg, user);
                break;
            case "setPositions":
                System.out.println("Set Positions Request by user: " + user);
                // get starting positions
                String positions = request.getParameter("positions");
                if (positions == null || Validator.emptyString(positions))
                {
                    System.out.println("GameControl: positions not set.");
                    rspMsg.setSuccessful(false);
                    rspMsg.setErrorMsg("Positions not set correctly.");
                    output.print(rspMsg.getMessage());
                    return;
                }
                char[][] field = convertStringToField(positions);
                if (field == null)
                {
                    System.out.println("GameControl: field from positions is null.");
                    rspMsg.setSuccessful(false);
                    rspMsg.setErrorMsg("Positions not set correctly.");
                    output.print(rspMsg.getMessage());
                    return;
                }
                gameController.setPositions(rspMsg, user, field);
                break;
            case "moveUnit":
                break;
            case "quitGame":
                break;
            case "getCurrentGame":
                break;
            default:
                rspMsg.setSuccessful(false);
                rspMsg.setErrorMsg("Invalid Action Type.");
        }

        // prints the json response message
        output.print(rspMsg.getMessage());
    }

    private char[][] convertStringToField(final String positions)
    {
        char[][] field = new char[Field.getStartingPlayerRowCount()][Field.getColumnCount()];
        try
        {
            JSONArray jsonField = new JSONArray(positions);
            if (jsonField.length() != Field.getStartingPlayerRowCount())
            {
                System.out.println("Positions are not the correct size.");
                return null;
            }
            for (int i = 0; i < jsonField.length(); i++)
            {
                JSONArray jsonFieldRow = jsonField.getJSONArray(i);
                if (jsonFieldRow.length() != Field.getColumnCount())
                {
                    System.out.println("Positions are not the correct size.");
                    return null;
                }
                for (int j = 0; j < jsonFieldRow.length(); j++)
                {
                    field[i][j] = jsonFieldRow.getString(j).charAt(0);
                }
            }
            return field;
        }
        catch (Exception e)
        {
            System.out.println("GameControl: Error converting positions to char[][] field.");
            e.printStackTrace();
            return null;
        }
    }
}
