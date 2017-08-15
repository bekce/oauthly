package com.sebworks.oauthly.repository;

import com.sebworks.oauthly.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by Selim Eren Bekçe on 15.08.2017.
 */
public interface UserRepository extends MongoRepository<User, String> {
    User findByUsername(String username);
    User findByEmail(String email);
    User findByCookie(String cookie);
}
