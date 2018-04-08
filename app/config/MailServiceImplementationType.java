package config;

public enum MailServiceImplementationType {

    mailer("config.MailerService"),
    mailgun("config.MailgunService");

    private String clazz;

    MailServiceImplementationType(String clazz) {
        this.clazz = clazz;
    }

    public String getClazz() {
        return clazz;
    }

    public MailServiceImplementationType setClazz(String clazz) {
        this.clazz = clazz;
        return this;
    }
}
