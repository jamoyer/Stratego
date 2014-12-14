package stratego.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import stratego.database.DatabaseAccess;
import stratego.info.InfoMessage;

public class HighScore
{
    private static final String CLASS_LOG = "Highscore: ";

    public HighScore(final String winner, final String loser, final GameEnd endType)
    {
        if (winner!=null)
        {
        updateHighScores(endType, winner, loser);
        updateUserScore(endType, winner, loser);
        }
    }

    public static List<String[]> getHighScores()
    {
        logMsg("Getting scores");
        PreparedStatement stmt = null;
        List<String[]> table = new ArrayList<>();
        String sql = "SELECT user, score FROM users ORDER BY score DESC LIMIT 10";
        stmt = DatabaseAccess.prepareSQL(sql);
        
        try
        {
            ResultSet rs = stmt.executeQuery();
            int nCol = rs.getMetaData().getColumnCount();
            while( rs.next()) {
                String[] row = new String[nCol];
                for( int iCol = 1; iCol <= nCol; iCol++ ){
                    row[iCol-1] = rs.getObject( iCol ).toString();
                }
                table.add( row );
            }
            stmt.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();

            // failure
            logMsg("Returning empty list");
        }
        
        return table;
    }

    private void updateHighScores(GameEnd endType, final String winner, final String loser)
    {
        logMsg("Adding game to highscores table");
        PreparedStatement stmt = null;
        try
        {
            String sql = "INSERT INTO highscores (winner, loser, endType) VALUES (?, ?, ?)";
            stmt = DatabaseAccess.prepareSQL(sql);

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
        }
        catch (SQLException se)
        {
            // Handle errors for JDBC
            se.printStackTrace();
        }
    }

    private void updateUserScore(GameEnd endType, final String winner, final String loser)
    {
        logMsg("Updating user table with new scores");
        PreparedStatement stmt = null;
        try
        {
            String sql = "UPDATE users SET score = score + ? WHERE user = ?";
            stmt = DatabaseAccess.prepareSQL(sql);

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
        }
        catch (SQLException se)
        {
            // Handle errors for JDBC
            se.printStackTrace();
        }
    }

    private static void logMsg(final String msg)
    {
        System.out.println(CLASS_LOG + msg);
    }

}