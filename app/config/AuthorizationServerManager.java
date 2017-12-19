package config;

import com.typesafe.config.Config;
import dtos.*;
import play.Logger;
import scala.Tuple2;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class AuthorizationServerManager {

    private Map<String, OAuthProvider> providerMap;
    private List<Tuple2<String, String>> providerList;

    @Inject
    public AuthorizationServerManager(Config config){
        this.providerMap = new LinkedHashMap<>();
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
                        provider.getString("displayName"),
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
        providerList = providerMap.entrySet().stream().map(e -> Tuple2.apply(e.getKey(), e.getValue().getDisplayName())).collect(Collectors.toList());
        Logger.info("Registered third party providers: {}", providerMap);
    }

    public OAuthProvider getProvider(String provider) {
        return providerMap.get(provider);
    }

    public List<Tuple2<String, String>> getProviders(){
        return providerList;
    }
}
