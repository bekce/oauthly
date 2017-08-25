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

    <#if success??>
        <div class="alert alert-success" role="alert">${success}</div>
    </#if>
    <div class="alert alert-danger" role="alert">${error?default('')}</div>
    <#if error??>
    </#if>

    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Change Password</h3>
        </div>
        <div class="panel-body">
            <form class="form-horizontal" action="/profile/password" method="post">
                <div class="form-group">
                    <label for="oldPassword" class="col-sm-2 control-label">Old Password</label>
                    <div class="col-sm-10">
                        <input type="password" name="oldPassword" class="form-control" id="oldPassword" placeholder="Old Password">
                    </div>
                </div>
                <div class="form-group">
                    <label for="newPassword" class="col-sm-2 control-label">New Password</label>
                    <div class="col-sm-10">
                        <input type="password" name="newPassword" class="form-control" id="newPassword" placeholder="New Password">
                    </div>
                </div>
                <div class="form-group">
                    <label for="newPassword2" class="col-sm-2 control-label">New Password</label>
                    <div class="col-sm-10">
                        <input type="password" name="newPassword2" class="form-control" id="newPassword2" placeholder="(again)">
                    </div>
                </div>
                <button class="btn" type="submit">Submit</button>
            </form>
        </div>
    </div>

    <#if canCreateClients>
        <h3>Clients</h3>
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
        <form class="form-inline" action="/client" method="post">
            <div class="form-group">
                <input type="text" name="name" class="form-control" placeholder="name"/>
            </div>
            <div class="form-group">
                <input type="text" name="redirectUri" class="form-control" placeholder="redirect_uri"/>
            </div>
            <button class="btn" type="submit">Create new client</button>
        </form>
    </#if>

    <#if isAdmin>
        <h3>Discourse Settings</h3>
        <form class="form-inline" action="/discourse/settings" method="post">
            <div class="checkbox">
                <label>
                    <input type="checkbox" name="enabled" <#if discourse.enabled>checked</#if>> Enabled
                </label>
            </div>
            <div class="form-group">
                <input type="text" name="redirectUri" value="${discourse.redirectUri?default('')}" class="form-control" placeholder="redirect_uri">
            </div>
            <button type="submit" class="btn">Save</button>
            <div class="form-group">
                <p class="form-control-static">Secret: ${discourse.secret?default('(enable first)')}</p>
            </div>
        </form>
    </#if>

    <h4 class="text-center"><a href="/logout">Logout</a></h4>

</div>
<!-- /container -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
<script src="/resources/js/bootstrap.min.js"></script>
</body>
</html>
