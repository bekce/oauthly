package dtos;

import play.libs.Json;
import play.libs.ws.WSClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.CompletionException;
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
		if(redirectUri == null){
			throw new IllegalStateException("No redirectUri present");
		}
		if(state == null){
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
		if(code == null){
			throw new IllegalStateException("No code present");
		}
		if(redirectUri == null){
			throw new IllegalStateException("No redirectUri present");
		}
//		return ws.url(provider.getTokenUrl())
//				.setContentType("application/x-www-form-urlencoded")
//				.post(String.format("client_id=%s&client_secret=%s&grant_type=authorization_code&code=%s&redirect_uri=%s", provider.getClientId(), provider.getClientSecret(), code, redirectUri))
		try {
//			String url = provider.getTokenUrl() + String.format("?client_id=%s&client_secret=%s&code=%s&redirect_uri=%s", provider.getClientId(), provider.getClientSecret(), URLEncoder.encode(code, "utf-8"), URLEncoder.encode(redirectUri, "utf-8"));
//			System.out.println("URL="+url);
			return ws.url(provider.getTokenUrl())
					.addQueryParameter("client_id", provider.getClientId())
					.addQueryParameter("client_secret", provider.getClientSecret())
					.addQueryParameter("code", code)
					.addQueryParameter("redirect_uri", redirectUri)
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
                        this.token = token;
                        return token;
                    });
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
