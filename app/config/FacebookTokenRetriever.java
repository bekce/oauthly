package config;

import dtos.OAuthContext;
import dtos.Token;
import play.libs.Json;

import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class FacebookTokenRetriever implements Function<OAuthContext, CompletionStage<Token>> {
    @Override
    public CompletionStage<Token> apply(OAuthContext context) {
        return context.getWs().url(context.getProvider().getTokenUrl())
                .addQueryParameter("client_id", context.getProvider().getClientId())
                .addQueryParameter("client_secret", context.getProvider().getClientSecret())
                .addQueryParameter("code", context.getCode())
                .addQueryParameter("redirect_uri", context.getRedirectUri())
                .addQueryParameter("grant_type", "authorization_code")
                .get()
                .handleAsync((res, e) -> {
                    if(e != null) {
                        play.Logger.error("retrieveToken: exception", e);
                        throw new CompletionException(e);
                    } else if(res.getStatus() != 200) {
                        String message = String.format("retrieveToken: status=%s, body=%s", res.getStatus(), res.getBody());
                        play.Logger.error(message);
                        throw new CompletionException(new IllegalStateException(message));
                    }
                    Token token = Json.fromJson(res.asJson(), Token.class);
                    token.setCreatedAt(System.currentTimeMillis());
                    context.setToken(token);
                    return token;
                });
    }
}
