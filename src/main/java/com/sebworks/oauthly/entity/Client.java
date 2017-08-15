package com.sebworks.oauthly.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

/**
 * Client, a.k.a. App
 * Created by Selim Eren Bekçe on 15.08.2017.
 */
@Data
public class Client {
    @Id
    private String id;
    private String secret;
    private String name;
    private String logoUrl;
    /**
     * If true, then all grants shall automatically be given without user consent
     */
    private boolean trusted;
    /**
     * Origin of redirect uri for each request shall match this.
     * e.g. 'http://localhost:8080'
     */
    private String redirectUri;
    /**
     * The id of the managing user of this client
     */
    private String ownerId;
}
