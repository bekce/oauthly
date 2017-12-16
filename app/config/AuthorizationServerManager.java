package config;

import com.typesafe.config.Config;
import dtos.OAuthProvider;
import play.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                providerMap.put(key, new OAuthProvider(
                        provider.getString("tokenUrl"),
                        provider.getString("authorizeUrl"),
                        provider.getString("clientId"),
                        provider.getString("clientSecret"),
                        provider.getString("scopes"),
                        provider.getString("userInfoUrl")));

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
