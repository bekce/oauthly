package models;

import org.jongo.marshall.jackson.oid.MongoId;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Models a user, more specifically a 'resource owner'
 * Created by Selim Eren Bekçe on 15.08.2017.
 */
public class User {
    @MongoId
    private String id;
    //    @Indexed
    private String username;
    //    @Indexed
    private String usernameNormalized;
    //    @Indexed
    private String email;
    private String password;
    /**
     * Can create his/her own clients
     */
    private boolean admin;
    /**
     * Last update time to important changes like email and password in millis
     */
    private long lastUpdateTime;
    /**
     * User creation time in millis
     */
    private long creationTime;
    /**
     * If the user's email address verified with confirmation link, value will be true.
     * Value can only be false if this user was imported from another system.
     */
    private boolean emailVerified;
    /**
     * If the user is disabled, this field will contain the reason, else it will be null.
     * Disabled users are not able to login.
     */
    private String disabledReason;

    public void encryptThenSetPassword(String password_plaintext) {
        String salt = BCrypt.gensalt(12);
        this.password = BCrypt.hashpw(password_plaintext, salt);
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public boolean checkPassword(String password_plaintext) {
        if (this.password == null || password_plaintext == null)
            return false;
        if (!this.password.startsWith("$2a$"))
            throw new IllegalArgumentException("Invalid hash provided for comparison");
        return BCrypt.checkpw(password_plaintext, password);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsernameNormalized() {
        return usernameNormalized;
    }

    public void setUsernameNormalized(String usernameNormalized) {
        this.usernameNormalized = usernameNormalized;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public boolean isDisabled() {
        return disabledReason != null;
    }

    public String getDisabledReason() {
        return disabledReason;
    }

    public void setDisabledReason(String disabledReason) {
        this.disabledReason = disabledReason;
    }
}
