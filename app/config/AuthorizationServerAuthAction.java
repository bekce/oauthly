package config;

import controllers.routes;
import models.User;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static play.mvc.Controller.flash;

public class AuthorizationServerAuthAction extends play.mvc.Action<AuthorizationServerSecure> {

    private final JwtUtils jwtUtils;

    @Inject
    public AuthorizationServerAuthAction(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public CompletionStage<Result> call(Http.Context ctx) {
        Http.Cookie ltat = ctx.request().cookie("ltat");
        boolean valid = configuration.optional();
        if(ltat != null) {
            User user = jwtUtils.validateCookie(ltat.value());
            if(user != null) {
                if (user.isDisabled()) {
                    flash("error", "Your account was disabled.");
                    Http.Cookie ltatRemove = Http.Cookie.builder("ltat", "")
                            .withPath("/").withHttpOnly(true).withMaxAge(Duration.ZERO).build();
                    return CompletableFuture.completedFuture(redirect(routes.LoginController.get(null)).withCookies(ltatRemove));
                }
                if(!configuration.requireAdmin() || user.isAdmin()){
                    ctx = ctx.withRequest(ctx.request().addAttr(AuthorizationServerSecure.USER, user));
                    valid = true;
                } else {
                    return CompletableFuture.completedFuture(unauthorized(Json.newObject()
                            .put("message", "you are not authorized for this operation")
                    ));
                }
            }
        }
        if(!valid) {
            return CompletableFuture.completedFuture(redirect(routes.LoginController.get(ctx.request().uri())));
        } else { // pass on
            return delegate.call(ctx);
        }
    }
}
