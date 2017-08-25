<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Reset your password</title>

    <link href="/resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="/resources/css/common.css" rel="stylesheet">

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
    <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->

    <script src='https://www.google.com/recaptcha/api.js'></script>
</head>

<body>

<div class="container">

    <form method="POST" action="/reset-password" class="form-signin">
        <#if info??>
            <div class="alert alert-info" role="alert">${info}</div>
        </#if>
        <#if success??>
            <div class="alert alert-success" role="alert">${success}</div>
        </#if>
        <#if error??>
            <div class="alert alert-danger" role="alert">${error}</div>
        </#if>

        <#if step == 1>
            <h2 class="form-heading">Reset Password</h2>
            <div class="form-group">
                <span>If you forgot your password, you can reset it via sending yourself an email link</span>
                <input name="username" type="text" class="form-control" placeholder="Username or Email" autofocus="true"/>
            </div>
        </#if>
        <#if step == 3>
            <h2 class="form-heading">Reset Password</h2>
            <div class="form-group">
                <span>Please set a new password for user <strong>${username}</strong> to complete the process</span>
                <input name="newPassword" type="password" class="form-control" placeholder="Password" autofocus/>
            </div>
            <div class="form-group">
                <input name="newPassword2" type="password" class="form-control" placeholder="(again)" autofocus/>
            </div>
            <input type="hidden" name="reset_code" value="${reset_code}"/>
        </#if>
        <#if step == 3 || step == 1>
            <input type="hidden" name="step" value="${step}"/>
            <div class="g-recaptcha" data-sitekey="6LfpYy0UAAAAALI0zTdsq9AzUE1Mi2KeCy-bpoEx"></div>
            <button class="btn btn-lg btn-primary btn-block" type="submit">Submit</button>
        </#if>
        <h4 class="text-center"><a href="/login">Back to login</a></h4>

    </form>

</div>
<!-- /container -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
<script src="/resources/js/bootstrap.min.js"></script>
</body>
</html>
