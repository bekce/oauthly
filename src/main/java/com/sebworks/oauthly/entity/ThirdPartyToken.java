package com.sebworks.oauthly.entity;

import com.sebworks.oauthly.Token;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

/**
 * Models a third party token, such as from facebook
 * Created by Selim Eren Bekçe on 15.08.2017.
 */
@Data
public class ThirdPartyToken {
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
     * retrieved token
     */
    private Token token;
}
