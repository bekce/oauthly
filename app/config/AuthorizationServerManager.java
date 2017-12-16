package config;

import com.typesafe.config.Config;
import dtos.*;
import play.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

@Singleton
public class AuthorizationServerManager {

    private Map<String, OAuthProvider> providerMap;

    @Inject
    public AuthorizationServerManager(Config config){
        this.providerMap = new HashMap<>();
        try {
            List<? extends Config> providers = config.getConfigList("oauth.providers");
            for (Config provider : providers) {
                String key = provider.getString("key");
                Function<OAuthContext, CompletionStage<Token>> tokenRetriever;
                Function<OAuthContext, CompletionStage<MeDto>> currentUserIdentifier;
                switch (key) {
                    case "facebook":
                        currentUserIdentifier = new GenericCurrentUserIdentifier();
                        tokenRetriever = new FacebookTokenRetriever();
                        break;
                    default:
                        throw new IllegalArgumentException("provider is not recognized");
                }
                providerMap.put(key, new OAuthProvider(
                        provider.getString("tokenUrl"),
                        provider.getString("authorizeUrl"),
                        provider.getString("clientId"),
                        provider.getString("clientSecret"),
                        provider.getString("scopes"),
                        provider.getString("userInfoUrl"),
                        tokenRetriever,
                        currentUserIdentifier));
            }
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
        }
        Logger.info("Registered third party providers: {}", providerMap);
    }

    public OAuthProvider getProvider(String provider) {
        return providerMap.get(provider);
    }
}
