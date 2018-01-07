package config;


import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import com.typesafe.config.Config;
import controllers.routes;
import dtos.Token;
import dtos.TokenStatus;
import models.Client;
import models.Grant;
import models.User;
import play.mvc.Http;
import play.mvc.Result;
import repositories.ClientRepository;
import repositories.GrantRepository;
import repositories.UserRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.time.Duration;
import java.util.*;

import play.Logger;
import scala.Tuple2;

import static play.mvc.Results.redirect;

@Singleton
public class JwtUtils {

    private final String jwtSecret;
    private final Duration expireCookie;
    private final Duration expireResetCode;
    private final Duration expireAccessToken;
    private final Duration expireRefreshToken;
    private final Duration expireAuthorizationCode;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final GrantRepository grantRepository;

    @Inject
    public JwtUtils(Config config, UserRepository userRepository, ClientRepository clientRepository, GrantRepository grantRepository) {
        jwtSecret = config.getString("jwt.secret");
        expireCookie = config.getDuration("jwt.expire.cookie");
        expireResetCode = config.getDuration("jwt.expire.resetCode");
        expireAccessToken = config.getDuration("jwt.expire.accessToken");
        expireRefreshToken = config.getDuration("jwt.expire.refreshToken");
        expireAuthorizationCode = config.getDuration("jwt.expire.authorizationCode");
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.grantRepository = grantRepository;
    }

    public String prepareResetCode(User user) {
        int hash = Objects.hash(user.getUsername(), user.getEmail(), user.getPassword(), user.getLastUpdateTime());
        final long iat = System.currentTimeMillis() / 1000L; // issued at claim
        final long exp = iat + expireResetCode.getSeconds(); // expires claim
        final JWTSigner signer = new JWTSigner(jwtSecret);

        final HashMap<String, Object> claims = new HashMap<>();
        claims.put("vt", 5); //type=reset_code
        claims.put("exp", exp);
        claims.put("hash", hash);
        claims.put("user", user.getId());

        return signer.sign(claims);
    }

    public User validateResetCode(String reset_code){
        if(reset_code == null)
            return null;
        try {
            final JWTVerifier verifier = new JWTVerifier(jwtSecret);
            final Map<String,Object> claims = verifier.verify(reset_code);
            // first check version
            int type = (int) claims.get("vt");
            if(type != 5){
                return null;
            }
            // check expiry date
            int exp = (int) claims.get("exp");
            if(exp < (System.currentTimeMillis() / 1000L))
                return null;
            // check user
            String user_id = (String) claims.get("user");
            User user = userRepository.findById(user_id);
            if(user == null){
                return null;
            }
            // check hash value
            int receivedHash = (int) claims.get("hash");
            int correctHash = Objects.hash(user.getUsername(), user.getEmail(), user.getPassword(), user.getLastUpdateTime());
            if(receivedHash != correctHash) {
                return null;
            }
            return user;
        } catch (JWTVerifyException | SignatureException | InvalidKeyException | NullPointerException e) {
            return null;
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
            return null;
        }
    }

    public String prepareEmailConfirmationCode(User user, String linkId) {
        final long iat = System.currentTimeMillis() / 1000L; // issued at claim
        final long exp = iat + expireResetCode.getSeconds(); // expires claim
        final JWTSigner signer = new JWTSigner(jwtSecret);

        final HashMap<String, Object> claims = new HashMap<>();
        claims.put("vt", 6); //type=email_confirm_code
        claims.put("exp", exp);
        claims.put("email", user.getEmail());
        claims.put("username", user.getUsername());
        if(user.getPassword() != null)
            claims.put("password", user.getPassword());
        if(linkId != null)
            claims.put("linkId", linkId);

        return signer.sign(claims);
    }

    public Tuple2<User, String> validateEmailConfirmationCode(String code){
        if(code == null)
            return null;
        try {
            final JWTVerifier verifier = new JWTVerifier(jwtSecret);
            final Map<String,Object> claims = verifier.verify(code);
            // first check version
            int type = (int) claims.get("vt");
            if(type != 6){
                return null;
            }
            // check expiry date
            int exp = (int) claims.get("exp");
            if(exp < (System.currentTimeMillis() / 1000L))
                return null;
            // check user
            User user = new User();
            user.setEmail((String) claims.get("email"));
            user.setUsername((String) claims.get("username"));
            if(claims.containsKey("password"))
                user.setPassword((String) claims.get("password"));
            String linkId = claims.containsKey("linkId") ? (String) claims.get("linkId") : null;
            return Tuple2.apply(user, linkId);
        } catch (JWTVerifyException | SignatureException | InvalidKeyException | NullPointerException e) {
            return null;
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
            return null;
        }
    }

