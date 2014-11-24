/*
 * Calls GameControl and attempts to join the user to a new game.
 */
function joinNewGame()
{
    $.ajax(
    {
        url : "/Stratego/GameControl",
        type : "POST",
        data :
        {
            actionType : "newGame"
        },
        dataType : 'json',
        success : function(data)
        {
            if (!data.isSuccessful)
            {
                // could not join game
                // error message will be stored in errorMsg
                // do something with it

                var errorMsg = data.errorMsg;
                alert(errorMsg);
                return;
            }

            /*
             * player will either be 1 or 2. Player 1 = bottom, player 2 = top.
             */
            var playerNum = data.playerNum;

            // opponent's username, display this somewhere
            var opponent = data.opponent;

            // allow the user to choose starting positions, etc ...
        }
    });
}

/*
 * Sends the user's chosen starting positions to the GameControl. GameControl returns the inital
 * 10x10 field to display.
 */
function setStartPositions()
{
    var startingPositions = [ [ 'F', 'B', 'B', 'B', 'B', 'B', 'B', 'S', '9', '9' ],
            [ '9', '9', '9', '9', '9', '9', '8', '8', '8', '8' ],
            [ '8', '7', '7', '7', '7', '6', '6', '6', '6', '5' ],
            [ '5', '5', '5', '4', '4', '4', '3', '3', '2', '1' ] ];

    $.ajax(
    {
        url : "/Stratego/GameControl",
        type : "POST",
        dataType : "json",
        data :
        {
            positions : startingPositions
        },
        success : function(data)
        {
            if (!data.isSuccessful)
            {
                // could not start game with these positions
                // error message will be stored in errorMsg
                // do something with it

                var errorMsg = data.errorMsg;
                alert(errorMsg);
                return;
            }

            // the 10x10 field will be stored in data.field.
            // This will consist of a double array of characters, each corresponding some unit or
            // tile to display, the mapping is defined in home.jsp
            var field = data.field;
            drawField(field);
        }
    });
}

// Calls GameControl attempting to move the source to the destination. GameControl will return twice
// under a normal successful move. First it will return the field after moving the unit. Then it
// will do a final return when the opponent has moved. There will be no second return if the unit
// won the game on his move. GameControl will return an error message if the move was unsuccessful
// for some reason.

// Parameters:
// source: the location of the unit {row:y, col:x}
// destination: where the unit should move to {row:y, col:x}

function moveUnit(source, destination)
{
    // need to manually handle readystatechange because there is
    // no way to do it in pure jquery apparently
    var xmlReq = $.ajax(
    {
        url : "/Stratego/GameControl",
        type : "POST",
        data :
        {
            source : source,
            destination : destination
        }
    });

    xmlReq.onreadystatechange = function()
    {
        // convert the responseText into JSON
        var data = JSON.parse(xmlReq.responseText);

        if (xmlReq.readyState === 3)
        {
            // A ready state of 3 means that the move was successful and we are waiting for the
            // opponent to take their turn. There should be no error message here.

            // the 10x10 field will be stored in data.field.
            // This will consist of a double array of characters, each corresponding some unit or
            // tile to display, the mapping is defined in home.jsp
            var field = data.field;
            drawField(field);
        }
        if (xmlReq.readyState === 4)
        {
            if (!data.isSuccessful)
            {
                // could not move unit for some reason
                // error message will be stored in errorMsg
                // do something with it

                var errorMsg = data.errorMsg;
                alert(errorMsg);

                // the field will still come back if the move was unsuccessful, it will remain
                // unchanged
                // though, we can redraw it or not

                // the 10x10 field will be stored in data.field.
                // This will consist of a double array of characters, each corresponding some unit
                // or
                // tile to display, the mapping is defined in home.jsp
                var field = data.field;
                drawField(field);
                return;
            }

            if (data.gameWon)
            {
                // do stuff for a victorious game
            }
            else if (data.gameLost)
            {
                // do stuff for a lost game
            }
            else
            {
                // just do regular display action
            }
        }
    };
}

/*
 * Draws a field on the screen given the 10x10 field array
 */
function drawField(field)
{

}