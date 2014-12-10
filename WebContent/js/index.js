$(document).ready(function() {
	$('#login').on('submit', function() {
		var username = $('#username').val();
		var password = $('#password').val();
		var password_confirm = $('#password_confirm input').val();
		var create_flag = $('#create_flag').is(':checked') ? 1 : 0;
		
		if ($.trim(username).length > 0 && $.trim(password).length > 0)
		{
			if (create_flag == 1 && ($.trim(password) != $.trim(password_confirm)))
			{
				error("<strong>Warning!</strong> Passwords do not match.");
				return false;
			}
			
			var requestType = create_flag ? 'signup' : 'login';
			
			var postData = 'username=' + username + '&password=' + password + '&actionType=' + requestType;
		
			$.ajax({
				type: "POST",
				url: "/Stratego/Validator",
				data: postData,
				beforeSend: function(){ $("#login").val("Connecting..."); },
				success: function(resultString) {
					try
					{
						var response = jQuery.parseJSON(resultString);
						if (response["id"] != null && response["id"] > 0)
						{
							window.location.replace("home.php");
						} else if (response["message"] != null && response["message"].length > 0)
						{
							error(response["message"]);
						} else
							throw 500;
						
					} catch(e)
					{
						error("<strong>Warning!</strong> Username / Password combination incorrect.");
					}
				}
			});
		}
		return false;
	});
	
	$('#create_flag').change(function(){
	    if (this.checked)
	    {
		    $('#password_confirm').show();
	    } else
	    {
		    $('#password_confirm').hide();
		    $('#password_confirm input').val("");
	    }
	});
	
	$("[data-hide]").on("click", function(){
        $(this).closest("." + $(this).attr("data-hide")).hide();
    });
});

function error(message)
{
	$("#error .message").html(message);
	$("#error").show();
	$(".panel-login").effect("shake");
	$("#login").val("Log In");
}