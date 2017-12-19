package controllers;

import config.AuthorizationServerManager;
import config.JwtUtils;
import dtos.OAuthContext;
import dtos.OAuthProvider;
import config.Utils;
import models.ProviderLink;
import models.User;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import repositories.ProviderLinkRepository;
import repositories.UserRepository;

import javax.inject.Inject;
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
		context.setState(Utils.newId());
		context.setRedirectUri(routes.OAuthClientController.callback(providerKey, Optional.ofNullable(next), Optional.empty(), Optional.empty(), Optional.empty()).absoluteURL(request()));
		flash("state", context.getState());
		return redirect(context.prepareAuthorizeUrl());
	}

	public CompletionStage<Result> callback(String providerKey, Optional<String> next, Optional<String> code, Optional<String> error, Optional<String> state) {
		OAuthProvider provider = manager.getProvider(providerKey);
		if(provider == null) {
			return CompletableFuture.completedFuture(notFound("N/A"));
		}
		if(error.isPresent()){
			flash("error", provider.getDisplayName() + " returned "+error.get());
			return CompletableFuture.completedFuture(redirect(routes.LoginController.get(next.orElse(null))));
		}
		if(!code.isPresent()){
			return CompletableFuture.completedFuture(badRequest("no code parameter"));
		}
		if(!state.isPresent() || !state.get().equals(flash("state"))){
			flash("error", "Request failed (states don't match), please enable cookies and try again");
			return CompletableFuture.completedFuture(redirect(routes.LoginController.get(next.orElse(null))));
		}

		OAuthContext context = new OAuthContext(provider, ws);
		context.setState(state.get());
		context.setRedirectUri(routes.OAuthClientController.callback(providerKey, next, Optional.empty(), Optional.empty(), Optional.empty()).absoluteURL(request()));
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
					if(user == null) { // need to register
						return redirect(routes.RegisterController.step2(next.orElse(null), link.getId()));
					} else { // we have a valid user here!
//						flash("info", "Login successful");
//						flash("info", String.format("Received: %s", dto));
						return jwtUtils.prepareCookieThenRedirect(user, next.orElse(null));
					}
				}, httpExecutionContext.current());
	}

}
