package stratego.controller;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import stratego.AppContext;
import stratego.user.Validator;

/**
 * Servlet implementation class ReqResHandler
 */
@SuppressWarnings("serial")
@WebServlet(urlPatterns = { "/GameControl" }, name = "GameControl", asyncSupported = true)
public class GameControl extends HttpServlet
{
    private static final String CLASS_LOG = "GameControl: ";

    public GameControl()
    {
        super();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException
    {
        AsyncContext context = request.startAsync();

        // check that user is logged in
        final String user = (String) request.getSession().getAttribute("user");
        if (Validator.emptyString(user))
        {
            GameControlMessage rspMsg = new GameControlMessage();
            logMsg("User not logged in.");
            rspMsg.setSuccessful(false);
            rspMsg.setLogMsg("User not logged in.");
            response.getWriter().print(rspMsg.getMessage());
            response.getWriter().flush();
            context.complete();
            return;
        }

        AppContext.putContext(user, context);
        new GameControlThread(context).start();
    }

    private void logMsg(final String msg)
    {
        System.out.println(CLASS_LOG + msg);
    }
}
