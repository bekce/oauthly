package com.sebworks.oauthly.repository;

import com.sebworks.oauthly.entity.Grant;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by Selim Eren Bekçe on 15.08.2017.
 */
public interface GrantRepository extends MongoRepository<Grant, String> {
    List<Grant> findByClientId(String clientId);
    List<Grant> findByUserId(String userId);
    Grant findByCode(String code);
}
