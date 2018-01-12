package models;

import org.jongo.marshall.jackson.oid.MongoId;

/**
 * Created by Selim Eren Bekçe on 7.01.2018.
 */
public class Event {

    @MongoId
    private String id;
    private long timestamp = System.currentTimeMillis();
    private EventType eventType;
    private String userId;
    private String ipAddress;
    private String userAgent;
    private Object oldValue;
    private Object newValue;

    public Event() {
    }

    public Event(String userId, Object oldValue, Object newValue, EventType eventType) {
        this();
        this.userId = userId;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.eventType = eventType;
    }

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

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public void setOldValue(Object oldValue) {
        this.oldValue = oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
}
