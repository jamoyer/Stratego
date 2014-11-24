<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Stratego</title>
</head>
<body>
    <h1>Welcome!</h1>
    <h2>Please login or make an account.</h2>
    <form action="/Stratego/Validator" method="post">
        <h3>Login</h3>
        <input type="hidden" name="actionType" value="login"></input>
        <h4>Username</h4>
        <input type="text" name="username"></input>
        <h4>Password</h4>
        <input type="password" name="password"></input>
        <br>
        <br>
        <input type="submit" value="Login"></input>
    </form>
</body>
</html>