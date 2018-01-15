# oauthly

[![Build Status](https://travis-ci.org/bekce/oauthly.svg?branch=master)](https://travis-ci.org/bekce/oauthly)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/18e70942adcf440e8c85d3e186c0e916)](https://www.codacy.com/app/seb_4/oauthly)

OAuth 2.0 Compliant Authorization and Resource Server in Java with Play Framework. Suitable for being the Authorization Server for your software platform, with SSO and social provider login support.

**Important Note** This project was started as a Spring Boot project (on branch `spring-boot`) then I have recently fully converted it to Play Framework. The reason for this change is the superior features of Play Framework, and a little bit of curiousity on my side.

There are a lot of authorization server examples on many platforms, such as Spring Boot, Play Framework, etc,
but none works as a full-fledged authorization server. This one does, for free. The functionality is comparable to
auth0, will be better in the future (with your PRs, of course).

## Instructions for local mode

0. Have a running mongodb instance and `sbt` installed.
1. `sbt run`
2. Go to <http://localhost:9000>, register a new account for yourself.
All accounts are required to confirm email addresses. To receive emails, you need to get your own mailbox api key. For demo purposes, the confirmation link is logged on console after you register. Copy and paste the link on your browser to finalize account creation. 
First account will be given admin access.
3. You can also enable login with Facebook, Google or any other OAuth 2.0 Authorization Server.
    1. For Facebook, go to <https://developers.facebook.com/apps/> and create yourself an app with redirect uri `http://localhost:9000/oauth/client/facebook/callback`, then put its client id and secret to `application.conf`. You may need some additional settings on your Facebook app. Consult their documentation. 
    2. For Google, go to <https://console.developers.google.com/apis/credentials> and create an OAuth Client ID with redirect url `http://localhost:9000/oauth/client/google/callback`, then put its client id and secret to `application.conf`. You will also need to enable Google People API. Consult their documentation. 
    3. For any other OAuth 2.0 Authorization Server, 
        1. Put relevant config block in `application.conf`.
        2. Set `tokenRetriever` and `currentUserIdentifier` in `AuthorizationServerManager` class. Only `id` is necessary for identifying users in remote system. If you map email addresses in remote system to OAuthly, the confirmation step will be bypassed resulting in a smoother sign-up process. 
        3. We use [bootstrap-social](https://lipis.github.io/bootstrap-social/) for provider login buttons, fork it if necessary.
3. By now, you have authenticated yourself as an admin on OAuthly platform. Now you will configure your applications and services (OAuth 2.0 Clients) to connect to OAuthly (OAuth 2.0 Authorization Server). Go to <http://localhost:9000/client> to create one client, by setting its `name` and `redirect_uri`. 
4. Set generated Client ID and Client Secret and following endpoint addresses on your OAuth 2.0 Client Application:

- Authorize endpoint: http://localhost:9000/oauth/authorize

    Example: `curl -v 'http://localhost:9000/oauth/authorize?client_id=vpNS2x3QTVSxjTuWUrY3&response_type=code&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Flogin'`
    
- Token endpoint: http://localhost:9000/oauth/token (Use POST with FORM parameters)

    Example: `curl -v -X POST -d 'grant_type=authorization_code&code=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJoIjoxMDI1MDQ5NzEzLCJyIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL2xvZ2luIiwiZXhwIjoxNTE1MDg0OTM3LCJ2dCI6MywiZyI6IjExbU9tMHVVMEQwOHMxSXo5S3RMIn0.wtLx54iK1kEWhXAVU5gb6AnyPQnN1Qb2r4L-s20TADk&client_id=vpNS2x3QTVSxjTuWUrY3&client_secret=0JPSlNiGKRmcgqidu77s&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Flogin' 'http://localhost:9000/oauth/token'`

- User info endpoint: http://localhost:9000/api/me (Use `Authorization: Bearer token` header)

    Example: `curl -v -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJoIjoxMDI1MDQ5NzEzLCJleHAiOjE1MTUwODUxNTYsImdyYW50IjoiMTFtT20wdVUwRDA4czFJejlLdEwiLCJ2dCI6MX0.sl4F-ik9Tstw38JxOSfHYUCi1cN4NxYmqgNCoA0hIbA" http://localhost:9000/api/me`
    
## Instructions for production mode

Please first follow the instructions for local mode AND run it on your local machine before attempting this. If you don't have that much time, AT LEAST read them once.

0. OAuthly must run on a seperate domain. If your domain is `example.com`, running on `oauthly.example.com` is recommended.
1. `sbt stage` in root folder to prepare distribution. 
2. Move `target/universal/stage/oauthly-1.0-SNAPSHOT.zip` to your server and extract it to some place.
3. Create account on <http://mailgun.com> and get a (free) API key for a valid domain of yours. It is absolutely essential to have email support but we don't support SMTP for now (PR welcome).
4. Create account on <https://www.google.com/recaptcha/admin> and get `recaptcha.siteKey` and `recaptcha.secret`. 
5. (Optional) If you want to allow Login with Facebook and/or Google, get your keys as specified above.
6. Prepare `conf/prod.conf` file from following template. Put random values for `jwt.secret` and `play.http.secret.key`. 

```
include "application.conf"

http.port=9000
play.filters.enabled=[
  play.filters.csrf.CSRFFilter,
  play.filters.headers.SecurityHeadersFilter,
  play.filters.hosts.AllowedHostsFilter,
]
play.filters.hosts.allowed = ["oauthly.example.com"]
playjongo.uri="mongodb://127.0.0.1:27017/oauthly"
play.http.secret.key=
jwt.secret=
recaptcha.enabled=true
recaptcha.siteKey=
recaptcha.secret=
mail.mailgun.key=key-123
mail.mailgun.domain=example.com
mail.mailgun.from="OAuthly <noreply@example.com>"
brand.name="My Brand"
tos.text="""
Terms of Service content
"""

oauth.providers=[
  {
    key=facebook
    displayName=Facebook
    clientId=
    clientSecret=
    tokenUrl="https://graph.facebook.com/v2.11/oauth/access_token"
    authorizeUrl="https://www.facebook.com/v2.11/dialog/oauth"
    scopes="public_profile email"
    userInfoUrl="https://graph.facebook.com/me?fields=id,name,email"
  }
]
```

7. `bin/oauthly -Dconfig.resource=prod.conf` to start the server. 
8. TLS is a must! You can follow <https://www.playframework.com/documentation/2.6.x/ConfiguringHttps> for configuring it but I recommend setting up TLS on a reverse proxy like nginx. 

## Features

- Uses Play Framework (Java) 2.6.x 
- Fully supported OAuth2 grant types: client credentials, authorization code, resource owner password, refresh token
- Login, register, profile, user management, client management and authorize client views with Bootstrap
- Supports logging in with social providers and advanced account linking features
- Supports sending email confirmation links
- Utilizes JWT for tokens, authorization codes and cookies
- Completely stateless server side logic
- Logged-in users are remembered with long-term safe cookies
- Multiple client id and secret pairs are supported, managed by a view
- Customizable expiry times for generated tokens (see `application.conf` file)
- Google reCAPTCHA support on endpoints (with `@RecaptchaProtected` annotation)
- OAuth2 scopes support
- MongoDB backend
- Mailgun API integration for sending emails
- Uses bcrypt for user passwords, [twirl](https://playframework.com/documentation/2.6.x/JavaTemplates) for templating
- [Discourse SSO](https://meta.discourse.org/t/official-single-sign-on-for-discourse/13045) support

## Screenshots (needs update)

![login](http://i.imgur.com/WpLsqYY.png)
![register](http://i.imgur.com/dCoEENL.png)
![reset-password](http://i.imgur.com/XeSO0vB.png)
![profile](http://i.imgur.com/oRrz6Iz.png)
![authorize](https://i.imgur.com/5FMlHCz.png)

## TODO (PRs are welcome!)
- Enable/disable user account function on user management view
- Possible production example with let's encrypt certificates, docker container and nginx
- Event logging
- Scope CRUD screen
- More social provider support

## Discourse SSO Instructions
OAuthly fully supports [Discourse SSO](https://meta.discourse.org/t/official-single-sign-on-for-discourse/13045)
integration. In this configuration, the single source of users becomes oauthly. When Login button is clicked on Discourse,
user is redirected to oauthly login page. If the user has already authenticated with oauthly with a long term token,
he/she immediately gets redirected back to discourse with user information. Follow the instructions below to configure
your system.

1. Login with admin and go to /discourse
2. Tick enabled in discourse settings panel, enter sso return url. It is usually `http://DISCOURSE_SERVER/session/sso_login`
3. Hit save and copy the generated secret
4. Open discourse Admin -> Settings -> Login,
    1. 'enable sso' -> true
    2. 'sso url' -> `http://OAUTHLY_SERVER/discourse/sso`
    3. 'sso secret' -> (secret from step 3)
    4. 'sso overrides username and email' -> true
    5. Users -> 'logout redirect' -> `http://OAUTHLY_SERVER/logout`
5. Done, you may test the integration. Remember that the admin account must have the same email address on both
systems, or you'll get locked out.
