package models;

import dtos.Token;
import org.jongo.marshall.jackson.oid.MongoId;

/**
 * Models a third party token, such as from facebook
 * Created by Selim Eren Bekçe on 15.08.2017.
 */
public class ThirdPartyConnection {
    @MongoId
    private String id;
    /**
     * belonging user
     */
//    @Indexed
    private String userId;
    /**
     * e.g. 'facebook' or 'twitter'
     */
    private String party;
    /**
     * last retrieved token
     */
    private Token token;
    /**
     * User id on the remote party (e.g. user id on facebook)
     */
//    @Indexed
    private String remoteUserId;

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

    public String getParty() {
        return party;
    }

    public void setParty(String party) {
        this.party = party;
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
}
