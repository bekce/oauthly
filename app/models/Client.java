package models;

//import org.mongodb.morphia.annotations.Entity;
//import org.mongodb.morphia.annotations.Id;

/**
 * Client, a.k.a. App
 * Created by Selim Eren Bekçe on 15.08.2017.
 */
//@Entity
public class Client {
//    @Id
    private String id;
    private String secret;
    private String name;
    private String logoUrl;
    /**
     * If true, then all grants shall automatically be given without user consent
     */
    private boolean trusted;
    /**
     * Origin of redirect uri for each request shall match this.
     * e.g. 'http://localhost:8080'
     */
    private String redirectUri;
    /**
     * The id of the managing user of this client
     */
    private String ownerId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public boolean isTrusted() {
        return trusted;
    }

    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
