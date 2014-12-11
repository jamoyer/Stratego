package stratego.model;

/**
 * Player 1 is on the bottom of the field, player 2 is on the top of the field.
 * 
 * @author Jacob Moyer
 *
 */
public enum PlayerPosition
{
    TOP_PLAYER(2),
    BOTTOM_PLAYER(1);

    private final int playerNumber;

    private PlayerPosition(final int playerNum)
    {
        this.playerNumber = playerNum;
    }

    public int getPlayerNumber()
    {
        return this.playerNumber;
    }
}
