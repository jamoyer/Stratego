
var bankSelected = false;
var selectedFieldTile = null;
var highlightedLocations = null;
var playerNumber = null;
var gameStarted = false;
var revealing = false;
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
                //$("#serverResponse").text(JSON.stringify(response));
            }
        }
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
    $("#buttonStartGameContainer").html('<button class="btn btn-lg btn-warning"><span class="glyphicon glyphicon-refresh glyphicon-refresh-animate"></span> Searching for Opponent...</button>');
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

        var position = getUnitTypeFromChar(field[row][col]);
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
        allowedMove.col = col;
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
    var currentField = $(".tileRow");
    $(currentField).each(function(rowIndex, row)
    {
        if (rowIndex >= 6)
        {
            var startingRow = [];
            $(row).children(".tile").each(function(colIndex, element)
            {
                var currentTileClass = $(element).attr('class').split(/\s+/)[1];
                var currentTileType = currentTileClass.substring(5).toUpperCase();
                var symbol = getCharFromUnitType(currentTileType);
                startingRow.push(symbol);
            });
            startingField.push(startingRow);
        }
    });

    if (isTopPlayer)
    {
        startingField = flipField(startingField);
    }

    //var theme = document.getElementById('theme').value;
    var theme = "batman";

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
            //$("#serverResponse").text(JSON.stringify(data));
            
            if (JSONObjectToSend["actionType"] == "moveUnit" || JSONObjectToSend["actionType"] == "setPositions" || gameStarted) {
            	data = data[data.length - 1];
            	
            	if (data.gameWon)
            	{
            		alert("you win!");
            		$.ajax({
            			url: '/Stratego/html/newGame.html',
            			type: 'GET',
            			success: function(data) {
            				$("#container").html(data);
            			}
            		});
            	}
            	
            	if (data.gameWon)
            	{
            		alert("you lose!");
            		$.ajax({
            			url: '/Stratego/html/newGame.html',
            			type: 'GET',
            			success: function(data) {
            				$("#container").html(data);
            			}
            		});
            	}
            	
            	if (revealing)
            	{	
            		setTimeout(function() {
            			setPositionsResponse(data);
            		}, 3000);
            	}
            	else
            	{	
            		setPositionsResponse(data);
            	}
            	revealing = data.isReveal;
            }
        }
        if (xmlReq.readyState === 4)
        {
            // convert the responseText into JSON
            var data = parseGameControlResponse(xmlReq.responseText);

            // display response for debugging purposes
            //$("#serverResponse").text(JSON.stringify(data));

            if (JSONObjectToSend["actionType"] == "newGame") {
            	data = data[0];
            	if (data.isSuccessful) {
            		playerNumber = data.playerNum;
            		var isTopPlayer = (playerNumber == 2);
            			
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
				        							totalCounter--;
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
        					
        					$(".tile").on('click.fieldToField', function () {
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
        					});
        					
        					$("#buttonSetStartPositions").on('click', function(e) {
        						totalCounter = 0;
        						if (totalCounter == 0)
        						{
        							$(".tile").off('click');
        							selectedFieldTile = null;
        							$(".bankTile").off('click');
        							setStartPositions(isTopPlayer);
        							$("#gameContainer").prepend('<button id="waitingBanner" disabled="disabled" class="btn btn-lg btn-warning"><span class="glyphicon glyphicon-refresh glyphicon-refresh-animate"></span> Waiting for Opponent...</button>');	
        							$(".panel-bank").remove();
        						} else
        						{
        							alert("You must place all units");
        						}
        					})
        				}
        			});
            	} else {
            		$("#buttonStartGameContainer").html('<p id="buttonStartGameContainer"><button class="btn btn-primary btn-lg" role="button" onClick="joinNewGame()">Play</button></p>');
            	}
           	}
            /*
            if (JSONObjectToSend["actionType"] == "setPositions") {
            	setPositionsResponse(data[0]);
            }
            */
        }
    }
    xmlReq.send("data=" + JSON.stringify(JSONObjectToSend));
}

