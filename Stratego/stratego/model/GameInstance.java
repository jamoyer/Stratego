package stratego.model;

public class GameInstance
{
    private String topPlayer = null;
    private long topPlayerLastResponseTime;
    private boolean topPlayerPositionsSet = false;

    private String bottomPlayer = null;
    private long bottomPlayerLastResponseTime;
    private boolean bottomPlayerPositionsSet = false;

    private final long initiatedTime; // seconds

    private Field field = null;
    private PlayerPosition winner = null;

    public GameInstance(final String player, final long currentTimeSeconds)
    {
        this.bottomPlayer = player;
        this.initiatedTime = currentTimeSeconds;
    }

    public void setWinner(final PlayerPosition position)
    {
        winner = position;
    }

    public long getTopPlayerLastResponseTime()
    {
        return this.topPlayerLastResponseTime;
    }

    public long getBottomPlayerLastResponseTime()
    {
        return this.bottomPlayerLastResponseTime;
    }

    public void setTopPlayerLastResponseTime(final long currentTimeSeconds)
    {
        this.topPlayerLastResponseTime = currentTimeSeconds;
    }

    public void setBottomPlayerLastResponseTime(final long currentTimeSeconds)
    {
        this.bottomPlayerLastResponseTime = currentTimeSeconds;
    }

    public long getInitiatedTimeSeconds()
    {
        return this.initiatedTime;
    }

    public String getBottomPlayer()
    {
        return this.bottomPlayer;
    }

    public void setTopPlayer(final String player)
    {
        this.topPlayer = player;
    }

    public String getTopPlayer()
    {
        return this.topPlayer;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (obj == this)
        {
            return true;
        }
        if (!(obj instanceof GameInstance))
        {
            return false;
        }
        GameInstance other = (GameInstance) obj;
        if (!this.getBottomPlayer().equals(other.getBottomPlayer()))
        {
            return false;
        }
        if (!this.getTopPlayer().equals(other.getTopPlayer()))
        {
            return false;
        }
        if (this.getInitiatedTimeSeconds() != other.getInitiatedTimeSeconds())
        {
            return false;
        }
        return true;
    }
}
