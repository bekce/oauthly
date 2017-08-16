package com.sebworks.oauthly.controller;

import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import com.sebworks.oauthly.OAuthFilter;
import com.sebworks.oauthly.SessionDataAccessor;
import com.sebworks.oauthly.Token;
import com.sebworks.oauthly.TokenStatus;
import com.sebworks.oauthly.entity.Client;
import com.sebworks.oauthly.entity.Grant;
import com.sebworks.oauthly.entity.User;
import com.sebworks.oauthly.repository.ClientRepository;
import com.sebworks.oauthly.repository.GrantRepository;
import com.sebworks.oauthly.repository.UserRepository;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.*;

/**
 * <p>This controller implements oauth2 authorization server semantics with password grant type.<br>
 * Supports refresh tokens.<br>
 * Works with request params in token post method for the requirement, it is best to modify it for generic usage.<br>
 * It uses JWT to issue tokens, so tokens are self contained and there is no token store.<br>
 * It works with {@link OAuthFilter} to protect resources.<br>
 * Currently it works with a single user and client id, but it is easy to extend for multiple users.</p>
 *
 * Created by Selim Eren Bekçe on 2016-08-25.
 */
@Controller
@RequestMapping("/oauth")
public class OAuthAuthorizationController implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(OAuthAuthorizationController.class);

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GrantRepository grantRepository;
    @Autowired
    private SessionDataAccessor sessionDataAccessor;
    @Value("${oauth.server.jwt.secret}")
    private String jwtSecret;
    /** In seconds */
    @Value("${oauth.server.expire.access.token}")
    private long expireAccessToken;
    /** In seconds */
    @Value("${oauth.server.expire.refresh.token}")
    private long expireRefreshToken;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    /**
     * Issues new tokens.
     *
     * @param client_id id of the client (required)
     * @param client_secret secret of the client (required)
     * @param grant_type supported grant types: 'client_credentials', 'authorization_code', 'password' or 'refresh_token' (required)
     * @param username the resource owner username when grant_type is 'password'
     * @param password the resource owner password when grant_type is 'password'
     * @param refresh_token the previously retrieved refresh_token when grant_type is 'refresh_token'
     * @param redirect_uri redirect_uri parameter while retrieving authorization code from /authorize, when grant_type is 'authorization_code'
     * @param code retrieved code when grant_type is 'authorization_code'
     * @param scope requested scope when grant_type is 'password'
     * @return token
     */
    @RequestMapping(value = "/token", method = {RequestMethod.POST})
    public @ResponseBody ResponseEntity<Token> token(
            @RequestParam(value = "client_id") String client_id,
            @RequestParam(value = "client_secret") String client_secret,
            @RequestParam(value = "grant_type") String grant_type,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "refresh_token", required = false) String refresh_token,
            @RequestParam(value = "redirect_uri", required = false) String redirect_uri,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "scope", required = false) String scope) {

        Client client = clientRepository.findOne(client_id);
        if(client == null || !Objects.equals(client.getSecret(), client_secret)){
            return ResponseEntity.badRequest().body(new Token("unauthorized"));
        }

        switch (grant_type) {
            case "client_credentials": {
                /* In this mode, we use client_id as user_id in Grant */
                Grant grant = grantRepository.findByClientIdAndUserId(client_id, client_id);
                if(grant == null){
                    grant = new Grant();
                    grant.setClientId(client_id);
                    grant.setUserId(client_id);
                    grant = grantRepository.save(grant);
                }
                Token token = prepareToken(client_id, client_secret, grant.getId(), grant.getScopes());
                return ResponseEntity.ok(token);
            }
            case "authorization_code": {
                Grant grant = grantRepository.findByCode(code);
                if(grant == null){
                    return ResponseEntity.badRequest().body(new Token("invalid code"));
                }
                if(!Objects.equals(grant.getRedirectUri(), redirect_uri)){
                    return ResponseEntity.badRequest().body(new Token("invalid redirect_uri"));
                }
                grant.setCode(null);
                grant.setRedirectUri(null);
                grant = grantRepository.save(grant);
                Token token = prepareToken(client_id, client_secret, grant.getId(), grant.getScopes());
                return ResponseEntity.ok(token);
            }
            case "password": {
                if(!client.isTrusted()){
                    return ResponseEntity.badRequest().body(new Token("client not trusted for password type grant"));
                }
                User user = userRepository.findByUsername(username);
                if(user == null){
                    user = userRepository.findByEmail(username);
                }
                if(user == null || user.checkPassword(password)){
                    return ResponseEntity.badRequest().body(new Token("invalid login"));
                }
                Grant grant = grantRepository.findByClientIdAndUserId(client_id, user.getId());
                if(grant == null){
                    // create new grant automatically because this is a trusted app
                    grant = new Grant();
                    grant.setUserId(user.getId());
                    grant.setClientId(client_id);
                    if(scope != null){
                        grant.setScopes(Arrays.asList(scope.split(" ")));
                    }
                    grant = grantRepository.save(grant);
                }
                Token token = prepareToken(client_id, client_secret, grant.getId(), grant.getScopes());
                return ResponseEntity.ok(token);
            }
            case "refresh_token": {
                Pair<Grant, TokenStatus> tokenStatus = getTokenStatus(refresh_token);
                if (tokenStatus.getValue1() != TokenStatus.VALID_REFRESH) {
                    return ResponseEntity.badRequest().body(new Token("invalid_refresh_token"));
                }
                Grant grant = tokenStatus.getValue0();
                Token token = prepareToken(client_id, client_secret, grant.getId(), grant.getScopes());
                return ResponseEntity.ok(token);
            }
            default:
                return ResponseEntity.badRequest().body(new Token("unsupported_grant_type"));
        }
    }

    @RequestMapping(value = "/authorize", method = RequestMethod.GET)
    public ModelAndView authorize(
            @RequestParam(value = "client_id") String client_id,
            @RequestParam(value = "response_type") String response_type,
            @RequestParam(value = "redirect_uri") String redirect_uri,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "state", required = false) String state) {

        Client client = clientRepository.findOne(client_id);
        if(client == null){
            return new ModelAndView("error", HttpStatus.BAD_REQUEST);
        }
        if(!redirect_uri.startsWith(client.getRedirectUri())){
            return new ModelAndView("error", HttpStatus.BAD_REQUEST);
        }
        if(!"code".equals(response_type)){
            return new ModelAndView("error", HttpStatus.BAD_REQUEST);
        }
        Grant grant = grantRepository.findByClientIdAndUserId(client_id, sessionDataAccessor.access().getUserId());
        if(grant != null){
            boolean scopeOK = true;
            if(scope != null){
                List<String> scopes = Arrays.asList(scope.split(" "));
                for (String s : scopes) {
                    if(!grant.getScopes().contains(s)){
                        scopeOK = false;
                        break;
                    }
                }
            }
            if(scopeOK){
                grant.setCode(UUID.randomUUID().toString().replace("-",""));
                grant.setRedirectUri(redirect_uri);
                grantRepository.save(grant);
                UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(redirect_uri);
                if(state != null){
                    uriComponentsBuilder.queryParam("state", state);
                }
                return new ModelAndView("redirect:"+uriComponentsBuilder.build().toUriString());
            }
        }
        ModelAndView modelAndView = new ModelAndView("authorize");
        modelAndView.addObject("app_name", client.getName());
        return modelAndView;
    }

    @RequestMapping(value = "/authorize", method = RequestMethod.POST)
    public ModelAndView authorizeDo(
            @RequestParam(value = "client_id") String client_id,
            @RequestParam(value = "response_type") String response_type,
            @RequestParam(value = "redirect_uri") String redirect_uri,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "state", required = false) String state) {

        Client client = clientRepository.findOne(client_id);
        if(client == null){
            return new ModelAndView("error", HttpStatus.BAD_REQUEST);
        }
        if(!redirect_uri.startsWith(client.getRedirectUri())){
            return new ModelAndView("error", HttpStatus.BAD_REQUEST);
        }
        if(!"code".equals(response_type)){
            return new ModelAndView("error", HttpStatus.BAD_REQUEST);
        }
        Grant grant = grantRepository.findByClientIdAndUserId(client_id, sessionDataAccessor.access().getUserId());
        if(grant == null){
            grant = new Grant();
            grant.setUserId(sessionDataAccessor.access().getUserId());
            grant.setClientId(client_id);
        }
        if(scope != null){
            List<String> scopes = Arrays.asList(scope.split(" "));
            grant.getScopes().addAll(scopes);
        }
        grant.setCode(UUID.randomUUID().toString().replace("-",""));
        grant.setRedirectUri(redirect_uri);
        grantRepository.save(grant);
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(redirect_uri);
        if(state != null){
            uriComponentsBuilder.queryParam("state", state);
        }
        return new ModelAndView("redirect:"+uriComponentsBuilder.build().toUriString());
    }

