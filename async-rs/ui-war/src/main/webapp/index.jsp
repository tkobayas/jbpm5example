<html>
<head>
<title>Async RS example</title>
</head>
<body>
<p>Async RS example</p>
<p><%= request.getAttribute("message") == null ? "" : request.getAttribute("message") %></p>
<form method="post" action="process">
<input type="submit" value="Start a process">
</form>
</body>
</html>