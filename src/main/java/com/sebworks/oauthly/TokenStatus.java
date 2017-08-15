package com.sebworks.oauthly;

/**
 * Created by Selim Eren Bekçe on 2016-10-10.
 */
public enum TokenStatus {
    /**
     * Token is invalid
     */
    INVALID,
    /**
     * Token is a valid access token
     */
    VALID_ACCESS,
    /**
     * Token is a valid refresh token. Does not qualify for an access token!
     */
    VALID_REFRESH
}
