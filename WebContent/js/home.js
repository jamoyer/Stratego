
var bankSelected = false;
var selectedFieldTile = null;
/*
 * this will ping the game server every 5 seconds and refresh the response time
 */ 
function pingForever()
{
    setTimeout(function()
    {
        pingGameControl();
        pingForever();
    }, 5000);
}

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
        if (xmlReq.readyState === 4)
        {
            // convert the responseText into JSON
            var response = JSON.parse(xmlReq.responseText);

            // display response for debugging purposes
            if (response.field != null)
            {
                $("#serverResponse").text(JSON.stringify(response));
            }
        }
    }
    xmlReq.send("data=" + JSON.stringify(data));
}

/*
 * Gets highscores
 */
function pingHighScores()
{
    var xmlReq = new XMLHttpRequest();
    xmlReq.open('POST', '/Stratego/Info', true);
    xmlReq.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xmlReq.onreadystatechange = function()
    {
        if (xmlReq.readyState === 4)
        {
            // convert the responseText into JSON
            var response = JSON.parse(xmlReq.responseText);

            // display response for debugging purposes
            if (response != null)
            {
            	alert(xmlReq.responseText);
            }
        }
    }
    xmlReq.send("actionType=getHighScores");
    
}

/*
 * Gets users logged in
 */
