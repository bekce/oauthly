package config;

import controllers.routes;
import models.User;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class AuthorizationServerAuthAction extends play.mvc.Action<AuthorizationServerSecure> {

    private final JwtUtils jwtUtils;

    @Inject
    public AuthorizationServerAuthAction(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public CompletionStage<Result> call(Http.Context ctx) {
        Http.Cookie ltat = ctx.request().cookie("ltat");
        boolean valid = false;
        if(ltat != null){
            User user = jwtUtils.validateCookie(ltat.value());
            if(user != null) {
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
