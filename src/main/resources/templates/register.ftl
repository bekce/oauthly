<#import "/spring.ftl" as spring/>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Create an account</title>

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

    <form action="" method="POST" class="form-signin">
        <@spring.bind "dto.username"/>
        <div class="form-group">
            <input type="text" name="${spring.status.expression}" value="${spring.status.value?default("")}" class="form-control" placeholder="Username" autofocus="true"/>
            <@spring.showErrors "<br>"/>
        </div>
        <@spring.bind "dto.email"/>
        <div class="form-group">
            <input type="text" name="${spring.status.expression}" value="${spring.status.value?default("")}" class="form-control" placeholder="Email"/>
            <@spring.showErrors "<br>"/>
        </div>
        <@spring.bind "dto.password"/>
        <div class="form-group">
            <input type="password" name="${spring.status.expression}" value="${spring.status.value?default("")}" class="form-control" placeholder="Password"/>
            <@spring.showErrors "<br>"/>
        </div>
        <@spring.bind "dto.passwordConfirm"/>
        <div class="form-group">
            <input type="password" name="${spring.status.expression}" value="${spring.status.value?default("")}" class="form-control" placeholder="Password"/>
            <@spring.showErrors "<br>"/>
        </div>

        <button class="btn btn-lg btn-primary btn-block" type="submit">Submit</button>

        <h4 class="text-center"><a href="/login">Back to login</a></h4>
    </form>

</div>
<!-- /container -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
<script src="/resources/js/bootstrap.min.js"></script>
</body>
</html>
