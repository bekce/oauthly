package controllers;

import config.AuthorizationServerManager;
import config.JwtUtils;
import dtos.OAuthContext;
import dtos.OAuthProvider;
import dtos.Utils;
import models.ProviderLink;
import models.User;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Http;
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
		context.setRedirectUri(routes.OAuthClientController.callback(providerKey, Optional.ofNullable(next), Optional.empty(), Optional.empty()).absoluteURL(request()));
		flash("state", context.getState());
		return redirect(context.prepareAuthorizeUrl());
	}

	public CompletionStage<Result> callback(String providerKey, Optional<String> next, Optional<String> code, Optional<String> state) {
		OAuthProvider provider = manager.getProvider(providerKey);
		if(provider == null) {
			return CompletableFuture.completedFuture(notFound("N/A"));
		}
		if(!code.isPresent()){
			return CompletableFuture.completedFuture(badRequest("no code param"));
		}
		if(!state.isPresent() || !state.get().equals(flash("state"))){
			flash("error", "Request failed (states don't match), please enable cookies and try again");
			return CompletableFuture.completedFuture(redirect(routes.LoginController.get(next.orElse(null))));
		}

		OAuthContext context = new OAuthContext(provider, ws);
		context.setState(state.get());
		context.setRedirectUri(routes.OAuthClientController.callback(providerKey, next, Optional.empty(), Optional.empty()).absoluteURL(request()));
		context.setCode(code.orElse(null));

		return context.retrieveToken()
				.thenApplyAsync(token -> context)
				.thenComposeAsync(provider.getCurrentUserIdentifier())
				.thenApplyAsync(dto -> {
					ProviderLink link = providerLinkRepository.findByProvider(providerKey, dto.getId());
					User user = null;
					if(link == null || link.getUserId() == null) {
						link = new ProviderLink();
						link.setId(Utils.newId());
						link.setProviderKey(providerKey);
						link.setRemoteUserId(dto.getId());
						link.setToken(context.getToken());
						providerLinkRepository.save(link);
					} else {
						user = userRepository.findById(link.getUserId());
					}
					if(user == null) { // TODO continue to final step of registration
						return redirect(routes.RegisterController.step1(next.orElse(null)));
					} else { // we have a valid user here!
						String cookieValue = jwtUtils.prepareCookie(user);
						Http.Cookie ltat = Http.Cookie.builder("ltat", cookieValue).withPath("/").withHttpOnly(true).withMaxAge(jwtUtils.getExpireCookie()).build();
//						flash("info", "Login successful");
//						flash("info", String.format("Received: %s", dto));
						if(next.isPresent() && next.get().matches("^/.*$"))
							return redirect(next.get()).withCookies(ltat);
						else
							return redirect(routes.ProfileController.get()).withCookies(ltat);
					}
				}, httpExecutionContext.current());
	}

}
