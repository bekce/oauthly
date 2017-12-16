package dtos;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class OAuthProvider {

	private final String tokenUrl;
    private final String authorizeUrl;
	private final String clientId;
	private final String clientSecret;
	private final String scopes;
	private final String userInfoUrl;
	private final Function<OAuthContext, CompletionStage<Token>> tokenRetriever;
	private final Function<OAuthContext, CompletionStage<MeDto>> currentUserIdentifier;

	public OAuthProvider(String tokenUrl, String authorizeUrl, String clientId, String clientSecret, String scopes, String userInfoUrl, Function<OAuthContext, CompletionStage<Token>> tokenRetriever, Function<OAuthContext, CompletionStage<MeDto>> currentUserIdentifier) {
		this.tokenUrl = tokenUrl;
		this.authorizeUrl = authorizeUrl;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.scopes = scopes;
		this.userInfoUrl = userInfoUrl;
		this.tokenRetriever = tokenRetriever;
		this.currentUserIdentifier = currentUserIdentifier;
	}

	public String getTokenUrl() {
		return tokenUrl;
	}

	public String getAuthorizeUrl() {
		return authorizeUrl;
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public String getScopes() {
		return scopes;
	}

	public String getUserInfoUrl() {
		return userInfoUrl;
	}

	public Function<OAuthContext, CompletionStage<MeDto>> getCurrentUserIdentifier() {
		return currentUserIdentifier;
	}

	public Function<OAuthContext, CompletionStage<Token>> getTokenRetriever() {
		return tokenRetriever;
	}

	@Override
	public String toString() {
		return "OAuthProvider{" +
				"tokenUrl='" + tokenUrl + '\'' +
				", authorizeUrl='" + authorizeUrl + '\'' +
				", clientId='" + clientId + '\'' +
				", clientSecret='" + clientSecret + '\'' +
				", scopes='" + scopes + '\'' +
				", userInfoUrl='" + userInfoUrl + '\'' +
				'}';
	}
}