    public String prepareEmailChangeConfirmationCode(User user, String newEmail) {
        int hash = Objects.hash(user.getUsername(), user.getEmail(), user.getPassword());
        final long iat = System.currentTimeMillis() / 1000L; // issued at claim
        final long exp = iat + expireResetCode.getSeconds(); // expires claim
        final JWTSigner signer = new JWTSigner(jwtSecret);

        final HashMap<String, Object> claims = new HashMap<>();
        claims.put("vt", 7); //type=email_change_confirm_code
        claims.put("exp", exp);
        claims.put("user", user.getId());
        claims.put("hash", hash);
        claims.put("new_email", newEmail);
        return signer.sign(claims);
    }


    public Tuple2<User, String> validateEmailChangeConfirmationCode(String code){
        if(code == null)
            return null;
        try {
            final JWTVerifier verifier = new JWTVerifier(jwtSecret);
            final Map<String,Object> claims = verifier.verify(code);
            // first check version
            int type = (int) claims.get("vt");
            if(type != 7){
                return null;
            }
            // check expiry date
            int exp = (int) claims.get("exp");
            if(exp < (System.currentTimeMillis() / 1000L))
                return null;
            // check user
            User user = userRepository.findById((String) claims.get("user"));
            if(user == null) return null;
            // check hash
            int hash = Objects.hash(user.getUsername(), user.getEmail(), user.getPassword());
            if((Integer) claims.get("hash") != hash) return null;
            String newEmail = (String) claims.get("new_email");
            return Tuple2.apply(user, newEmail);
        } catch (JWTVerifyException | SignatureException | InvalidKeyException | NullPointerException e) {
            return null;
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
            return null;
        }
    }

    public Result prepareCookieThenRedirect(User user, String next){
        Http.Cookie ltat = prepareCookie(user);
        if(next != null && next.matches("^/.*$"))
            return redirect(next).withCookies(ltat);
        else
            return redirect(routes.ProfileController.get()).withCookies(ltat);
    }

    public Http.Cookie prepareCookie(User user){
        return Http.Cookie.builder("ltat", prepareCookieValue(user)).withPath("/").withHttpOnly(true).withMaxAge(expireCookie).build();
    }

    public String prepareCookieValue(User user) {
        int hash = Objects.hash(user.getUsername(), user.getEmail(), user.getPassword());
        final long iat = System.currentTimeMillis() / 1000L; // issued at claim
        final long exp = iat + expireCookie.getSeconds(); // expires claim
        final JWTSigner signer = new JWTSigner(jwtSecret);

        final HashMap<String, Object> claims = new HashMap<>();
        claims.put("vt", 4); //type=cookie_ltat
        claims.put("exp", exp);
        claims.put("hash", hash);
        claims.put("user", user.getId());

        return signer.sign(claims);
    }

    public User validateCookie(String cookie_value){
        if(cookie_value == null)
            return null;
        try {
            final JWTVerifier verifier = new JWTVerifier(jwtSecret);
            final Map<String,Object> claims = verifier.verify(cookie_value);
            // first check version
            int type = (int) claims.get("vt");
            if(type != 4){
                return null;
            }
            // check expiry date
            int exp = (int) claims.get("exp");
            if(exp < (System.currentTimeMillis() / 1000L))
                return null;
            // check user
            String user_id = (String) claims.get("user");
            User user = userRepository.findById(user_id);
            if(user == null){
                return null;
            }
            // check hash value
            int receivedHash = (int) claims.get("hash");
            int correctHash = Objects.hash(user.getUsername(), user.getEmail(), user.getPassword());
            if(receivedHash != correctHash) {
                return null;
            }
            return user;
        } catch (JWTVerifyException | SignatureException | InvalidKeyException | NullPointerException e) {
            return null;
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
            return null;
        }
    }


    public String prepareAuthorizationCode(String client_id, String client_secret, String grant_id, String redirect_uri) {
        int hash = Objects.hash(client_id, client_secret);
        final long iat = System.currentTimeMillis() / 1000L; // issued at claim
        final long exp = iat + expireAuthorizationCode.getSeconds();
        final JWTSigner signer = new JWTSigner(jwtSecret);

        final HashMap<String, Object> claims = new HashMap<>();
        claims.put("vt", 3); //type=authorization_code
        claims.put("exp", exp);
        claims.put("h", hash);
        claims.put("g", grant_id);
        claims.put("r", redirect_uri);
        return signer.sign(claims);
    }

