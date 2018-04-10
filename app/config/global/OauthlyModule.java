package config.global;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import config.MailService;
import config.MailServiceImplementationType;
import play.Environment;
import play.libs.akka.AkkaGuiceSupport;

import java.util.Optional;

public class OauthlyModule extends AbstractModule implements AkkaGuiceSupport {

    private final Environment environment;
    private final Config config;

    @Inject
    public OauthlyModule(Environment environment, Config config) {
        this.environment = environment;
        this.config = config;
    }

    @Override
    protected void configure() {
        bindMailService();
    }

    private void bindMailService() {
        final String mailServiceImplementationConf = Optional
                .ofNullable(config.getString("mail.service.implementation"))
                .orElse("mailer");

        try {
            MailServiceImplementationType type =
                    Enum.valueOf(MailServiceImplementationType.class, mailServiceImplementationConf);

            Class<? extends MailService> bindingClass = environment
                    .classLoader()
                    .loadClass(type.getClazz())
                    .asSubclass(MailService.class);

            bind(MailService.class)
                    .to(bindingClass)
                    .asEagerSingleton();

        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
}
