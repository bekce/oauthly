package config;

import dtos.MeDto;
import dtos.OAuthContext;
import play.libs.ws.WSResponse;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class CurrentUserIdentifierOverUserInfoUrl implements Function<OAuthContext, CompletionStage<MeDto>> {

    private final Function<WSResponse, MeDto> responseToUserFunction;

    public CurrentUserIdentifierOverUserInfoUrl(Function<WSResponse, MeDto> responseToUserFunction) {
        this.responseToUserFunction = responseToUserFunction;
    }

    @Override
    public CompletionStage<MeDto> apply(OAuthContext context) {
        return context.getWs()
                .url(context.getProvider().getUserInfoUrl())
                .addHeader("Authorization", "Bearer " + context.getToken().getAccessToken())
                .get()
                .thenApplyAsync(responseToUserFunction);
    }

}
