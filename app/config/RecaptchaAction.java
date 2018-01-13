package config;

import play.Logger;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class RecaptchaAction extends play.mvc.Action<RecaptchaProtected> {

    private final RecaptchaUtil recaptchaUtil;
    private final HttpExecutionContext httpExecutionContext;

    @Inject
    public RecaptchaAction(RecaptchaUtil recaptchaUtil, HttpExecutionContext httpExecutionContext) {
        this.recaptchaUtil = recaptchaUtil;
        this.httpExecutionContext = httpExecutionContext;
    }

    @Override
    public CompletionStage<Result> call(Http.Context ctx) {
        String clientIp = ctx._requestHeader().asJava().remoteAddress();
        String recaptchaResponse = null;
        try {
            recaptchaResponse = ctx.request().body().asFormUrlEncoded().get("g-recaptcha-response")[0];
            Logger.debug("Found g-recaptcha-response by FormUrlEncoded");
            ctx.request().body().asFormUrlEncoded().remove("g-recaptcha-response");
        } catch (Exception ignored){}
        if(recaptchaResponse == null) {
            try {
                recaptchaResponse = ctx.request().body().asMultipartFormData().asFormUrlEncoded().get("g-recaptcha-response")[0];
                Logger.debug("Found g-recaptcha-response by MultipartFormData");
                ctx.request().body().asMultipartFormData().asFormUrlEncoded().remove("g-recaptcha-response");
            } catch (Exception ignored){}
        }
        return recaptchaUtil.check(clientIp, recaptchaResponse).thenComposeAsync(passed -> {
            if(passed) {
                Logger.debug("Recaptcha passes");
                return delegate.call(ctx);
            } else {
                Logger.debug("Recaptcha didn't pass");
                ctx.flash().put("error", "Your request did not succeed - captcha required");
                return CompletableFuture.completedFuture(redirect(
                        configuration.fallback().isEmpty() ? ctx._requestHeader().asJava().path() : configuration.fallback()
                ));
            }
        }, httpExecutionContext.current());
    }
}
