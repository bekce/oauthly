package com.sebworks.oauthly.repository;

import com.sebworks.oauthly.entity.Scope;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by Selim Eren Bekçe on 15.08.2017.
 */
public interface ScopeRepository extends MongoRepository<Scope, String> {
    List<Scope> findByClientId(String clientId);
}
