package com.sebworks.oauthly.repository;

import com.sebworks.oauthly.entity.Client;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by Selim Eren Bekçe on 15.08.2017.
 */
public interface ClientRepository extends MongoRepository<Client, String> {
    List<Client> findByOwnerId(String ownerId);
}
