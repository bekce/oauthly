<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Profile</title>

    <link href="/resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="/resources/css/common.css" rel="stylesheet">

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
    <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
</head>

<body>

<div class="container">

    <h2 class="form-heading">Welcome ${username}!</h2>

    <#if canCreateClients>
        <table class="table table-striped">
            <thead><tr><td>id</td><td>secret</td><td>name</td><td>redirect_uri</td></tr></thead>
            <tbody>
            <#list clients as client>
                <tr>
                    <td>${client.id}</td>
                    <td>${client.secret}</td>
                    <form action="/client" method="post">
                        <td><input type="text" name="name" value="${client.name}"></td>
                        <td><input type="text" name="redirectUri" value="${client.redirectUri?default('')}"></td>
                        <td><input type="hidden" value="${client.id}" name="id"/><button class="btn" type="submit">Save</button></td>
                    </form>
                </tr>
            </#list>
            </tbody>
        </table>
        <br>Create new client <form action="/client" method="post"><input type="text" name="name" /><button class="btn" type="submit">Save</button></form>
    </#if>
    <h4 class="text-center"><a href="/logout">Logout</a></h4>

</div>
<!-- /container -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
<script src="/resources/js/bootstrap.min.js"></script>
</body>
</html>
