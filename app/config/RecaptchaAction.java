package config;

import play.Logger;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

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
            Logger.info("Found g-recaptcha-response by FormUrlEncoded");
            ctx.request().body().asFormUrlEncoded().remove("g-recaptcha-response");
        } catch (Exception ignored){}
        if(recaptchaResponse == null) {
            try {
                recaptchaResponse = ctx.request().body().asMultipartFormData().asFormUrlEncoded().get("g-recaptcha-response")[0];
                Logger.info("Found g-recaptcha-response by MultipartFormData");
                ctx.request().body().asMultipartFormData().asFormUrlEncoded().remove("g-recaptcha-response");
            } catch (Exception ignored){}
        }
        boolean passed = false;
        try {
            passed = recaptchaUtil.check(clientIp, recaptchaResponse).toCompletableFuture().get(); //.thenCompose(passed -> { // async composing does not run due to a Play bug, try in later versions
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if(passed) {
            Logger.info("Recaptcha passes");
            return delegate.call(ctx);
        } else {
            Logger.info("Recaptcha didn't pass");
            ctx.flash().put("error", "Your request did not succeed - captcha required");
            return CompletableFuture.completedFuture(redirect(ctx._requestHeader().asJava().path()));
        }
    }
}
