package config;

import dtos.OAuthContext;
import dtos.Token;
import play.libs.Json;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class GoogleTokenRetriever implements Function<OAuthContext, CompletionStage<Token>> {
    @Override
    public CompletionStage<Token> apply(OAuthContext context) {
        try {
            return context.getWs().url(context.getProvider().getTokenUrl())
                    .setContentType("application/x-www-form-urlencoded")
                    .post(String.format("code=%s&redirect_uri=%s&client_id=%s&client_secret=%s&grant_type=authorization_code",
                            URLEncoder.encode(context.getCode(), "utf-8"),
                            URLEncoder.encode(context.getRedirectUri(), "utf-8"),
                            URLEncoder.encode(context.getProvider().getClientId(), "utf-8"),
                            URLEncoder.encode(context.getProvider().getClientSecret(), "utf-8")))
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
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }
}