    public Grant validateAuthorizationCode(String code, String redirect_uri){
        if(code == null || redirect_uri == null)
            return null;
        try {
            final JWTVerifier verifier = new JWTVerifier(jwtSecret);
            final Map<String,Object> claims = verifier.verify(code);
            // first check version
            int type = (int) claims.get("vt");
            if(type != 3){
                return null;
            }
            // check expiry date
            int exp = (int) claims.get("exp");
            if(exp < (System.currentTimeMillis() / 1000L))
                return null;
            // check grant
            String grant_id = (String) claims.get("g");
            Grant grant = grantRepository.findById(grant_id);
            if(grant == null){
                return null;
            }
            // check hash value
            int receivedHash = (int) claims.get("h");
            Client client = clientRepository.findById(grant.getClientId());
            if(client == null){
                return null;
            }
            int correctHash = Objects.hash(client.getId(), client.getSecret());
            if(receivedHash != correctHash) {
                return null;
            }
            // check redirect_uri
            if(!Objects.equals(claims.get("r"), redirect_uri)){
                return null;
            }
            return grant;
        } catch (JWTVerifyException | SignatureException | InvalidKeyException | NullPointerException e) {
            return null;
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
            return null;
        }
    }


    /**
     * Prepares & issues a new token and returns it.
     * It puts the following information to the token:
     * <li>
     * <ul>version 'v'</ul>
     * <ul>expiry date 'exp'</ul>
     * <ul>combined hash 'h' value of client_id, client_secret, username and password.</ul><br>
     * This hash is important because if either one of the parameters change, the change in hash value will render the token invalid. Example: if password of user changes, the token will automatically get invalid. Note that this hash is not technically bulletproof, there exist very little (approx. 1 in 4*10^9) chance that it will compute the same hash value.
     * <ul>token type 't' -> 'a' (access) or 'r' (refresh)</ul>
     * </li>
     * It prepares both access and refresh tokens, signs them using JWT and packs in a Token object.
     * Note that this single user implementation does not put username to the claims map.
     *
     * @return generated token
     */
    public Token prepareToken(String client_id, String client_secret, String grant_id, Collection<String> scopes) {
        int hash = Objects.hash(client_id, client_secret);
        final long iat = System.currentTimeMillis() / 1000L; // issued at claim
        final long exp = iat + expireAccessToken.getSeconds(); // expires claim. In this case the token expires in 60 seconds
        final JWTSigner signer = new JWTSigner(jwtSecret);

        final HashMap<String, Object> claims = new HashMap<>();
        claims.put("vt", 1); //version=1 & type=access_token
        claims.put("exp", exp);
        claims.put("h", hash);
        claims.put("grant", grant_id);
        final String token_a = signer.sign(claims);

        final HashMap<String, Object> claims_r = new HashMap<>();
        final long exp_r = iat + expireRefreshToken.getSeconds(); // refresh token expire time: 1 week
        claims_r.put("vt", 2); //version=1 & type=refresh_token
        claims_r.put("exp", exp_r);
        claims_r.put("h", hash);
        claims_r.put("grant", grant_id);
        final String token_r = signer.sign(claims_r);

        /* The last parameter (scope) is entirely optional. You can use it to implement scoping requirements. If you would like so, put it to claims map to verify it. */
        return new Token(token_a, token_r, "bearer", expireAccessToken.getSeconds(), scopes == null ? "" : String.join(" ", scopes));
    }

    /**
     * Validate given token and return its type
     * @see TokenStatus
     */
    public Tuple2<Grant, TokenStatus> getTokenStatus(String access_token){
        if(access_token == null)
            return new Tuple2<>(null, TokenStatus.INVALID);
        try {
            final JWTVerifier verifier = new JWTVerifier(jwtSecret);
            final Map<String,Object> claims = verifier.verify(access_token);
            // first check version
            int type = (int) claims.get("vt");
            if(type != 1 && type != 2){
                return new Tuple2<>(null, TokenStatus.INVALID);
            }
            // check expiry date
            int exp = (int) claims.get("exp");
            if(exp < (System.currentTimeMillis() / 1000L))
                return new Tuple2<>(null, TokenStatus.INVALID);
            // check grant
            String grant_id = (String) claims.get("grant");
            Grant grant = grantRepository.findById(grant_id);
            if(grant == null){
                return new Tuple2<>(null, TokenStatus.INVALID);
            }
            Client client = clientRepository.findById(grant.getClientId());
            if(client == null){
                return new Tuple2<>(null, TokenStatus.INVALID); // how can this be?
            }
            // check hash value
            int receivedHash = (int) claims.get("h");
            int correctHash = Objects.hash(client.getId(), client.getSecret());
            if(receivedHash != correctHash) {
                return new Tuple2<>(null, TokenStatus.INVALID);
            }
            // check token type & version
            if(type == 1){
                return new Tuple2<>(grant, TokenStatus.VALID_ACCESS);
            } else {
                return new Tuple2<>(grant, TokenStatus.VALID_REFRESH);
            }
        } catch (JWTVerifyException | SignatureException | InvalidKeyException | NullPointerException e) {
            return new Tuple2<>(null, TokenStatus.INVALID);
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
            return new Tuple2<>(null, TokenStatus.INVALID);
        }
    }

}
