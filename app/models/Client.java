package models;

import org.jongo.marshall.jackson.oid.MongoId;

/**
 * Client, a.k.a. App
 * Created by Selim Eren Bekçe on 15.08.2017.
 */
public class Client {
    @MongoId
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
    /**
     * Contains the allowed origin for using the JS authentication. Must match incoming Origin header 1-1 else the request will not be allowed.
     */
    private String allowedOrigin;

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

    public String getAllowedOrigin() {
        return allowedOrigin;
    }

    public void setAllowedOrigin(String allowedOrigin) {
        this.allowedOrigin = allowedOrigin;
    }
}
