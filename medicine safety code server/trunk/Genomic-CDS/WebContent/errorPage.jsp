<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1" isErrorPage="true"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href="css/medicine-safety-code.css" />
<link rel="stylesheet"
	href="http://code.jquery.com/mobile/1.1.0/jquery.mobile.structure-1.1.0.min.css" />
<script src="http://code.jquery.com/jquery-1.7.1.min.js"></script>
<script src="http://code.jquery.com/mobile/latest/jquery.mobile.min.js"></script>
<title>Error Page</title>
</head>
<body>
	<div data-role="page" id="mainPage" data-theme="c">
		<div data-role="header" data-theme="c"
			style="text-align: center; padding: 3px">
			<img src="img/safety-code-logo.png" width="209"
				height="30" alt="safety-code.org" />
		</div>
		<div data-role="content">
			<h2>Message:</h2>
			<b><%=exception.getMessage()%></b>
		</div>
		<div data-theme="c" data-role="footer"
			style="text-align: center; padding: 5px;">
			<div>
				This service is provided for research purposes only and comes
				without any warranty. (C)&nbsp;2012&nbsp;<a
					href="http://safety-code.org/" data-ajax="false">safety-code.org</a>
			</div>
		</div>
	</div>
</body>
</html>