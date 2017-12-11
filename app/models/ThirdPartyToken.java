package models;

import dtos.Token;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;
//import org.mongodb.morphia.annotations.Entity;
//import org.mongodb.morphia.annotations.Id;
//import org.mongodb.morphia.annotations.Indexed;

/**
 * Models a third party token, such as from facebook
 * Created by Selim Eren Bekçe on 15.08.2017.
 */
//@Entity
public class ThirdPartyToken {
//    @Id
    @MongoObjectId
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
     * retrieved token
     */
    private Token token;

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
}
