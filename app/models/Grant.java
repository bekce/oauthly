package models;

import org.jongo.marshall.jackson.oid.MongoId;

import java.util.HashSet;
import java.util.Set;

/**
 * Models a grant made by user to client
 * Created by Selim Eren Bekçe on 15.08.2017.
 */
public class Grant {
    @MongoId
    private String id;
    private String userId;
    private String clientId;
    private Set<String> scopes = new HashSet<>();

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

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }
}
