package config;


import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import com.typesafe.config.Config;
import models.User;
import repositories.UserRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import play.Logger;

@Singleton
public class JwtUtils {

    private final int expireCookie;
    private final int expireResetCode;
    private final UserRepository userRepository;
    private final String jwtSecret;

    @Inject
    public JwtUtils(Config config, UserRepository userRepository) {
        jwtSecret = config.getString("jwt.secret");
        expireCookie = config.getInt("jwt.expire.cookie");
        expireResetCode = config.getInt("jwt.expire.resetCode");
        this.userRepository = userRepository;
    }

    public String prepareResetCode(User user) {
        int hash = Objects.hash(user.getUsername(), user.getEmail(), user.getPassword(), user.getLastUpdateTime());
        final long iat = System.currentTimeMillis() / 1000L; // issued at claim
        final long exp = iat + expireResetCode; // expires claim
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

    public String prepareCookie(User user) {
        int hash = Objects.hash(user.getUsername(), user.getEmail(), user.getPassword());
        final long iat = System.currentTimeMillis() / 1000L; // issued at claim
        final long exp = iat + expireCookie; // expires claim
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

    public int getExpireCookie() {
        return expireCookie;
    }
}
