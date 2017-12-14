package controllers;

import config.AuthorizationServerSecure;
import config.JwtUtils;
import dtos.Token;
import dtos.TokenStatus;
import dtos.Utils;
import models.Client;
import models.Grant;
import models.User;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Result;
import repositories.ClientRepository;
import repositories.GrantRepository;
import repositories.UserRepository;
import scala.Tuple2;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * <p>This controller implements oauth2 authorization server semantics with password grant type.<br>
 * Supports refresh tokens.<br>
 * Works with request params in token post method for the requirement, it is best to modify it for generic usage.<br>
 * It uses JWT to issue tokens, so tokens are self contained and there is no token store.<br>
 * It works with ResourceServerFilter to protect resources.<br>
 * Currently it works with a single user and client id, but it is easy to extend for multiple users.</p>
 *
 * Created by Selim Eren Bekçe on 2016-08-25.
 */
public class OAuthController extends play.mvc.Controller {
    private final FormFactory formFactory;
    private final JwtUtils jwtUtils;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final GrantRepository grantRepository;

    @Inject
    public OAuthController(FormFactory formFactory, JwtUtils jwtUtils, ClientRepository clientRepository, UserRepository userRepository, GrantRepository grantRepository) {
        this.formFactory = formFactory;
        this.jwtUtils = jwtUtils;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.grantRepository = grantRepository;
    }

    /**
     * Issues new tokens.
     *
     * client_id id of the client (required)
     * client_secret secret of the client (required)
     * grant_type supported grant types: 'client_credentials', 'authorization_code', 'password' or 'refresh_token' (required)
     * username the resource owner username when grant_type is 'password'
     * password the resource owner password when grant_type is 'password'
     * refresh_token the previously retrieved refresh_token when grant_type is 'refresh_token'
     * redirect_uri redirect_uri parameter while retrieving authorization code from /authorize, when grant_type is 'authorization_code'
     * code retrieved code when grant_type is 'authorization_code'
     * scope requested scope when grant_type is 'password'
     * @return token
     */
    public Result token() {
        DynamicForm form = formFactory.form().bindFromRequest();
        String grant_type = form.get("grant_type");
        if(grant_type == null){
            return badRequest(Json.newObject().put("message", "missing grant_type"));
        }
        String client_id = form.get("client_id");
        String client_secret = form.get("client_secret");
        Client client = clientRepository.findById(client_id);
        if(client == null || !Objects.equals(client.getSecret(), client_secret)){
            return badRequest(Json.newObject().put("message", "either client_id or client_secret is missing or invalid"));
        }
        String username = form.get("username");
        String password = form.get("password");
        String refresh_token = form.get("refresh_token");
        String redirect_uri = form.get("redirect_uri");
        String code = form.get("code");
        String scope = form.get("scope");

        switch (grant_type) {
            case "client_credentials": {
                /* In this mode, we use client_id as user_id in Grant */
                Grant grant = grantRepository.findByClientAndUser(client_id, client_id);
                if(grant == null){
                    grant = new Grant();
                    grant.setId(Utils.newId());
                    grant.setClientId(client_id);
                    grant.setUserId(client_id);
                    grantRepository.save(grant);
                }
                Token token = jwtUtils.prepareToken(client_id, client_secret, grant.getId(), grant.getScopes());
                return ok(Json.toJson(token));
            }
            case "authorization_code": {
                Grant grant = jwtUtils.validateCode(code, redirect_uri);
                if(grant == null){
                    return badRequest(Json.newObject().put("message", "invalid code or redirect_uri"));
                }
                Token token = jwtUtils.prepareToken(client_id, client_secret, grant.getId(), grant.getScopes());
                return ok(Json.toJson(token));
            }
            case "password": {
                if(!client.isTrusted()){
                    return badRequest(Json.newObject().put("message", "client not trusted for password type grant"));
                }
                User user = userRepository.findByUsernameNormalized(Utils.normalizeUsername(username));
                if(user == null){
                    user = userRepository.findByEmail(username);
                }
                if(user == null || !user.checkPassword(password)){
                    return badRequest(Json.newObject().put("message", "invalid username or password"));
                }
                Grant grant = grantRepository.findByClientAndUser(client_id, user.getId());
                if(grant == null){
                    // create new grant automatically because this is a trusted app
                    grant = new Grant();
                    grant.setId(Utils.newId());
                    grant.setUserId(user.getId());
                    grant.setClientId(client_id);
                    if(scope != null){
                        grant.setScopes(Arrays.asList(scope.split(" ")));
                    }
                    grantRepository.save(grant);
                }
                Token token = jwtUtils.prepareToken(client_id, client_secret, grant.getId(), grant.getScopes());
                return ok(Json.toJson(token));
            }
            case "refresh_token": {

                Tuple2<Grant, TokenStatus> tokenStatus = jwtUtils.getTokenStatus(refresh_token);
                if (tokenStatus._2() != TokenStatus.VALID_REFRESH) {
                    return badRequest(Json.newObject().put("message", "invalid_refresh_token"));
                }
                Grant grant = tokenStatus._1();
                Token token = jwtUtils.prepareToken(client_id, client_secret, grant.getId(), grant.getScopes());
                return ok(Json.toJson(token));
            }
            default:
                return badRequest(Json.newObject().put("message", "unsupported_grant_type"));
        }
    }

