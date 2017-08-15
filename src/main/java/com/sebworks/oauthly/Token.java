package com.sebworks.oauthly;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * Created by Selim Eren Bekçe on 2016-10-10.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Token {
    private String access_token;
    private String refresh_token;
    private String token_type;
    private Long created_at;
    private Long expires_in;
    private String scope;
    private String error;

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
