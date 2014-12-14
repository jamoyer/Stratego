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
		File file = new File("/Users/michaeldmead/workspace/Stratego/WebContent/img/");
		if (file != null)
		{
		    String[] themes = file.list();
		    if (themes != null)
		    {
		        for (String theme : themes)
		        {
		        	out.write("."+theme+" .tile-marshall\n");
		        	out.write("{\n");
		        	out.write("background-image: url(\"img/"+theme+"/1.jpg\")\n");
		        	out.write("}\n");
		        	
		        	out.write("."+theme+" .tile-general\n");
		        	out.write("{\n");
		        	out.write("background-image: url(\"img/"+theme+"/2.jpg\");\n");
		        	out.write("}\n");
		        	
		        	out.write("."+theme+" .tile-colonel\n");
		        	out.write("{\n");
		        	out.write("background-image: url(\"img/"+theme+"/3.jpg\");\n");
		        	out.write("}\n");
		        	
		        	out.write("."+theme+" .tile-major\n");
		        	out.write("{\n");
		        	out.write("background-image: url(\"img/"+theme+"/4.jpg\");\n");
		        	out.write("}\n");
		        	
		        	out.write("."+theme+" .tile-captain\n");
		        	out.write("{\n");
		        	out.write("background-image: url(\"img/"+theme+"/5.jpg\");\n");
		        	out.write("}\n");
		        	
		        	out.write("."+theme+" .tile-lieutenant\n");
		        	out.write("{\n");
		        	out.write("background-image: url(\"img/"+theme+"/6.jpg\");\n");
		        	out.write("}\n");
		        	
		        	out.write("."+theme+" .tile-sergeant\n");
		        	out.write("{\n");
		        	out.write("background-image: url(\"img/"+theme+"/7.jpg\");\n");
		        	out.write("}\n");
		        	
		        	out.write("."+theme+" .tile-miner\n");
		        	out.write("{\n");
		        	out.write("background-image: url(\"img/"+theme+"/8.jpg\");\n");
		        	out.write("}\n");
		        	
		        	out.write("."+theme+" .tile-scout\n");
		        	out.write("{\n");
		        	out.write("background-image: url(\"img/"+theme+"/9.jpg\");\n");
		        	out.write("}\n");
		        	
		        	out.write("."+theme+" .tile-spy\n");
		        	out.write("{\n");
		        	out.write("background-image: url(\"img/"+theme+"/spy.jpg\");\n");
		        	out.write("}\n");
		        	
		        	out.write("."+theme+" .tile-bomb\n");
		        	out.write("{\n");
		        	out.write("background-image: url(\"img/"+theme+"/bomb.jpg\");\n");
		        	out.write("}\n");
		        	
		        	out.write("."+theme+" .tile-flag\n");
		        	out.write("{\n");
		        	out.write("background-image: url(\"img/"+theme+"/flag.jpg\");\n");
		        	out.write("}\n");
		        	
		        	out.write("."+theme+" .tile-enemy_covered\n");
		        	out.write("{\n");
	
		        	out.write("background-image: url(\"img/"+theme+"/cover.jpg\");\n");
		        	out.write("}\n");
		            
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
<p id="serverResponse"></p>
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
					<li class="dropdown">
						<a href="#" class="dropdown-toggle" data-toggle="dropdown">Difficulty <span class="caret"></span></a>
						<ul class="dropdown-menu" role="menu">
							<li><a href="#" id="beginner" >Beginner</a></li>
							<li><a href="#" id="intermediate" >Intermediate</a></li>
							<li><a href="#" id="expert" >Expert</a></li>
						</ul>
					</li>
					<li class="dropdown">
						<a href="#" class="dropdown-toggle" data-toggle="dropdown">Theme <span class="caret"></span></a>
						<ul class="dropdown-menu" role="menu">
							<li><a href="#" id="normalMode">Normal</a></li>
							<li><a href="#" id="batmanMode">Batman</a></li>
							<li><a href="#" id="sharkMode">Shark</a></li>
						</ul>
					</li>
					<li><a id="AboutModalButton" data-toggle="modal" data-target="#AboutModal">About</a></li>
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

<!-- Modal -->
<div class="modal fade" id="AboutModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
				<h4 class="modal-title title" id="myModalLabel">Stratego</h4>
			</div>
			<div class="modal-body">
				JSweeper is a Linux-Apache-MySQL-PHP (LAMP) web application rendition of the classic Minesweeper game created by Jacob Moyer, Matt Clucas,  and Mike Mead. In the next version... multiplayer, and more!
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
}
</script>
</body>
</html>
<%
    }
%>