function setPositionsResponse(data) {
	if(data.isSuccessful)
	{
		gameStarted = true;
		if (data.playerNum == 2)
		{
			data.field = flipField(data.field);
		}
		
		updateField(data.field);
		if(data.playerNum == data.currentTurn && !data.isReveal)
		{
			$("#waitingBanner").remove();
			$(".tile").on('click.moveUnit', function() {
				var clickedTile = this;
				var clickedTileClass = $(clickedTile).attr('class').split(/\s+/)[1];
				var clickedTileType = clickedTileClass.substring(5);
				
				var coordinate = {};
				coordinate["col"] = $(clickedTile).index();
				coordinate["row"] = $(clickedTile).parent().index();
				
				if (selectedFieldTile == null)
				{
					if (clickedTileType != "empty" && clickedTileType != "enemy_covered" && clickedTileType != "obstacle")
					{
						highlightedLocations = getListOfAllowedMoves(data.field, coordinate);
						for(var i = 0; i < highlightedLocations.length; i++)
						{
							var destination = highlightedLocations[i];
							var destRow = $(".tileRow")[destination["row"]];
							var destTile = $(destRow).children(".tile")[destination["col"]];
							if (destination.isAttack)
								$(destTile).css("border-color","red");
							else
								$(destTile).css("border-color","green");
						}
						selectedFieldTile = clickedTile;
					} else
						alert("You can only move your own units");
				} else {
					if (clickedTileType == "empty" || clickedTileType == "enemy_covered")
					{
						$(".tile").off('click');
						
						for(var i = 0; i < highlightedLocations.length; i++)
						{
							var destination = highlightedLocations[i];
							var destRow = $(".tileRow")[destination["row"]];
							var destTile = $(destRow).children(".tile")[destination["col"]];
							$(destTile).css("border-color","black");
						}
						highlightedLocations = null;
						
						var precedingCoordinate = {};
						precedingCoordinate["col"] = $(selectedFieldTile).index();
						precedingCoordinate["row"] = $(selectedFieldTile).parent().index();
						selectedFieldTile = null;
						if (data.playerNum == 2)
						{
							//data.field = flipField(data.field);
							var flipPredCoord = flipCoordinate(precedingCoordinate, data.field.length, data.field[0].length);
							var flipDestCoord = flipCoordinate(coordinate, data.field.length, data.field[0].length);
							sendMoveRequest(flipPredCoord, flipDestCoord);
						} else {
							sendMoveRequest(precedingCoordinate, coordinate);
						}
					} else
						alert("You can only move to an empty tile or attack");
				}
			});
		} else
		{
			$("#gameContainer").prepend('<button id="waitingBanner" disabled="disabled" class="btn btn-lg btn-warning"><span class="glyphicon glyphicon-refresh glyphicon-refresh-animate"></span> Waiting for Opponent...</button>');
		}
	}
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

/*
 * Will flip a coordinate which belongs to a 2-dimensional array so that it is the coordinate if
 * this array were upside-down. Takes a json coordinate like {row:x,col:y} and the number of rows
 * and columns in the array. Returns null if the coordinate data is null or out of bounds.
 */
function flipCoordinate(coordinate, numRows, numCols)
{
    if (coordinate == null || coordinate.row == null || coordinate.row >= numRows
            || coordinate.col == null || coordinate.col >= numCols)
    {
        return null;
    }

    // arrays are 0 indexed
    numRows -= 1;
    numCols -= 1;

    function flip(number, max)
    {
        return Math.abs(max - number);
    }

    var coord =
    {
        row : flip(coordinate.row, numRows),
        col : flip(coordinate.col, numCols)
    };

    return coord;
}

/*
 * Takes an arbitrary sized double-array and flips it upside down. Assumes that each second array is
 * the same size.
 */
function flipField(field)
{
    if (field == null || field.length == 0)
    {
        return null;
    }

    var maxRow = field.length / 2;
    if (field.length % 2 == 1)
    {
        maxRow++;
    }

    for (var row = 0; row < maxRow; row++)
    {
        for (var col = 0; col < field[row].length; col++)
        {
            var coord1 =
            {
                row : row,
                col : col
            };
            var coord2 = flipCoordinate(coord1, field.length, field[row].length);
            var temp = field[row][col];
            field[row][col] = field[coord2.row][coord2.col];
            field[coord2.row][coord2.col] = temp;
        }
    }
    return field;
}