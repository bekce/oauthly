package dtos;

import play.libs.ws.WSClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.CompletionStage;

public class OAuthContext {

    private Token token;
    private String code;
    private String redirectUri;
    private String scope;
    private String state;
    private OAuthProvider provider;
    private WSClient ws;

    public OAuthContext(OAuthProvider provider, WSClient ws) {
        this.provider = provider;
        this.ws = ws;
    }

    public String prepareAuthorizeUrl() {
        if (redirectUri == null) {
            throw new IllegalStateException("No redirectUri present");
        }
        if (state == null) {
            throw new IllegalStateException("No state present");
        }
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(provider.getAuthorizeUrl())
                    .append("?client_id=").append(provider.getClientId())
                    .append("&response_type=code")
                    .append("&redirect_uri=").append(URLEncoder.encode(redirectUri, "utf-8"))
                    .append("&state=").append(state);
            if (scope != null) {
                builder.append("&scope=").append(URLEncoder.encode(scope, "utf-8"));
            } else if (provider.getScopes() != null) {
                builder.append("&scope=").append(URLEncoder.encode(provider.getScopes(), "utf-8"));
            }
            return builder.toString();
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    public CompletionStage<Token> retrieveToken() {
        if (code == null) {
            throw new IllegalStateException("No code present");
        }
        if (redirectUri == null) {
            throw new IllegalStateException("No redirectUri present");
        }
//		return ws.url(provider.getTokenUrl())
//				.setContentType("application/x-www-form-urlencoded")
//				.post(String.format("client_id=%s&client_secret=%s&grant_type=authorization_code&code=%s&redirect_uri=%s", provider.getClientId(), provider.getClientSecret(), code, redirectUri))
        return provider.getTokenRetriever().apply(this);
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public OAuthProvider getProvider() {
        return provider;
    }

    public WSClient getWs() {
        return ws;
    }

    @Override
    public String toString() {
        return "OAuthContext{" +
                "token=" + token +
                ", code='" + code + '\'' +
                ", redirectUri='" + redirectUri + '\'' +
                ", scope='" + scope + '\'' +
                ", state='" + state + '\'' +
                ", provider=" + provider +
                '}';
    }
}
