<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String sessionUser = (String) session.getAttribute("user");
    // logged in users skip this page and go directly to the home page
    if (sessionUser != null && !sessionUser.isEmpty())
    {
        response.sendRedirect("/Stratego/home.jsp");
    }
    else
    {
%>
<!DOCTYPE html>
<html>
<head>
	<meta charset=utf-8 />
	<title>JSweeper</title>
	<link rel="stylesheet" type="text/css" media="screen" href="/Stratego/css/bootstrap.min.css" />
	<link rel="stylesheet" type="text/css" media="screen" href="/Stratego/css/index.css" />
	<script src="/Stratego/js/jquery.min.js"></script>
	<script src="/Stratego/js/jquery-ui.min.js"></script>
	<script src="/Stratego/js/bootstrap.min.js"></script>
	<script src="/Stratego/js/index.js"></script>
	<script src="//use.typekit.net/qxl0nsj.js"></script>
	<script>try{Typekit.load();}catch(e){}</script>
	<!--[if IE]>
		<script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
	<![endif]-->
</head>
<body>
	<div class="container-centered">
		<div class="panel panel-default panel-login">
			<div class="panel-body">
				<div class="panel-heading">
					<h1 class="title">Stratego</h1>
				</div>
				<form method="post" id="login">
					<div class="alert alert-warning alert-dismissible" role="alert" id="error" style="display:none">
						<button type="button" class="close" data-hide="alert"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
						<div class="message"></div>
					</div>
				
					<div class="form-group">
						<label>Username</label> 
						<input type="text" class="form-control" id="username" />
					</div>
					
					<div class="form-group">
						<label>Password</label>
						<input type="password" class="form-control" id="password" />
					</div>
					
					<div class="form-group" id="password_confirm" style="display: none;">
						<label>Confirm Password</label>
						<input type="password" class="form-control" />
					</div>
	
					<div class="checkbox">
						<label>
							<input type="checkbox" id="create_flag"> Create account
						</label>
					</div>
					
					<input type="submit" class="btn btn-primary" value="Log In" />
				</form>
			</div>
		</div>
	</div>
</body>
</html>
<%
    }
%>