<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page
    import="org.apache.commons.lang3.StringEscapeUtils,
            stratego.model.TileSymbol, 
            stratego.model.UnitType,
           java.io.File"
%>
<%
    String sessionUser = (String) session.getAttribute("user");

	// Users must login before viewing the home page
	if (sessionUser == null || sessionUser.isEmpty()) {
		response.sendRedirect("/Stratego/login.jsp");
	} else {
%>
<!DOCTYPE html>
<html>
<head>
	<meta charset=utf-8 />
	<title>JSweeper</title>
	<link rel="stylesheet" type="text/css" media="screen" href="/Stratego/css/bootstrap.min.css" />
	<link rel="stylesheet" type="text/css" media="screen" href="/Stratego/css/main.css" />
	<link rel="stylesheet" type="text/css" media="screen" href="/Stratego/css/home.css" />
	<style>
	<%
		File file = new File("/usr/share/tomcat/webapps/Stratego/img/");
		if (file != null)
		{
		    String[] themes = file.list();
		    if (themes != null)
		    {
		        for (String theme : themes)
		        {
			        for (String enemyTheme : themes)
			        {
	                    for (UnitType unit : UnitType.values())
	                    {
	                        if(unit!= UnitType.SPY)
	                        {
	                        out.write("."+theme+enemyTheme+" .tile-"+unit.toString().toLowerCase()+"\n");
	                        out.write("{\n");
	                        out.write("background-image: url(\"img/"+theme+"/"+unit.getRank()+".jpg\")\n");
	                        out.write("}\n");
	                        
	                        out.write("."+theme+enemyTheme+" .tile-enemy_"+unit.toString().toLowerCase()+"\n");
	                        out.write("{\n");
	                        out.write("background-image: url(\"img/"+enemyTheme+"/"+unit.getRank()+".jpg\")\n");
	                        out.write("}\n");
	                        }
	                    }
			        	
	                    out.write("."+theme+enemyTheme+" .tile-spy\n");
	                    out.write("{\n");
	                    out.write("background-image: url(\"img/"+theme+"/spy.jpg\");\n");
	                    out.write("}\n");
	                    
	                    out.write("."+theme+enemyTheme+" .tile-enemy_spy\n");
	                    out.write("{\n");
	                    out.write("background-image: url(\"img/"+enemyTheme+"/spy.jpg\");\n");
	                    out.write("}\n");
	                    
			        	out.write("."+theme+enemyTheme+" .tile-bomb\n");
			        	out.write("{\n");
			        	out.write("background-image: url(\"img/"+theme+"/bomb.jpg\");\n");
			        	out.write("}\n");
			        	
			        	out.write("."+theme+enemyTheme+" .tile-flag\n");
			        	out.write("{\n");
			        	out.write("background-image: url(\"img/"+theme+"/flag.jpg\");\n");
			        	out.write("}\n");
	                    
	                    out.write("."+theme+enemyTheme+" .tile-enemy_bomb\n");
	                    out.write("{\n");
	                    out.write("background-image: url(\"img/"+enemyTheme+"/bomb.jpg\");\n");
	                    out.write("}\n");
	                    
	                    out.write("."+theme+enemyTheme+" .tile-enemy_flag\n");
	                    out.write("{\n");
	                    out.write("background-image: url(\"img/"+enemyTheme+"/flag.jpg\");\n");
	                    out.write("}\n");
			        	
			        	out.write("."+theme+enemyTheme+" .tile-enemy_covered\n");
			        	out.write("{\n");
			        	out.write("background-image: url(\"img/"+enemyTheme+"/cover.jpg\");\n");
			        	out.write("}\n");
			            
			        }
		        }
		    }
		}
	
	%>
	</style>
	<script src="/Stratego/js/jquery.min.js"></script>
	<script src="/Stratego/js/jquery-ui.min.js"></script>
	<script src="/Stratego/js/bootstrap.min.js"></script>
	<script src="//use.edgefonts.net/patua-one.js"></script>
	<script>
		function getUnitTypeFromChar(symbol)
		{
		    var tileType = {};
		    switch (symbol)
		    {
		        <%for (TileSymbol tile : TileSymbol.values())
		        {
		            UnitType unit = UnitType.getUnitByTile(tile);
		            int numUnits = -1;
		            if(unit != null)
		            {
		                numUnits = unit.getNumUnits();
		            }%>
		        case "<%out.print(tile.getSymbol());%>":
		            tileType.name = "<%out.print(tile.toString());%>";
		            tileType.numUnits = "<%out.print(numUnits);%>";
		            break;
		        <%}%>
		        default:
		            tileType = null;
		        break;
		    }
		    return tileType;
		}
		
		function getCharFromUnitType(type)
	    {
	        switch (type)
	        {
	            <%for (TileSymbol tile : TileSymbol.values())
	            {%>
	            case "<%out.print(tile.name());%>":
	                return "<%out.print(tile.getSymbol());%>";
	            <%}%>
	            default:
	                return null;
	        }
	    }
	</script>
	<script src="/Stratego/js/home.js"></script>
</head>
<body>
<!-- <p id="serverResponse"></p> -->
<div class="container">
	<!-- Static navbar -->
	<div class="navbar navbar-default" role="navigation">
		<div class="container-fluid">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target=".navbar-collapse">
					<span class="sr-only">Toggle navigation</span>
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
				</button>
				<a class="navbar-brand title" href="home.php">Stratego</a>
			</div>
			<div class="navbar-collapse collapse">
				<ul class="nav navbar-nav">
					<!--
					<li class="dropdown">
						<a href="#" class="dropdown-toggle" data-toggle="dropdown">Difficulty <span class="caret"></span></a>
						<ul class="dropdown-menu" role="menu">
							<li><a href="#" id="beginner" >Beginner</a></li>
							<li><a href="#" id="intermediate" >Intermediate</a></li>
							<li><a href="#" id="expert" >Expert</a></li>
						</ul>
					</li>
					-->
					<li class="dropdown">
						<a href="#" class="dropdown-toggle" data-toggle="dropdown">Theme <span class="caret"></span></a>
						<ul class="dropdown-menu" role="menu" id="selectTheme">
							<%
								if (file != null)
								{
								    String[] themes = file.list();
								    if (themes != null)
								    {
								        for (String theme : themes)
								        {
								        	if (theme.indexOf('.') == -1)
								        		out.println("<li><a href=\"#\">" + theme + "</a></li>");
								        }
								    }
								}
							%>
						</ul>
					</li>
					 

<li class="dropdown">
						<a href="#" class="dropdown-toggle" data-toggle="dropdown">Info <span class="caret"></span></a>
						<ul class="dropdown-menu" role="menu">
                            <li><a id="HighScoresModal" data-toggle="modal" data-target="#HighScoreModal">High Scores</a></li>
					        <li><a id="UserModal" data-toggle="modal" data-target="#UsersModal">Online Users</a></li>
					        <li><a id="AboutModalButton" data-toggle="modal" data-target="#AboutModal">About</a></li>
						</ul>
					</li>


				</ul>
				<div class="navbar-right">
					<p class="navbar-text">Signed in as <% out.print(StringEscapeUtils.escapeHtml4(sessionUser)); %></p>
					<button type="button" class="btn btn-default navbar-btn" id="buttonLogout">Logout</button>
				</div>
			</div><!--/.nav-collapse -->
		</div><!--/.container-fluid -->
	</div>

	<div class="row">
		<div class="col-md-12" id="container">
			
		</div>
	</div>

</div> <!-- /container -->

<!-- About Modal -->
<div class="modal fade" id="AboutModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
				<h4 class="modal-title title" id="myModalLabel">Stratego</h4>
			</div>
			<div class="modal-body">
				Stratego is a board game with 2 players where 2 opponents attempt to capture each other's flag. Each player sets up their army's starting locations and then battle each other. We are bringing this game to a java servlet ran website. This is the final project for Computer Science 319 Fall Semester 2014 by Jacob Moyer, Matt Clucas, and Mike Mead.
			</div>
		</div>
	</div>
</div>

<!-- High Scores -->
<div class="modal fade" id="HighScoreModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
				<h4 class="modal-title title" id="myModalLabel">Stratego</h4>
			</div>
			<div id = "highScores"class="modal-body">
			<table id="highScoresTable" class="table table-striped"></table>
			</div>
		</div>
	</div>
</div>

<!-- Online Users -->
<div class="modal fade" id="UsersModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
				<h4 class="modal-title title" id="myModalLabel">Stratego</h4>
			</div>
			<div id = "usersOnline"class="modal-body">
			<table id="onlineUsersTable" class="table table-striped"></table>
			</div>
		</div>
	</div>
</div>
<script>
$(document).ready()
{
	pingForever();
	var currentGame = getCurrentGame();
	if (currentGame) {
		
	} else
	{
		$.ajax({
			url: '/Stratego/html/newGame.html',
			type: 'GET',
			success: function(data) {
				$("#container").html(data);
			}
		});
	}
		
	$('#buttonLogout').on('click', function(e) {
		var request = {};
		request["actionType"] = "logout";
		$.ajax({
			url: '/Stratego/Validator',
			type: 'POST',
			data: request,
			success: function(data) {
				window.location.replace("/Stratego/")
			},	
			error: function() {
				alert("An error has occurred");
			}
		});
	});
	
	$("#selectTheme li").on('click', function(e) {
		if (opponentThemeSet == false)
		{
			var clickedTheme = $(this).children("a")[0].text;
			myTheme = clickedTheme;
			$("#gameContainer").removeClass();
			$("#gameContainer").addClass(clickedTheme+"classic");	
		}
	});

$('#HighScoresModal').on('click', function(e) {
		var request = {};
		request["actionType"] = "getHighScores";
		$.ajax({
			url: '/Stratego/Info',
			type: 'POST',
			data: request,
			success: function(data) {
				data = JSON.parse(data);
				$("#highScoresTable").empty();
				$("#highScoresTable").append("<tr><th>User</th><th>Score</th></tr>");
				for (var i = 0; i < data.highscores.length ; i++)
				{
					var user = data.highscores[i][0];
					var score = data.highscores[i][1];
					$("#highScoresTable").append("<tr><td>"+user+"</td><td>"+score+"</td></tr>");
				}
			},	
			error: function() {
				alert("An error has occurred");
			}
		});
	});
	
	$('#UserModal').on('click', function(e) {
		var request = {};
		request["actionType"] = "getCurrentUsers";
		$.ajax({
			url: '/Stratego/Info',
			type: 'POST',
			data: request,
			success: function(data) {
				data = JSON.parse(data);
				$("#onlineUsersTable").empty();
				$("#onlineUsersTable").append("<tr><th>User</th></tr>");
				for (var i = 0; i < data.users.length ; i++)
				{
					var user = data.users[i];
					$("#onlineUsersTable").append("<tr><td>"+user+"</td></tr>");
				}
			},	
			error: function() {
				alert("An error has occurred");
			}
		});
	});
}
</script>
</body>
</html>
<%
    }
%>
