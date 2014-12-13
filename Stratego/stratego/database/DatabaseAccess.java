package stratego.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseAccess
{
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost/stratego";
    private static final String USER = "root";
    private static final String PASS = "rootpw";

    private static final String CLASS_LOG = "DatabaseAccess: ";

    private static Connection _con = null;

    private static Connection getConnection() throws ClassNotFoundException, SQLException
    {
        if (_con == null)
        {
            synchronized (DatabaseAccess.class)
            {
                if (_con == null)
                {
                    // Register JDBC driver
                    Class.forName(DRIVER);

                    // Open a connection
                    _con = DriverManager.getConnection(DB_URL, USER, PASS);
                }
            }
        }
        return _con;
    }

    public static PreparedStatement prepareSQL(final String sql)
    {
        try
        {
            // STEP 4: Execute a prepared query
            // prepared statements are better than escaping strings and
            // guarantee there is no sql injection
            return DatabaseAccess.getConnection().prepareStatement(sql);
        }
        catch (ClassNotFoundException | SQLException e)
        {
            logMsg("Unable to get database connection.");
            e.printStackTrace();
        }
        return null;
    }

    private static void logMsg(final String msg)
    {
        System.out.println(CLASS_LOG + msg);
    }
}
