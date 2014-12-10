package stratego.user;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Validator
 */
@SuppressWarnings("serial")
@WebServlet("/Validator")
public class Validator extends HttpServlet
{
    private static final String CLASS_LOG = "Validator: ";

    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost/stratego";
    private static final String USER = "root";
    private static final String PASS = "rootpw";

    private static final String ERROR_MESSAGE = "ERROR: ";
    private static final String SUCCESS_MESSAGE = "SUCCESS: ";

    /*
     * Methods should put information in here for the client.
     */
    private String _responseMessage;

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
        _responseMessage = null;
        boolean isSuccessful = false;
        switch (actionType)
        {
            case "login":
                isSuccessful = validateUser(username, password);
                if (isSuccessful)
                {
                    request.getSession().setAttribute("user", username);
                }
                break;
            case "signup":
                isSuccessful = createUser(username, password);
                if (isSuccessful)
                {
                    request.getSession().setAttribute("user", username);
                }
                break;
            case "logout":
                String user = (String) request.getSession().getAttribute("user");
                request.getSession().setAttribute("user", null);
                isSuccessful = true;
                _responseMessage = user + " logged out.";
                break;
            default:
                _responseMessage = "Invalid Action Type.";
        }

        // send the response back to the client
        if (isSuccessful)
        {
            _responseMessage = SUCCESS_MESSAGE + _responseMessage;
        }
        else
        {
            _responseMessage = ERROR_MESSAGE + _responseMessage;
        }
        logMsg(_responseMessage);
        response.sendRedirect("/Stratego/home.jsp");
    }

    /**
     * Given a username and password, checks if the user exists and the password
     * is correct.
     * 
     * @param username
     * @param password
     * @return true for success and false for failure of logging in.
     */
    private boolean validateUser(final String username, final String password)
    {
        if (emptyString(username))
        {
            _responseMessage = "Username not set.";
            return false;
        }
        if (emptyString(password))
        {
            _responseMessage = "Password not set.";
            return false;
        }

        boolean isSuccessful = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        try
        {
            // STEP 2: Register JDBC driver
            Class.forName(DRIVER);

            // STEP 3: Open a connection
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // STEP 4: Execute a prepared query
            // prepared statements are better than escaping strings and
            // guarantee there is no sql injection
            String sql = "SELECT user FROM users WHERE user=? AND password=?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            // STEP 5: Extract data from result set
            if (rs.next() && username.equals(rs.getString("user")))
            {
                _responseMessage = "Access Granted for " + username;
                isSuccessful = true;
            }
            else
            {
                _responseMessage = "Access Denied for " + username;
                isSuccessful = false;
            }

            // STEP 6: Clean-up environment
            rs.close();
            stmt.close();
            conn.close();
        }
        catch (SQLException se)
        {
            // Handle errors for JDBC
            se.printStackTrace();
        }
        catch (Exception e)
        {
            // Handle errors for Class.forName
            e.printStackTrace();
        }
        finally
        {
            // finally block used to close resources
            try
            {
                if (stmt != null)
                {
                    stmt.close();
                }
            }
            catch (SQLException se2)
            {
            }
            try
            {
                if (conn != null)
                {
                    conn.close();
                }
            }
            catch (SQLException se)
            {
                se.printStackTrace();
            }
        }
        return isSuccessful;
    }

    private boolean createUser(final String username, final String password)
    {
        if (emptyString(username))
        {
            _responseMessage = "Username not set.";
            return false;
        }
        if (emptyString(password))
        {
            _responseMessage = "Password not set.";
            return false;
        }

        _responseMessage = "Action unimplemented at this time.";
        return false;
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
