package config;

import dtos.TokenStatus;
import models.Grant;
import org.apache.commons.lang3.StringUtils;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import scala.Tuple2;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ResourceServerAuthAction extends play.mvc.Action<ResourceServerSecure> {

    @Inject
    private JwtUtils jwtUtils;

    @Override
    public CompletionStage<Result> call(Http.Context ctx) {

        Http.RequestHeader requestHeader = ctx._requestHeader().asJava();
        String token = requestHeader.header("Authorization")
                .filter(s -> StringUtils.startsWithIgnoreCase(s, "Bearer "))
                .map(s -> s.substring("Bearer ".length()).trim())
                .orElseGet(() -> requestHeader.getQueryString("access_token"));

        Tuple2<Grant, TokenStatus> tuple = jwtUtils.getTokenStatus(token);
        if(tuple._2 == TokenStatus.VALID_ACCESS) {
            Grant grant = tuple._1;

            boolean scopeOK = true;
            List<String> scopes = Arrays.asList(configuration.scope().split(" "));
            for (String s : scopes) {
                if(!grant.getScopes().contains(s)){
                    scopeOK = false;
                    break;
                }
            }
            if(scopeOK) {
                ctx = ctx.withRequest(ctx.request().addAttr(ResourceServerSecure.GRANT, grant));
                return delegate.call(ctx);
            } else {
                return CompletableFuture.completedFuture(unauthorized(Json.newObject()
                                .put("message", "insufficient scope")
                                .put("scope", configuration.scope()))
                );
            }
        }

        return CompletableFuture.completedFuture(
                unauthorized(Json.newObject().put("message", "invalid or non-existing access token"))
        );
    }
}
