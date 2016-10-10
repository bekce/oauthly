package com.sebworks;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by Selim Eren Bekçe on 2016-10-10.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Token {
    public String access_token;
    public String refresh_token;
    public String token_type;
    public Long expires_in;
    public String scope;
    public String error;

    public Token(String error) {
        this.error = error;
    }

    public Token(String access_token, String refresh_token, String token_type, long expires_in, String scope) {
        this.access_token = access_token;
        this.refresh_token = refresh_token;
        this.token_type = token_type;
        this.expires_in = expires_in;
        this.scope = scope;
    }
}
