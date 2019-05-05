package config;

import akka.stream.javadsl.Source;
import com.typesafe.config.Config;
import play.libs.ws.WSAuthScheme;
import play.libs.ws.WSClient;
import play.mvc.Http.MultipartFormData.DataPart;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Selim Eren Bekçe on 6.09.2017.
 * <p>
 * TODO Use circuit breaker or akka for retries https://github.com/jhalterman/failsafe#circuit-breakers
 */
@Singleton
public class MailgunService implements MailService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MailgunService.class);

    private final String mailgunKey;
    private final String mailgunFrom;
    private final String mailgunDomain;
    private final WSClient ws;

    @Inject
    public MailgunService(Config config, WSClient ws) {
        this.mailgunKey = config.getString("mail.mailgun.key");
        this.mailgunFrom = config.getString("mail.mailgun.from");
        this.mailgunDomain = config.getString("mail.mailgun.domain");
        this.ws = ws;
    }

    @Override
    public CompletableFuture<String> sendEmail(String to, String subject, String content) {
        return ws.url(String.format("https://api.mailgun.net/v3/%s/messages", mailgunDomain))
                .setAuth("api", mailgunKey, WSAuthScheme.BASIC)
                .post(Source.from(Arrays.asList(
                        new DataPart("from", mailgunFrom),
                        new DataPart("to", to),
                        new DataPart("subject", subject),
                        new DataPart("html", content))))
                .handleAsync((response, throwable) -> {
                    if (throwable != null) {
                        log.error("sendEmail: Exception", throwable);
                    } else if (response.getStatus() != 200) {
                        log.error("sendEmail: Non-200 response, status={}, body={}", response.getStatus(), response.getBody());
                    } else {
                        log.error("sendEmail: OK, status={}, body={}", response.getStatus(), response.getBody());
                        return response.asJson().get("id").textValue();
                    }
                    return null;
                }).toCompletableFuture();
    }
}
