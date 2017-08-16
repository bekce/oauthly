package com.sebworks.oauthly;

import com.sebworks.oauthly.entity.User;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This session scoped bean will be used to check whether the given client is already authenticated in the same session prior to current request, without the use of an access token in every subsequent request within the same session. This feature creates session stickiness but it is useful in many types of applications but services which entirely consist of API endpoints. Its use is entirely optional.
 * See {@link OAuthFilter} for the usage.
 *
 * For the multi-user implementation it is customary to put 'who' got authenticated.
 * Created by Selim Eren Bekçe on 2016-10-10.
 */
@Component
@Scope("session")
@Getter
@Setter
public class SessionData {
    private String userId;
    private String clientId;
}
