package stratego.model;

public enum GameEnd
{
    Standard(100,-25, "Standard"),
    Timeout(25,-50, "Timeout"),
    Rage(50,-100, "Rage");


    private final int winValue;
    private final int loseValue;
    private final String endType;
    
    private GameEnd(final int winValue, final int loseValue, final String endType)
    {
        this.winValue = winValue;
        this.loseValue = loseValue;
        this.endType = endType;
    }

    public int getWinValue()
    {
        return winValue;
    }

    public int getLoseValue()
    {
        return loseValue;
    }
    
    public String getEndType()
    {
        return endType;
    }
    
}
