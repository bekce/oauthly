package config;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;

import com.typesafe.config.Config;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;

@Singleton
public class MailerService implements MailService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MailerService.class);

    private final MailerClient mailerClient;
    private final String mailFrom;

    @Inject
    public MailerService(Config config, MailerClient mailerClient) {
        this.mailerClient = mailerClient;
        this.mailFrom = config.getString("mail.mailer.from");
    }

    @Override
    public CompletableFuture<String> sendEmail(String to, String subject, String content) {
        Email email = new Email()
                .setSubject(subject)
                .setFrom(this.mailFrom)
                .addTo(to)
                .setBodyHtml(content);

        mailerClient.send(email);

        log.info("An email with subject " + subject + " was sent successfully to: " + to);

        return CompletableFuture.completedFuture("Mail was sent successfully.");
    }
}
