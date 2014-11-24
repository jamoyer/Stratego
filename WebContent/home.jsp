<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String sessionUser = (String) session.getAttribute("user");
    // Users must login before viewing the home page
    if (sessionUser == null || sessionUser.isEmpty())
    {
        response.sendRedirect("/Stratego/login.jsp");
    }
    else
    {
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Stratego - Home</title>
</head>
<body>
    <h1>Welcome!</h1>
    <h3>
        Logged in as
        <%
        out.print(sessionUser);
    %>
    </h3>
    <form action="/Stratego/Validator" method="post">
        <input type="hidden" name="actionType" value="logout"></input>
        <br> <br>
        <input type="submit" value="Logout"></input>
    </form>
    
    
</body>
</html>
<%
    }
%>
