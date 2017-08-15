package com.sebworks.oauthly.repository;

import com.sebworks.oauthly.entity.ThirdPartyToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by Selim Eren Bekçe on 15.08.2017.
 */
public interface ThirdPartyTokenRepository extends MongoRepository<ThirdPartyToken, String> {
    List<ThirdPartyToken> findByUserId(String userId);
}
