package models;

import dtos.Token;
import org.jongo.marshall.jackson.oid.MongoId;

/**
 * Models an authorization from an oauth provider (facebook, twitter, etc)
 * to one of oauthly users.
 * Created by Selim Eren Bekçe on 15.08.2017.
 */
public class ProviderLink {
    @MongoId
    private String id;
    /**
     * belonging user, could be null while setting up
     */
//    @Indexed
    private String userId;
    /**
     * e.g. 'facebook' or 'twitter'
     */
    private String providerKey;
    /**
     * last retrieved token
     */
    private Token token;
    /**
     * External user id (e.g. on facebook)
     */
//    @Indexed
    private String remoteUserId;
    private String remoteUserEmail;
    private String remoteUserName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProviderKey() {
        return providerKey;
    }

    public void setProviderKey(String providerKey) {
        this.providerKey = providerKey;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public String getRemoteUserId() {
        return remoteUserId;
    }

    public void setRemoteUserId(String remoteUserId) {
        this.remoteUserId = remoteUserId;
    }

    public String getRemoteUserEmail() {
        return remoteUserEmail;
    }

    public void setRemoteUserEmail(String remoteUserEmail) {
        this.remoteUserEmail = remoteUserEmail;
    }

    public String getRemoteUserName() {
        return remoteUserName;
    }

    public void setRemoteUserName(String remoteUserName) {
        this.remoteUserName = remoteUserName;
    }
}
