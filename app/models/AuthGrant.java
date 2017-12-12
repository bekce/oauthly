package models;

import org.jongo.marshall.jackson.oid.MongoId;

import java.util.ArrayList;
import java.util.List;

/**
 * Models a grant made by user to client
 * Created by Selim Eren Bekçe on 15.08.2017.
 */
public class AuthGrant {
    @MongoId
    private String id;
    private String userId;
    private String clientId;
    private List<String> scopes = new ArrayList<>();

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

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }
}