    @AuthorizationServerSecure
    public Result authorize(String client_id, String response_type, String redirect_uri, String scope, String state) {

        Client client = clientRepository.findById(client_id);
        if(client == null){
            return badRequest(Json.newObject().put("message", "invalid client_id"));
        }
        if(!redirect_uri.startsWith(client.getRedirectUri())){
            return badRequest(Json.newObject().put("message", "invalid redirect_uri"));
        }
        if(!"code".equals(response_type)){
            return badRequest(Json.newObject().put("message", "invalid response_type"));
        }

        User user = request().attrs().get(AuthorizationServerSecure.USER);

        Grant grant = grantRepository.findByClientAndUser(client_id, user.getId());
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
                String code = jwtUtils.prepareCode(client.getId(), client.getSecret(), grant.getId(), redirect_uri);
                String uri = String.format("%s?code=%s", redirect_uri, code);
                if(state != null){
                    uri += "&state="+state;
                }
                return redirect(uri);
            }
        }

        return ok(views.html.authorize.render(client.getName(), client_id, response_type, redirect_uri, scope, state));
    }

    @AuthorizationServerSecure
    public Result authorizeDo(String client_id, String response_type, String redirect_uri, String scope, String state) {

        Client client = clientRepository.findById(client_id);
        if(client == null){
            return badRequest(Json.newObject().put("message", "invalid client_id"));
        }
        if(!redirect_uri.startsWith(client.getRedirectUri())){
            return badRequest(Json.newObject().put("message", "invalid redirect_uri"));
        }
        if(!"code".equals(response_type)){
            return badRequest(Json.newObject().put("message", "invalid response_type"));
        }

        User user = request().attrs().get(AuthorizationServerSecure.USER);

        Grant grant = grantRepository.findByClientAndUser(client_id, user.getId());
        if(grant == null){
            grant = new Grant();
            grant.setId(Utils.newId());
            grant.setUserId(user.getId());
            grant.setClientId(client_id);
        }
        if(scope != null){
            List<String> scopes = Arrays.asList(scope.split(" "));
            grant.getScopes().addAll(scopes);
        }
        grantRepository.save(grant);

        String code = jwtUtils.prepareCode(client.getId(), client.getSecret(), grant.getId(), redirect_uri);
        String uri = String.format("%s?code=%s", redirect_uri, code);
        if(state != null){
            uri += "&state="+state;
        }
        return redirect(uri);
    }

}
