package com.sebworks.oauthly.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

/**
 * Scope record for clients
 * Created by Selim Eren Bekçe on 15.08.2017.
 */
@Data
public class Scope {
    @Id
    private String id;
    /**
     * belonging client
     */
    private String clientId;
    /**
     * e.g. 'email'
     */
    private String scope;
    /**
     * e.g. 'Read your email address'
     */
    private String description;
}