//    public static void main(String[] args) {
//        String redirect_uri = "http://localhost:8080/return#!tablefortwo";
//        String state = "dsfsfsd";
//        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(redirect_uri);
//        if(state != null){
//            uriComponentsBuilder.fragment("a21321321");
//            uriComponentsBuilder.queryParam("state", state);
//        }
//        System.out.println(uriComponentsBuilder.build().toUriString());;
//
//    }

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
    private Token prepareToken(String client_id, String client_secret, String grant_id, List<String> scopes) {
        int hash = Objects.hash(client_id, client_secret);
        final long iat = System.currentTimeMillis() / 1000L; // issued at claim
        final long exp = iat + expireAccessToken; // expires claim. In this case the token expires in 60 seconds
        final JWTSigner signer = new JWTSigner(jwtSecret);

        final HashMap<String, Object> claims = new HashMap<>();
        claims.put("vt", 1); //version=1 & type=access
        claims.put("exp", exp);
        claims.put("h", hash);
        claims.put("grant", grant_id);
        final String token_a = signer.sign(claims);

        final HashMap<String, Object> claims_r = new HashMap<>();
        final long exp_r = iat + expireRefreshToken; // refresh token expire time: 1 week
        claims_r.put("vt", 2); //version=1 & type=refresh
        claims_r.put("exp", exp_r);
        claims_r.put("h", hash);
        claims_r.put("grant", grant_id);
        final String token_r = signer.sign(claims_r);

        /* The last parameter (scope) is entirely optional. You can use it to implement scoping requirements. If you would like so, put it to claims map to verify it. */
        return new Token(token_a, token_r, "bearer", expireAccessToken, scopes == null ? "" : String.join(" ", scopes));
    }

    /**
     * Validate given token and return its type
     * @see TokenStatus
     */
    public Pair<Grant, TokenStatus> getTokenStatus(String access_token){
        if(access_token == null)
            return new Pair<>(null, TokenStatus.INVALID);
        try {
            final JWTVerifier verifier = new JWTVerifier(jwtSecret);
            final Map<String,Object> claims = verifier.verify(access_token);
            // first check version
            int type = (int) claims.get("vt");
            if(type != 1 && type != 2){
                return new Pair<>(null, TokenStatus.INVALID);
            }
            // check expiry date
            int exp = (int) claims.get("exp");
            if(exp < (System.currentTimeMillis() / 1000L))
                return new Pair<>(null, TokenStatus.INVALID);
            // check grant
            String grant_id = (String) claims.get("grant");
            Grant grant = grantRepository.findOne(grant_id);
            if(grant == null){
                return new Pair<>(null, TokenStatus.INVALID);
            }
            Client client = clientRepository.findOne(grant.getClientId());
            if(client == null){
                return new Pair<>(null, TokenStatus.INVALID); // how can this be?
            }
            // check hash value
            int receivedHash = (int) claims.get("h");
            int correctHash = Objects.hash(client.getId(), client.getSecret());
            if(receivedHash != correctHash) {
                return new Pair<>(null, TokenStatus.INVALID);
            }
            // check token type & version
            if(type == 1){
                return new Pair<>(grant, TokenStatus.VALID_ACCESS);
            } else {
                return new Pair<>(grant, TokenStatus.VALID_REFRESH);
            }
        } catch (JWTVerifyException | SignatureException | InvalidKeyException | NullPointerException e) {
            return new Pair<>(null, TokenStatus.INVALID);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new Pair<>(null, TokenStatus.INVALID);
        }
    }

}
