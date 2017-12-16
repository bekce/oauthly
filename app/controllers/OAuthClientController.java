package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import config.AuthorizationServerManager;
import dtos.OAuthContext;
import dtos.OAuthProvider;
import dtos.Utils;
import play.Logger;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;

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
		Logger.info(context.toString());
		// TODO call /me endpoints, identify the user and return to next
		return context.retrieveToken()
				.thenComposeAsync(token -> ws.url(provider.getUserInfoUrl()).addHeader("Authorization", "Bearer "+token.getAccessToken()).get(), httpExecutionContext.current())
				.thenApplyAsync(wsResponse -> {
					JsonNode node = wsResponse.asJson();
					String id = node.get("id").asText();
					String name = node.get("name").textValue();
					String email = node.get("email") == null ? null : node.get("email").textValue();
					Logger.info("body="+wsResponse.getBody());
					flash("info", String.format("Received: id=%s, name=%s, email=%s", id, name, email));
					if(true){
						return redirect(routes.LoginController.get(next.orElse(null)));
					}

					if(next.isPresent() && next.get().matches("^/.*$"))
						return redirect(next.get());
					else
						return redirect(routes.ProfileController.get());

//					if(next != null && next.matches("^/.*$"))
//						return redirect(next);
//					else
//						return redirect(routes.ProfileController.get());
				}, httpExecutionContext.current());
	}

}
