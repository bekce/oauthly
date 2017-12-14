package models;

import org.jongo.marshall.jackson.oid.MongoId;

/**
 * Generic way of storing various settings
 */
public abstract class Setting {
    @MongoId
    protected String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
