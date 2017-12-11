package config;

import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class RecaptchaAction extends play.mvc.Action.Simple {

    private final RecaptchaUtil recaptchaUtil;

    @Inject
    public RecaptchaAction(RecaptchaUtil recaptchaUtil) {
        this.recaptchaUtil = recaptchaUtil;
    }

    @Override
    public CompletionStage<Result> call(Http.Context ctx) {
        String clientIp = ctx._requestHeader().asJava().remoteAddress();
        String recaptchaResponse = null;
        try {
            recaptchaResponse = ctx.request().body().asFormUrlEncoded().get("g-recaptcha-response")[0];
        } catch (Exception ignored){}
        if(recaptchaResponse == null) {
            try {
                recaptchaResponse = ctx.request().body().asMultipartFormData().asFormUrlEncoded().get("g-recaptcha-response")[0];
            } catch (Exception ignored){}
        }
        return recaptchaUtil.check(clientIp, recaptchaResponse).thenComposeAsync(passed -> {
            if(passed) {
                return delegate.call(ctx);
            } else {
                ctx.flash().put("error", "Your request did not succeed - captcha required");
                return CompletableFuture.completedFuture(redirect(ctx._requestHeader().asJava().path()));
            }
        });
    }
}
