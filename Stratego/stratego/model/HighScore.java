package stratego.model;

import static stratego.database.DatabaseAccess.DB_URL;
import static stratego.database.DatabaseAccess.DRIVER;
import static stratego.database.DatabaseAccess.PASS;
import static stratego.database.DatabaseAccess.USER;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class HighScore
{
    private static final String CLASS_LOG = "Highscore: ";

    public HighScore(final String winner, final String loser, final GameEnd endType)
    {       
        updateHighScores(endType, winner, loser);
        updateUserScore(endType, winner, loser);    
    }
    
    public static String getHighScores()
    {
        return "";
    }
    
    private void updateHighScores(GameEnd endType, final String winner, final String loser)
    {
        logMsg("Adding game to highscores table");
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
            String sql = "INSERT INTO highscores (winner, loser, endType) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, winner);
            stmt.setString(2, loser);
            stmt.setString(3, endType.getEndType());

            try
            {
                stmt.executeUpdate();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }

            // STEP 6: Clean-up environment
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
    }
    
    private void updateUserScore(GameEnd endType, final String winner, final String loser)
    {
        logMsg("Updating user table with new scores");
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
            String sql = "UPDATE users SET score = score + ? WHERE user = ?";
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, endType.getWinValue());
            stmt.setString(2, winner);

            try
            {
                if (stmt.executeUpdate() == 1)
                {
                    // success
                    logMsg("successfully updated winner's score");
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();

                // failure
                logMsg("score update failed");
            }
            
            stmt.setInt(1, endType.getLoseValue());
            stmt.setString(2, loser);

            try
            {
                if (stmt.executeUpdate() == 1)
                {
                    // success
                    logMsg("successfully updated loser's score");
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();

                // failure
                logMsg("score update failed");
            }

            // STEP 6: Clean-up environment
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
        
    }
    
    private void logMsg(final String msg)
    {
        System.out.println(CLASS_LOG + msg);
    }

}