package com.sebworks.oauthly.entity;

import lombok.Data;
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
    private String email;
    private String password;
    /**
     * Contains user's long-lived authenticator
     */
    @Indexed(unique = true)
    private String cookie;
}
