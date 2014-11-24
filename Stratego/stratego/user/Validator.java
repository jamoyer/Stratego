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
@WebServlet("/Validator")
public class Validator extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost/stratego";
    private static final String USER = "root";
    private static final String PASS = "12323434qwewerer";

    private static final String ERROR_MESSAGE = "ERROR: ";
    private static final String SUCCESS_MESSAGE = "SUCCESS: ";

    public Validator()
    {
        super();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        PrintWriter output = response.getWriter();

        // check that actionType is set
        String actionType = request.getParameter("actionType");
        if (actionType == null || actionType.isEmpty())
        {
            output.write(ERROR_MESSAGE + "No Action Type.");
            return;
        }

        // get a response based on the actiontype
        String responseMessage = null;
        switch (actionType)
        {
            case "login":
                responseMessage = doLogin(request.getParameter("username"),
                        request.getParameter("password"));
                break;
            case "signup":
                responseMessage = doSignup(request.getParameter("username"),
                        request.getParameter("password"));
                break;
            default:
                responseMessage = ERROR_MESSAGE + "Invalid Action Type.";
        }

        // send the response back to the client
        output.write(responseMessage);
    }

    private static String doLogin(final String username, final String password)
    {
        if (username == null || username.isEmpty())
        {
            return ERROR_MESSAGE + "No Username.";
        }

        if (password == null || password.isEmpty())
        {
            return ERROR_MESSAGE + "No Username.";
        }

        String message = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        try
        {
            // STEP 2: Register JDBC driver
            Class.forName(DRIVER);

            // STEP 3: Open a connection
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // STEP 4: Execute a prepared query
            String sql = "SELECT user FROM users WHERE user=? AND password=?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            // STEP 5: Extract data from result set
            if (rs.next() && username.equals(rs.getString("user")))
            {
                message = SUCCESS_MESSAGE + "Access Granted.";
            }
            else
            {
                message = ERROR_MESSAGE + "Access Denied.";
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
            }// nothing we can do
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
        return message;
    }

    private static String doSignup(final String username, final String password)
    {
        return null;
    }
}
