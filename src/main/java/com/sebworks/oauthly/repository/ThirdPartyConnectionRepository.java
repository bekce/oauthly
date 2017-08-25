package com.sebworks.oauthly.repository;

import com.sebworks.oauthly.entity.ThirdPartyConnection;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by Selim Eren Bekçe on 15.08.2017.
 */
public interface ThirdPartyConnectionRepository extends MongoRepository<ThirdPartyConnection, String> {
    List<ThirdPartyConnection> findByUserId(String userId);
}
