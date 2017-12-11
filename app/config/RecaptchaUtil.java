package config;

import com.typesafe.config.Config;
import play.libs.ws.WSClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletionStage;

@Singleton
public class RecaptchaUtil {

    private final WSClient ws;
    private final String recaptchaSecret;

    @Inject
    public RecaptchaUtil(Config config, WSClient ws) {
        recaptchaSecret = config.getString("recaptcha.secret");
        this.ws = ws;
    }

    public CompletionStage<Boolean> check(String clientIpAddress, String response){
        return ws.url("https://www.google.com/recaptcha/api/siteverify")
                .setContentType("application/x-www-form-urlencoded")
                .post(String.format("response=%s&secret=%s&remoteip=%s", response, recaptchaSecret, clientIpAddress))
                .thenApplyAsync(res -> res.getStatus() == 200 && res.asJson().get("success").asBoolean());
    }

}
