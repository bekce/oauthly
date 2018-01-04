package controllers;

import config.AuthorizationServerManager;
import config.JwtUtils;
import config.Utils;
import dtos.OAuthContext;
import dtos.OAuthProvider;
import models.ProviderLink;
import models.User;
import org.apache.commons.codec.binary.Base64;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import repositories.ProviderLinkRepository;
import repositories.UserRepository;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class OAuthClientController extends Controller {

	@Inject
	private AuthorizationServerManager manager;
	@Inject
	private WSClient ws;
	@Inject
	private HttpExecutionContext httpExecutionContext;
	@Inject
	private JwtUtils jwtUtils;
	@Inject
	private UserRepository userRepository;
	@Inject
	private ProviderLinkRepository providerLinkRepository;

	public Result authorize(String providerKey, String next) {
		OAuthProvider provider = manager.getProvider(providerKey);
		if(provider == null) {
			return notFound("N/A");
		}
		OAuthContext context = new OAuthContext(provider, ws);
		String state = Base64.encodeBase64String(String.format("%s,%s", Utils.newId(), next == null ? "" : next).getBytes(StandardCharsets.UTF_8));
		context.setState(state);
		context.setRedirectUri(routes.OAuthClientController.callback(providerKey, Optional.empty(), Optional.empty(), Optional.empty()).absoluteURL(request()));
		flash("state", state);
		return redirect(context.prepareAuthorizeUrl());
	}
	
	@config.AuthorizationServerSecure(optional = true)
	public CompletionStage<Result> callback(String providerKey, Optional<String> code, Optional<String> error, Optional<String> state) {
		OAuthProvider provider = manager.getProvider(providerKey);
		if(provider == null) {
			return CompletableFuture.completedFuture(notFound("N/A"));
		}
		if(!state.isPresent() || !state.get().equals(flash("state"))){
			flash("error", "Request failed (states don't match), please enable cookies and try again");
			return CompletableFuture.completedFuture(redirect(routes.LoginController.get(null)));
		}
		String decodedState = new String(Base64.decodeBase64(state.get()), StandardCharsets.UTF_8);
		String next = org.apache.commons.lang3.StringUtils.trimToNull(decodedState.split(",", -1)[1]);
		if(error.isPresent()){
			flash("error", provider.getDisplayName() + " returned "+error.get());
			return CompletableFuture.completedFuture(redirect(routes.LoginController.get(next)));
		}
		if(!code.isPresent()){
			return CompletableFuture.completedFuture(badRequest("no code parameter"));
		}
		Optional<User> authenticatedUser = request().attrs().getOptional(config.AuthorizationServerSecure.USER);
		play.Logger.info("authenticatedUser.isPresent():{}", authenticatedUser.isPresent());

		OAuthContext context = new OAuthContext(provider, ws);
		context.setState(state.get());
		context.setRedirectUri(routes.OAuthClientController.callback(providerKey, Optional.empty(), Optional.empty(), Optional.empty()).absoluteURL(request()));
		context.setCode(code.get());

		return context.retrieveToken()
				.thenApplyAsync(token -> context)
				.thenComposeAsync(provider.getCurrentUserIdentifier())
				.thenApplyAsync(dto -> {
					ProviderLink link = providerLinkRepository.findByProvider(providerKey, dto.getId());
					User user = null;
					if(link == null) {
						link = new ProviderLink();
						link.setId(Utils.newId());
						link.setProviderKey(providerKey);
						link.setRemoteUserId(dto.getId());
						link.setRemoteUserName(dto.getName());
						link.setRemoteUserEmail(Utils.normalizeEmail(dto.getEmail()));
						link.setToken(context.getToken());
						providerLinkRepository.save(link);
					} else if(link.getUserId() != null) {
						user = userRepository.findById(link.getUserId());
					}
					if(user == null) { // need to register or link
						if(authenticatedUser.isPresent()){ // already authenticated, link it
							return redirect(routes.ProfileController.linkProvider(link.getId()));
						} else { // not authenticated, register
							return redirect(routes.RegisterController.step2(next, link.getId()));
						}
					} else { // we have a valid user here!
						if(!user.getId().equals(authenticatedUser.map(User::getId).orElse(null))) {
							// the linked account is connected to another account, we cannot allow this
							flash("warning", "Warning: The "+providerKey+" account you tried to link is already linked to another account with email address "+user.getEmail()+". To proceed, you need to unlink it first");
							return redirect(routes.ProfileController.get());
						}
						return jwtUtils.prepareCookieThenRedirect(user, next);
					}
				}, httpExecutionContext.current());
	}

}
