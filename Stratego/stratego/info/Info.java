package stratego.info;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import stratego.user.Validator;

/**
 * Servlet implementation class Info
 */
@SuppressWarnings("serial")
@WebServlet("/Info")
public class Info extends HttpServlet
{
    private static final String CLASS_LOG = "Info: ";

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Info()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    private void logMsg(final String msg)
    {
        System.out.println(CLASS_LOG + msg);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException
    {
        PrintWriter output = response.getWriter();
        InfoMessage rspMsg = new InfoMessage();

        // check that user is logged in
        final String user = (String) request.getSession().getAttribute("user");
        if (Validator.emptyString(user))
        {
            logMsg("User not logged in.");
            rspMsg.setSuccessful(false);
            rspMsg.setLogMsg("User not logged in.");
            output.print(rspMsg.getMessage());
            return;
        }

        // check that actionType is set
        final String actionType = request.getParameter("actionType");
        if (Validator.emptyString(actionType))
        {
            rspMsg.setLogMsg("No Action Type.");
            return;
        }

        // get a response based on the actiontype

        switch (actionType)
        {
            case "getCurrentUsers":
                rspMsg.setUsers();
                break;
            case "getHighScores":
                rspMsg.setHighScores();
                break;
            default:
                rspMsg.setSuccessful(false);
                rspMsg.setLogMsg("Invalid Action Type.");
        }
        output.print(rspMsg.getMessage());
    }

}
