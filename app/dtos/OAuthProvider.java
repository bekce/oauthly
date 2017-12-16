package dtos;

public class OAuthProvider {

	private final String tokenUrl;
    private final String authorizeUrl;
	private final String clientId;
	private final String clientSecret;
	private final String scopes;
	private final String userInfoUrl;

	public OAuthProvider(String tokenUrl, String authorizeUrl, String clientId, String clientSecret, String scopes, String userInfoUrl) {
		this.tokenUrl = tokenUrl;
		this.authorizeUrl = authorizeUrl;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.scopes = scopes;
		this.userInfoUrl = userInfoUrl;
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