function pingOnlineUsers()
{
    var xmlReq = new XMLHttpRequest();
    xmlReq.open('POST', '/Stratego/Info', true);
    xmlReq.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xmlReq.onreadystatechange = function()
    {
        if (xmlReq.readyState === 4)
        {
            // convert the responseText into JSON
            var response = JSON.parse(xmlReq.responseText);

            // display response for debugging purposes
            if (response != null)
            {
            	alert(xmlReq.responseText);
            }
        }
    }
    xmlReq.send("actionType=getCurrentUsers");
    
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
    $("#buttonStartGameContainer").html('<button class="btn btn-lg btn-warning"><span class="glyphicon glyphicon-refresh glyphicon-refresh-animate"></span> Searching for Opponent...</button>');
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

/*
 * Given the field and the coordinate of a unit to move like {row:x,col:y},
 * this function returns an array of allowed move locations. The elements 
 * of the array will be {row:x,col:y,isAttack:boolean}. isAttack will be 
 * true if moving the unit to that spot would be an attack action. This 
 * function returns null or an empty array if there are no allowed moves for 
 * this coordinate.
 */
function getListOfAllowedMoves(field, coordinate)
{
    // make sure data is valid
    if (field == null || coordinate == null || coordinate.row == null || coordinate.col == null
            || coordinate.row >= field.length || coordinate.col >= field[0].length)
    {
        return null;
    }

    var type = getUnitTypeFromChar(field[coordinate.row][coordinate.col]);

    // check if unit can be moved at all
    if (type == null || type.name == "FLAG" || type.name == "OBSTACLE" || type.name == "EMPTY"
            || type.name == "BOMB" || type.name.substring(0, 5) == "ENEMY")
    {
        return null;
    }

    function checkIfAllowedMove(row, col)
    {
        if (row < 0 || col < 0 || row >= field.length || col >= field[0].length)
        {
            return null;
        }

        var position = getUnitTypeFromChar(field[row][coordinate.col]);
        var allowedMove = {};

        if (position.name != "EMPTY" && position.name.substring(0, 5) != "ENEMY")
        {
            return null;
        }
        else if (position.name == "EMPTY")
        {
            allowedMove.isAttack = false;
        }
        else
        {
            allowedMove.isAttack = true;
        }
        allowedMove.row = row;
        allowedMove.col = coordinate.col;
        return allowedMove;
    }

    var allowedMoves = [];

    if (type.name == "SCOUT")
    {
        // check down
        for (var row = coordinate.row; row < field.length; row++)
        {
            var allowedMove = checkIfAllowedMove(row, coordinate.col);
            if (allowedMove == null)
            {
                break;
            }
            allowedMoves.push(allowedMove);
        }

        // check up
        for (var row = coordinate.row; row >= 0; row--)
        {
            var allowedMove = checkIfAllowedMove(row, coordinate.col);
            if (allowedMove == null)
            {
                break;
            }
            allowedMoves.push(allowedMove);
        }

        // check left
        for (var col = coordinate.col; col >= 0; col--)
        {
            var allowedMove = checkIfAllowedMove(coordinate.row, col);
            if (allowedMove == null)
            {
                break;
            }
            allowedMoves.push(allowedMove);
        }

        // check right
        for (var col = coordinate.col; col < field[0].length; col++)
        {
            var allowedMove = checkIfAllowedMove(coordinate.row, col);
            if (allowedMove == null)
            {
                break;
            }
            allowedMoves.push(allowedMove);
        }
    }
    else
    {
        var allowedMove = checkIfAllowedMove(coordinate.row + 1, coordinate.col);
        if (allowedMove != null)
        {
            allowedMoves.push(allowedMove);
        }
        var allowedMove = checkIfAllowedMove(coordinate.row - 1, coordinate.col);
        if (allowedMove != null)
        {
            allowedMoves.push(allowedMove);
        }
        var allowedMove = checkIfAllowedMove(coordinate.row, coordinate.col + 1);
        if (allowedMove != null)
        {
            allowedMoves.push(allowedMove);
        }
        var allowedMove = checkIfAllowedMove(coordinate.row, coordinate.col - 1);
        if (allowedMove != null)
        {
            allowedMoves.push(allowedMove);
        }
    }
    return allowedMoves;
}

/*
 * Sends the user's chosen starting positions to the GameControl. GameControl returns the inital
 * 10x10 field to display.
 */
function setStartPositions(isTopPlayer)
{
    var startingField = [];
    var count = 0;
    var currentField = $(".tileRow");
    $(currentField).each(function(row)
    {
        if (count >= 6)
        {
            var startingRow = [];
            $(row).each(function(element)
            {
                var currentTileClass = $(element).attr('class').split(/\s+/)[1];
                var currentTileType = currentTileClass.substring(5);
                var symbol = getCharFromUnitType(currentTileType);
                startingRow.push(symbol);
            });
            startingField.push(startingRow);
        }
        count++;
    });

    if (isTopPlayer)
    {
        startingField = flipField(startingField);
    }

    var theme = document.getElementById('theme').value;

    var data =
    {
        actionType : "setPositions",
        positions : JSON.stringify(startingField),
        theme : theme
    };

    makeGameControlRequest(data);
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
            if (JSONObjectToSend["actionType"] = "newGame") {
            	data = data[0];
            	if (data.isSuccessful) {
            		$.ajax({
        				url: '/Stratego/_place.jsp',
        				type: 'GET',
        				success: function(response) {
        					$("#container").html(response);
        					//updateField(data.field);
        					
        					var totalCounter = 40;
        					var counters = {}
        					counters["flag"] = 1;
        					counters["bomb"] = 6;
        					counters["spy"] = 1;
        					counters["marshall"] = 1;
        					counters["general"] = 1;
        					counters["colonel"] = 2;
        					counters["major"] = 3;
        					counters["captain"] = 4;
        					counters["lieutenant"] = 4;
        					counters["sergeant"] = 4;
        					counters["miner"] = 5;
        					counters["scout"] = 8;
        					
        					$(".bankTile").on('click', function(e) {
        						if (selectedFieldTile == null)
        						{
	        						bankSelected = true;
	        						var selectedTile = this;
	        						var selectedTileClass = $(selectedTile).attr('class').split(/\s+/)[1];
	    							var selectedTileType = selectedTileClass.substring(5);
	    							if (counters[selectedTileType] > 0)
	    							{
		        						$(this).css("border-color", "red");
		        						$(".tile").on('click.bankToField', function(e) {
		        							var currentTile = this;
		        							if ($(currentTile).parent().index() > 5)
		        							{
			        							var currentTileClass = $(currentTile).attr('class').split(/\s+/)[1];
			        							var currentTileType = currentTileClass.substring(5);
			        							if(currentTileType == "empty" && currentTileType != "enemy_covered" && currentTileType != "obstacle")
			        							{
			        								$(this).removeClass("tile-empty");
				        							$(this).addClass("tile-" + selectedTileType);
				        							counters[selectedTileType]--;
				        							if (counters[selectedTileType] == 0)
				        							{
				        								selectedTile = $(selectedTile).remove();
				        							}
				        							$(".tile").off('click.bankToField');
				        							bankSelected = false;
			        							}
		        							}
		        						});
	    							}
        						}
        					});
        					
        					$(".tile").on('click.fieldToField', fieldToField);		
        					function fieldToField()
        					{
        						if (!bankSelected)
        						{
        							var currentTile = this;
        							var currentTileClass = $(currentTile).attr('class').split(/\s+/)[1];
        							var currentTileType = currentTileClass.substring(5);
        							if (selectedFieldTile == null)
        							{
	        							if (currentTileType != "empty" && currentTileType != "enemy_covered" && currentTileType != "obstacle")
	        							{
	        								$(this).css("border-color", "red");
	        								selectedFieldTile = currentTile;
	        							}
        							} else
        							{
            							if (currentTileType == "empty" && $(currentTile).parent().index() > 5)
            							{
            								var selectedFieldTileClass = $(selectedFieldTile).attr('class').split(/\s+/)[1];
                							var selectedFieldTileType = selectedFieldTileClass.substring(5);
            								$(this).removeClass("tile-empty");
                							$(this).addClass("tile-" + selectedFieldTileType);
                							
                							$(selectedFieldTile).css("border-color", "transparent");
                							$(selectedFieldTile).removeClass("tile-" + selectedFieldTileType);
                							$(selectedFieldTile).addClass("tile-empty");
                							selectedFieldTile = null;
            							}
        							}
        						}
        					}
        				}
        			});
            	} else {
            		$("#buttonStartGameContainer").html('<p id="buttonStartGameContainer"><button class="btn btn-primary btn-lg" role="button" onClick="joinNewGame()">Play</button></p>');
            	}
           	}
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
	for (var i = 0; i < field.length; i++) {
		$("#field").append('<div class="tileRow"></div>');
		for (var j = 0; j < field[i].length; j++) {
			var rowArray = $("#field .tileRow");
			$(rowArray[rowArray.length - 1]).append('<div class="tile tile-' + field[i][j] + '"></div>');
		}
	}
}

function updateField(field)
{
	var currentField = $(".tileRow");
	for (var i = 0; i < field.length; i++) {
		var currentRow = $(currentField[i]).find(".tile");
		for (var j = 0; j < field[i].length; j++) {
			var currentTileClass = $(currentRow[j]).attr('class').split(/\s+/)[1];
			var currentTileType = currentTileClass.substring(5);
			var newTileType = getUnitTypeFromChar(field[i][j]).name.toLowerCase();
			if (currentTileType != newTileType)
			{
				$(currentRow[j]).removeClass(currentTileClass);
				$(currentRow[j]).addClass("tile-" + newTileType);
			}
		}
	}
}