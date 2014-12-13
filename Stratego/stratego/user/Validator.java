package stratego.user;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import stratego.AppContext;
import stratego.controller.GameControlMessage;
import stratego.database.DatabaseAccess;
import stratego.model.GameEnd;
import stratego.model.GameInstance;

/**
 * Servlet implementation class Validator
 */
@SuppressWarnings("serial")
@WebServlet("/Validator")
public class Validator extends HttpServlet
{
    private static final String CLASS_LOG = "Validator: ";
    private static final String ERROR_MESSAGE = "ERROR: ";
    private static final String SUCCESS_MESSAGE = "SUCCESS: ";

    /*
     * Methods should put information in here for the client.
     */

    public Validator()
    {
        super();
    }

    private void logMsg(final String msg)
    {
        System.out.println(CLASS_LOG + msg);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException
    {
        PrintWriter output = response.getWriter();

        // check that actionType is set
        final String actionType = request.getParameter("actionType");
        if (emptyString(actionType))
        {
            output.write(ERROR_MESSAGE + "No Action Type.");
            return;
        }

        final String username = request.getParameter("username");
        final String password = request.getParameter("password");

        // get a response based on the actiontype
        GameControlMessage rspMsg = new GameControlMessage();
        boolean isSuccessful = false;
        switch (actionType)
        {
            case "login":
                isSuccessful = validateUser(rspMsg, username, password);
                if (isSuccessful)
                {
                    request.getSession().setAttribute("user", username);
                }
                break;
            case "signup":
                isSuccessful = createUser(rspMsg, username, password);
                if (isSuccessful)
                {
                    request.getSession().setAttribute("user", username);
                }
                break;
            case "logout":
                String user = (String) request.getSession().getAttribute("user");
                request.getSession().setAttribute("user", null);

                // need to end any games this user is in
                GameInstance game = AppContext.getGame(user);
                if (game != null)
                {
                    // the opponent wins by default
                    game.setWinner(GameInstance.negatePosition(game.getPlayerPosition(user)),
                                   Validator.currentTimeSeconds(), GameEnd.Rage);
                }
                isSuccessful = true;
                rspMsg.setLogMsg(user + " logged out.");
                response.sendRedirect("/Stratego/login.jsp");
                break;
            default:
                rspMsg.setSuccessful(false);
                rspMsg.setLogMsg("Invalid Action Type.");
        }

        // send the response back to the client
        if (isSuccessful)
        {
            rspMsg.setLogMsg(SUCCESS_MESSAGE + rspMsg.getLogMsg());
            logMsg(SUCCESS_MESSAGE + rspMsg.getLogMsg());
        }
        else
        {
            rspMsg.setLogMsg(ERROR_MESSAGE + rspMsg.getLogMsg());
            logMsg(rspMsg.getLogMsg());
        }
        output.print(rspMsg.getMessage());
    }

    /**
     * Given a username and password, checks if the user exists and the password
     * is correct.
     * 
     * @param username
     * @param password
     * @param password2
     * @return true for success and false for failure of logging in.
     */
    private boolean validateUser(GameControlMessage rspMsg, final String username, final String password)
    {
        if (emptyString(username))
        {
            rspMsg.setSuccessful(false);
            rspMsg.setLogMsg("Username not set.");
            return false;
        }
        if (emptyString(password))
        {
            rspMsg.setSuccessful(false);
            rspMsg.setLogMsg("Password not set.");
            return false;
        }

        boolean isSuccessful = false;
        PreparedStatement stmt = null;
        try
        {
            String sql = "SELECT user FROM users WHERE user=? AND password=?";
            stmt = DatabaseAccess.prepareSQL(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            // STEP 5: Extract data from result set
            if (rs.next() && username.equals(rs.getString("user")))
            {
                rspMsg.setSuccessful(true);
                rspMsg.setLogMsg("Access Granted for " + username);
                isSuccessful = true;
            }
            else
            {
                rspMsg.setSuccessful(false);
                rspMsg.setLogMsg("Access Denied for " + username);
                isSuccessful = false;
            }

            // STEP 6: Clean-up environment
            rs.close();
            stmt.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return isSuccessful;
    }

    private boolean createUser(GameControlMessage rspMsg, final String username, final String password)
    {
        if (emptyString(username))
        {
            rspMsg.setSuccessful(false);
            rspMsg.setLogMsg("Username not set.");
            return false;
        }
        if (emptyString(password))
        {
            rspMsg.setSuccessful(false);
            rspMsg.setLogMsg("Password not set.");
            return false;
        }

        boolean isSuccessful = false;
        PreparedStatement stmt = null;
        try
        {
            String sql = "INSERT INTO users (user, password, score) VALUES (?, ?, 0)";
            stmt = DatabaseAccess.prepareSQL(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);

            try
            {
                if (stmt.executeUpdate() == 1)
                {
                    // success
                    rspMsg.setSuccessful(true);
                    rspMsg.setLogMsg("User: " + username + " created.\nAccess Granted for " + username);
                    isSuccessful = true;
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();

                // failure
                rspMsg.setSuccessful(false);
                rspMsg.setLogMsg("User: " + username + " already exists.\nAccess Denied for " + username);
                isSuccessful = false;
            }

            // STEP 6: Clean-up environment
            stmt.close();
        }
        catch (SQLException se)
        {
            // Handle errors for JDBC
            se.printStackTrace();
        }

        return isSuccessful;
    }

    public static boolean emptyString(final String string)
    {
        return string == null || string.isEmpty();
    }

    public static long currentTimeSeconds()
    {
        return System.currentTimeMillis() / 1000;
    }
}
