package com.sebworks.oauthly.service;

import com.sebworks.oauthly.entity.User;

import java.util.concurrent.CompletableFuture;

/**
 * Created by Selim Eren Bekçe on 6.09.2017.
 */
public interface MailService {
    CompletableFuture<String> sendEmail(String to, String subject, String content);
}
