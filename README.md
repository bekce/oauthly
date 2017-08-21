# oauthly
OAuth2 Authorization and Resource Server in Java with Spring Boot

Suitable for being the Oauth2 authorization server for your software platform, with SSO support.

There are a lot of authorization server examples on many platforms, such as Spring Boot, Play Framework, etc,
but none works as a full-fledged authorization server. This one does, for free. The functionality is comparable to
auth0, will be better in the future (with your PRs, of course).

## Instructions

0. Have a running mongodb instance
1. `./mvnw spring-boot:run` For a different port, append `-Dserver.port=8181` to this command.
2. Go to <http://localhost:8080>, register a new account for yourself.
First account will be given admin access
3. Create a client, set its `name` and `redirect_uri` (required) through profile screen
4. Set following endpoint addresses on your application:

- Token endpoint: http://localhost:8080/oauth/token
- Authorize endpoint: http://localhost:8080/oauth/authorize
- User info endpoint: http://localhost:8080/api/me

## Features

- Does NOT use Spring Security which means no configuration mess or cryptic class names
- Fully supported oauth2 grant types: client credentials, authorization code, resource owner password, refresh token
- Login, register and authorize client views with Bootstrap
- JWT is used to issue tokens, authorization codes and cookies
- Logged-in users are remembered with long-term safe cookies
- Multiple client id and secret pairs are supported, managed by a view
- Customizable expiry times for generated tokens (see `application.properties` file)
- Implements reCAPTCHA on register page
- OAuth2 scopes support
- MongoDB backend
- Uses bcrypt for user passwords, freemarker for templating

## Screenshots

![login](https://i.imgur.com/DpHykoJ.png)
![register](https://i.imgur.com/kksvw9p.png)
![authorize](https://i.imgur.com/5FMlHCz.png)
![client management](https://i.imgur.com/vVXfNbL.png)

## TODO (PRs are welcome!)
- Third Party Provider login: ability to Login with Facebook, etc, on the login page.
  This feature should ask the user's email if he/she had not authorized on that provider
- Change password routine
- Reset password routine via email (SMTP, Mailgun and SES)
- Email validation routine by sending a code
- OpenID default scopes with their meaning
- User management view
- Enable reCAPTCHA on login page after X number of failed requests
- Access logging
- Security checkup to see whether it has any vulnerabilities or not
- Possible production example with let's encrypt certificates, docker container and nginx
- Customized error pages
- Scope CRUD screen
