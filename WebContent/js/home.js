/*
 * Returns the status of the current game running for the user. 
 * isSuccessful will be false if the user is not in a game.
 */
function getCurrentGame()
{
    var data =
    {
        actionType : "getCurrentGame"
    };
    makeGameControlRequest(data);
}
/*
 * Refreshes the last response time for the user's game.
 */
function pingGameControl()
{
    var data =
    {
        actionType : "ping"
    };
    var xmlReq = new XMLHttpRequest();
    xmlReq.open('POST', '/Stratego/GameControl', true);
    xmlReq.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xmlReq.onreadystatechange = function()
    {
        // should try to detect if there is no response meaning that the connection is bad
    }
    xmlReq.send("data=" + JSON.stringify(data));
}

/*
 * Calls GameControl and attempts to join the user to a new game.
 */
function joinNewGame()
{
    var data =
    {
        actionType : "newGame"
    };
    makeGameControlRequest(data);
}

/*
 * Sends the user's chosen starting positions to the GameControl. GameControl returns the inital
 * 10x10 field to display.
 */
function setStartPositions()
{
    var startingPositions = [ [ '1', 'B', 'B', 'B', 'B', 'B', 'B', 'S', '5', '9' ],
            [ '9', '9', '9', '9', '9', '9', '8', '8', '8', '8' ],
            [ '8', '7', '7', '7', '7', '6', '6', '6', '6', '5' ],
            [ '9', '5', '5', '4', '4', '4', '3', '3', '2', 'F' ] ];

    var theme = document.getElementById('theme').value;

    var data =
    {
        actionType : "setPositions",
        positions : JSON.stringify(startingPositions),
        theme : theme
    };

    makeGameControlRequest(data);
}

// Calls GameControl attempting to move the source to the destination. GameControl will return twice
// under a normal successful move. First it will return the field after moving the unit. Then it
// will do a final return when the opponent has moved. There will be no second return if the unit
// won the game on his move. GameControl will return an error message if the move was unsuccessful
// for some reason.

// Parameters:
// source: the location of the unit {row:y, col:x}
// destination: where the unit should move to {row:y, col:x}
function sendMoveRequest(source, destination)
{
    var data =
    {
        actionType : "moveUnit",
        source : JSON.stringify(source),
        destination : JSON.stringify(destination)
    };

    makeGameControlRequest(data);
}

/*
 * Gets values from the text box inputs and submits them as data to sendMoveRequest
 */
function moveUnit()
{
    var source = {};
    source.row = $("#sourceRow").val();
    source.col = $("#sourceCol").val();

    var destination = {};
    destination.row = $("#destinationRow").val();
    destination.col = $("#destinationCol").val();
    sendMoveRequest(source, destination);
}

function makeGameControlRequest(JSONObjectToSend)
{
    var xmlReq = new XMLHttpRequest();
    xmlReq.open('POST', '/Stratego/GameControl', true);
    xmlReq.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xmlReq.onreadystatechange = function()
    {
        function parseGameControlResponse(rspTxt)
        {
            var responseArray = [];
            var startIndex;
            for (var i = 0; i < rspTxt.length; i++)
            {
                if (rspTxt.charAt(i) === '{')
                {
                    startIndex = i;
                }
                else if (rspTxt.charAt(i) === '}')
                {
                    var objectText = rspTxt.substring(startIndex, i + 1);
                    var responseObject = JSON.parse(objectText);
                    responseArray.push(responseObject);
                }
            }
            return responseArray;
        }

        if (xmlReq.readyState === 3)
        {
            // convert the responseText into JSON
            var data = parseGameControlResponse(xmlReq.responseText);

            // display response for debugging purposes\
            $("#serverResponse").text(JSON.stringify(data));

            // if (!data.isSuccessful)
            // {
            // // could not move unit for some reason
            // // error message will be stored in errorMsg
            // // do something with it
            //
            // var errorMsg = data.errorMsg;
            // alert(errorMsg);
            // return;
            // }
            // // A ready state of 3 means that the move was successful and we are waiting for the
            // // opponent to take their turn. There should be no error message here.
            //
            // // the 10x10 field will be stored in data.field.
            // // This will consist of a double array of characters, each corresponding some unit or
            // // tile to display, the mapping is defined in home.jsp
            // var field = data.field;
            // drawField(field);
        }
        if (xmlReq.readyState === 4)
        {
            // convert the responseText into JSON
            var data = parseGameControlResponse(xmlReq.responseText);

            // display response for debugging purposes
            $("#serverResponse").text(JSON.stringify(data));

            // if (!data.isSuccessful)
            // {
            // // could not move unit for some reason
            // // error message will be stored in errorMsg
            // // do something with it
            //
            // var errorMsg = data.errorMsg;
            // alert(errorMsg);
            // return;
            // }
            //
            // if (data.gameWon != null)
            // {
            // // do stuff for a victorious game
            // }
            // else if (data.gameLost != null)
            // {
            // // do stuff for a lost game
            // }
            // else if (data.playerNum != null)
            // {
            // // do stuff for a new game
            //
            // var playerNum = data.playerNum;
            //
            // // opponent's username, display this somewhere
            // var opponent = data.opponent;
            //
            // // allow the user to choose starting positions, etc ...
            // }
            // else
            // {
            // // just do regular display action
            //
            // // the 10x10 field will be stored in data.field.
            // // This will consist of a double array of characters, each corresponding some unit
            // // or
            // // tile to display, the mapping is defined in home.jsp
            // var field = data.field;
            // drawField(field);
            // }
        }
    }
    xmlReq.send("data=" + JSON.stringify(JSONObjectToSend));
}

function quitGame()
{
    var data =
    {
        actionType : "quitGame"
    };
    makeGameControlRequest(data);
}

/*
 * Draws a field on the screen given the 10x10 field array
 */
function drawField(field)
{

}