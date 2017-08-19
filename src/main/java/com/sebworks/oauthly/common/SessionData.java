package com.sebworks.oauthly.common;

import com.sebworks.oauthly.filter.AuthorizationServerFilter;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Session scoped bean to track the authenticated user on only the authorization server itself.
 * Resource endpoints will always require a proper access token.
 * See {@link AuthorizationServerFilter} for the usage.
 *
 * Created by Selim Eren Bekçe on 2016-10-10.
 */
@Component
@Scope("session")
@Getter
@Setter
public class SessionData {
    private String userId;
}
