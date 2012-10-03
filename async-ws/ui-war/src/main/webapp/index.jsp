<html>
<head>
<title>Async WS example</title>
</head>
<body>
<p>Async WS example</p>
<p><%= request.getAttribute("message") == null ? "" : request.getAttribute("message") %></p>
<form method="post" action="process">
<input type="submit" value="Start a process">
</form>
</body>
</html>