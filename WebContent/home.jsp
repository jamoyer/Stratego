<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.apache.commons.lang3.StringEscapeUtils;"%>
<%
    String sessionUser = (String) session.getAttribute("user");
			// Users must login before viewing the home page
			if (sessionUser == null || sessionUser.isEmpty()) {
				response.sendRedirect("/Stratego/login.jsp");
			} else {
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script src="/Stratego/js/jquery-2.1.1.min.js"></script>
<script src="/Stratego/js/home.js"></script>
<title>Stratego - Home</title>
<script>
    // this will ping the game server every 5 seconds and refresh the response time
    function pingForever()
    {
        setTimeout(function()
        {
            pingGameControl();
            pingForever();
        }, 5000);
    }
    pingForever();

    //returns the current game, if the user is in a game already
    getCurrentGame();
</script>
</head>
<body>
    <h1>Welcome!</h1>
    <h3>
        Logged in as
        <%
        out.print(StringEscapeUtils.escapeHtml4(sessionUser));
    %>
    </h3>
    <div>
        <form action="/Stratego/Validator" method="post">
            <input type="hidden" name="actionType" value="logout"></input>
            <br> <br>
            <input type="submit" value="Logout"></input>
        </form>
    </div>
    <div>
        <button onclick="joinNewGame()">New Game</button>
        <button onclick="setStartPositions()">Set Start Positions</button>
        <input type="text" id="theme"></input>
        <button onclick="quitGame()">Quit Game</button>
        <h5>Source</h5>
        <p>Source Row</p>
        <input type="text" id="sourceRow"></input>
        <p>Source Column</p>
        <input type="text" id="sourceCol"></input>
        <h5>Destination</h5>
        <p>Destination Row</p>
        <input type="text" id="destinationRow"></input>
        <p>Destination Column</p>
        <input type="text" id="destinationCol"></input>
        <button onclick="moveUnit()">Move Unit</button>
    </div>
    <div>
        <h4>Server Response</h4>
        <p id="serverResponse"></p>
    </div>
</body>
</html>
<%
    }
%>
