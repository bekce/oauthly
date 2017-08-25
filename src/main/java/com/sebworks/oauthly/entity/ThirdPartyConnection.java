package com.sebworks.oauthly.entity;

import com.sebworks.oauthly.common.Token;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

/**
 * Models a third party token, such as from facebook
 * Created by Selim Eren Bekçe on 15.08.2017.
 */
@Data
public class ThirdPartyConnection {
    @Id
    private String id;
    /**
     * belonging user
     */
    @Indexed
    private String userId;
    /**
     * e.g. 'facebook' or 'twitter'
     */
    private String party;
    /**
     * last retrieved token
     */
    private Token token;
    /**
     * User id on the remote party (e.g. user id on facebook)
     */
    @Indexed
    private String remoteUserId;
}
