package com.sebworks.oauthly.entity;

import lombok.Data;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

/**
 * Models a user, more specifically a 'resource owner'
 * Created by Selim Eren Bekçe on 15.08.2017.
 */
@Data
public class User {
    @Id
    private String id;
    @Indexed(unique = true)
    private String username;
    @Indexed(unique = true)
    private String usernameNormalized;
    @Indexed(unique = true)
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

    public void encryptThenSetPassword(String password_plaintext){
        String salt = BCrypt.gensalt(12);
        this.password = BCrypt.hashpw(password_plaintext, salt);
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public boolean checkPassword(String password_plaintext){
        if(null == this.password || !this.password.startsWith("$2a$"))
            throw new java.lang.IllegalArgumentException("Invalid hash provided for comparison");
        return BCrypt.checkpw(password_plaintext, password);
    }
}
