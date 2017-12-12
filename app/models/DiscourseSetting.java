package models;

import java.util.Objects;

public class DiscourseSetting extends Setting {
    private boolean enabled;
    private String redirectUri;
    private String secret;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscourseSetting that = (DiscourseSetting) o;
        return enabled == that.enabled &&
                Objects.equals(id, that.id) &&
                Objects.equals(redirectUri, that.redirectUri) &&
                Objects.equals(secret, that.secret);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, enabled, redirectUri, secret);
    }

    @Override
    public String toString() {
        return "DiscourseSetting{" +
                "id='" + id + '\'' +
                ", enabled=" + enabled +
                ", redirectUri='" + redirectUri + '\'' +
                ", secret='" + secret + '\'' +
                '}';
    }
}
