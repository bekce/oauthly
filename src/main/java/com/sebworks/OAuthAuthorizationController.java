package com.sebworks;

import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This controller implements oauth2 authorization server semantics with password grant type.
 * Supports refresh tokens.
 * Works with request params in token post method for the requirement, it is best to modify it for generic usage.
 * It uses JWT to issue tokens, so tokens are self contained and there is no token store.
 * It works with {@link OAuthFilter} to protect resources.
 * Currently it works with a single user and client id, but it is easy to extend for multiple users.
 *
 * Created by Selim Eren Bekçe on 2016-08-25.
 */
@RestController
@RequestMapping("/oauth")
public class OAuthAuthorizationController implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(OAuthAuthorizationController.class);

    @Value("${oauth.server.client.id}")
    private String clientId;
    @Value("${oauth.server.client.secret}")
    private String clientSecret;
    @Value("${oauth.server.username}")
    private String username;
    @Value("${oauth.server.password}")
    private String password;
    @Value("${oauth.server.jwt.secret}")
    private String jwtSecret;
    /** In seconds */
    @Value("${oauth.server.expire.access.token}")
    private long expireAccessToken;
    /** In seconds */
    @Value("${oauth.server.expire.refresh.token}")
    private long expireRefreshToken;

    private String basicAuthValue;

    @Override
    public void afterPropertiesSet() throws Exception {
        // precompute the basic auth value for fast checks
        String s = clientId + ":" + clientSecret;
        basicAuthValue = "Basic " + Base64.encodeBase64String(s.getBytes("utf-8"));
    }

    /**
     * Validate given token and return its type
     * @see TokenStatus
     */
    public TokenStatus getTokenStatus(String access_token){
        if(access_token == null)
            return TokenStatus.INVALID;
        try {
            final JWTVerifier verifier = new JWTVerifier(jwtSecret);
            final Map<String,Object> claims = verifier.verify(access_token);
            // check expiry date
            int exp = (int) claims.get("exp");
            if(exp < (System.currentTimeMillis() / 1000L))
                return TokenStatus.INVALID;
            // check hash value
            int receivedHash = (int) claims.get("h");
            int correctHash = Objects.hash(clientId, clientSecret, username, password);
            if(receivedHash != correctHash) {
                return TokenStatus.INVALID;
            }
            // check token type
			Object type = claims.get("t");
			if("a".equals(type))
                return TokenStatus.VALID_ACCESS;
            else if("r".equals(type))
                return TokenStatus.VALID_REFRESH;
            else{
                log.debug("Unknown token type: "+ type);
                return TokenStatus.INVALID;
            }
        } catch (JWTVerifyException | SignatureException | InvalidKeyException | NullPointerException e) {
            return TokenStatus.INVALID;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return TokenStatus.INVALID;
        }
    }

    /**
     * This endpoint issues a new token to the client.
     * Two grant_types are supported: 'password' and 'refresh_token'
     * Its parameters can be customized to fit project needs. Currently it requires client_id and client_secret as Basic Authorization header.
     * @param username the resource owner username when grant_type is 'password'
     * @param password the resource owner password when grant_type is 'password'
     * @param grant_type supported grant types: 'password' or 'refresh_token'
     * @param refresh_token the previously retrieved refresh_token when grant_type is 'refresh_token'
     * @param authorization_header The client_id and client_secret as basic authorization header:
     *                             'Basic '+base64encode(client_id+':'+client_secret)
     * @return token
     */
    @RequestMapping(value = "/token", method = RequestMethod.POST)
    public ResponseEntity<Token> token(@RequestParam(value = "username", required = false) String username,
                                       @RequestParam(value = "password", required = false) String password,
                                       @RequestParam("grant_type") String grant_type,
                                       @RequestParam(value = "refresh_token", required = false) String refresh_token,
                                       @RequestHeader("Authorization") String authorization_header) {

        // Here we expect the client to send client_id:client_secret as Basic auth.
        // This behavior can easily be changed depending on the requirements.
        if(!basicAuthValue.equals(authorization_header)){
            return ResponseEntity.badRequest().body(new Token("unauthorized"));
        }

        switch (grant_type) {
            case "password": {
                if (!this.username.equals(username) || !this.password.equals(password)) {
                    return ResponseEntity.badRequest().body(new Token("invalid_grant"));
                }
                Token token = prepareToken();
                return ResponseEntity.ok(token);
            }
            case "refresh_token": {
                if (getTokenStatus(refresh_token) == TokenStatus.VALID_REFRESH) {
                    return ResponseEntity.badRequest().body(new Token("invalid_refresh_token"));
                }
                Token token = prepareToken();
                return ResponseEntity.ok(token);
            }
            default:
                return ResponseEntity.badRequest().body(new Token("unsupported_grant_type"));
        }
    }

    /**
     * Prepares & issues a new token and returns it.
     * It puts the following information to the token:
     * - version 'v'
     * - expiry date 'exp'
     * - combined hash value of client_id, client_secret, username and password.
     * This hash is important because if either one of the parameters change, the change in hash value will render the token invalid. Example: if password of user changes, the token will automatically get invalid. Note that this hash is not technically bulletproof, there exist very little (approx. 1 in 4*10^9) chance that it will compute the same hash value.
     * - token type 't' -> 'a' (access) or 'r' (refresh)
     * It prepares both access and refresh tokens, signs them using JWT and packs in a Token object.
     * Note that this single user implementation does not put username to the claims map.
     *
     * @return generated token
     */
    private Token prepareToken() {
        int hash = Objects.hash(clientId, clientSecret, username, password);
        final long iat = System.currentTimeMillis() / 1000L; // issued at claim
        final long exp = iat + expireAccessToken; // expires claim. In this case the token expires in 60 seconds
        final JWTSigner signer = new JWTSigner(jwtSecret);
        final HashMap<String, Object> claims = new HashMap<>();
        claims.put("v", 1); //version
        claims.put("exp", exp);
        claims.put("h", hash);
        claims.put("t", "a");
        final String token_a = signer.sign(claims);

        final HashMap<String, Object> claims_r = new HashMap<>();
        final long exp_r = iat + expireRefreshToken; // refresh token expire time: 1 week
        claims_r.put("v", 1); //version
        claims_r.put("exp", exp_r);
        claims_r.put("h", hash);
        claims_r.put("t", "r");
        final String token_r = signer.sign(claims_r);

        /* The last parameter (scope) is entirely optional. You can use it to implement scoping requirements. If you would like so, put it to claims map to verify it. */
        return new Token(token_a, token_r, "bearer", expireAccessToken, "read write");
    }
}
