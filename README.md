# oauthly

[![Build Status](https://travis-ci.org/bekce/oauthly.svg?branch=master)](https://travis-ci.org/bekce/oauthly)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/18e70942adcf440e8c85d3e186c0e916)](https://www.codacy.com/app/seb_4/oauthly)

OAuth2 Authorization and Resource Server in Java with Play Framework. Suitable for being the OAuth2 authorization server for your software platform, with SSO support.

**Important Note** This project was started as a Spring Boot project (on branch `spring-boot`) then I have recently fully converted it to Play Framework. The reason for this change is the superior features of Play Framework, and a little bit of curiousity on my side.

There are a lot of authorization server examples on many platforms, such as Spring Boot, Play Framework, etc,
but none works as a full-fledged authorization server. This one does, for free. The functionality is comparable to
auth0, will be better in the future (with your PRs, of course).

## Instructions

0. Have a running mongodb instance and `sbt` installed.
1. `sbt run`
2. Go to <http://localhost:9000>, register a new account for yourself.
First account will be given admin access
3. Create a client, set its `name` and `redirect_uri` (required) through profile screen
4. Set following endpoint addresses on your application:

- Token endpoint: http://localhost:9000/oauth/token
- Authorize endpoint: http://localhost:9000/oauth/authorize
- User info endpoint: http://localhost:9000/api/me

## Features

- Uses Play Framework (Java) 2.6.x 
- Fully supported OAuth2 grant types: client credentials, authorization code, resource owner password, refresh token
- Login, register, profile, user management, client management and authorize client views with Bootstrap
- Supports logging in with social providers and advanced account linking features
- Utilizes JWT for tokens, authorization codes and cookies
- Completely stateless server side logic
- Logged-in users are remembered with long-term safe cookies
- Multiple client id and secret pairs are supported, managed by a view
- Customizable expiry times for generated tokens (see `application.conf` file)
- Google reCAPTCHA support on endpoints (with `@RecaptchaSecured` annotation)
- OAuth2 scopes support
- MongoDB backend
- Mailgun API integration for sending emails
- Uses bcrypt for user passwords, [twirl](https://playframework.com/documentation/2.6.x/JavaTemplates) for templating
- [Discourse SSO](https://meta.discourse.org/t/official-single-sign-on-for-discourse/13045) support

## Screenshots

![login](http://i.imgur.com/WpLsqYY.png)
![register](http://i.imgur.com/dCoEENL.png)
![reset-password](http://i.imgur.com/XeSO0vB.png)
![profile](http://i.imgur.com/oRrz6Iz.png)
![authorize](https://i.imgur.com/5FMlHCz.png)

## TODO (PRs are welcome!)
- Change email function
- OpenID default scopes with their meaning
- Enable/disable user account function on user management view
- Enable reCAPTCHA on login page after X number of failed requests
- Access logging
- Security checkup
- Possible production example with let's encrypt certificates, docker container and nginx
- Customized error pages
- Scope CRUD screen

## Discourse SSO Instructions
OAuthly fully supports [Discourse SSO](https://meta.discourse.org/t/official-single-sign-on-for-discourse/13045)
integration. In this configuration, the single source of users becomes oauthly. When Login button is clicked on Discourse,
user is redirected to oauthly login page. If the user has already authenticated with oauthly with a long term token,
he/she immediately gets redirected back to discourse with user information. Follow the instructions below to configure
your system.

1. Login with admin and go to /profile
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

Important note: currently, oauthly does NOT implement email code validation,
which is essential for running in production environments. If you're interested, please submit a PR!
