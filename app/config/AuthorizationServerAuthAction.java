package config;

import controllers.routes;
import models.User;
import play.libs.typedmap.TypedKey;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class AuthorizationServerAuthAction extends play.mvc.Action.Simple {

    private final JwtUtils jwtUtils;

    @Inject
    public AuthorizationServerAuthAction(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public CompletionStage<Result> call(Http.Context ctx) {
//        HandlerDef handler = requestHeader.attrs().get(Router.Attrs.HANDLER_DEF);
//        List<String> modifiers = handler.getModifiers();
//        if(modifiers.contains("authorization")){
//        }
        Http.Cookie ltat = ctx.request().cookie("ltat");
        boolean valid = false;
        if(ltat != null){
            User user = jwtUtils.validateCookie(ltat.value());
            if(user != null) {
                // uses request attr
                ctx = ctx.withRequest(ctx.request().addAttr(AuthorizationServerSecure.USER, user));
                // uses ctx args
//                ctx.args.put("user", user);
                valid = true;
            }
        }
        if(!valid) {
            return CompletableFuture.completedFuture(redirect(routes.LoginController.get(ctx.request().uri())));
        } else { // pass on
            return delegate.call(ctx);
        }
    }
}
