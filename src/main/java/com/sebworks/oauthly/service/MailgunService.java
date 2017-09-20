package com.sebworks.oauthly.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sebworks.oauthly.entity.User;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by Selim Eren Bekçe on 6.09.2017.
 */
public class MailgunService implements MailService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MailgunService.class);

    private final String mailgunKey;
    private final String mailgunFrom;
    private final String mailgunDomain;
    private final String brandName;

//    private final TemplateEngine templateEngine;
    private final OkHttpClient mailgunClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public MailgunService(@Value("${mail.mailgun.key}") String mailgunKey,
                          @Value("${mail.mailgun.from}") String mailgunFrom,
                          @Value("${mail.mailgun.domain}") String mailgunDomain,
                          @Value("${brand.name}") String brandName) {
        this.mailgunKey = mailgunKey;
        this.mailgunFrom = mailgunFrom;
        this.mailgunDomain = mailgunDomain;
        this.brandName = brandName;
        this.mailgunClient = new OkHttpClient.Builder().authenticator((route, response) -> {
            String credential = Credentials.basic("api", mailgunKey);
            return response.request().newBuilder().header("Authorization", credential).build();
        }).build();
    }

    @Override
    public CompletableFuture<String> sendEmail(String to, String subject, String content) {
        return CompletableFuture.supplyAsync(() -> {
            MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("from", mailgunFrom)
                    .addFormDataPart("to", to)
                    .addFormDataPart("subject", subject)
                    .addFormDataPart("html", content);
            Request request = new Request.Builder()
                    .url(String.format("https://api.mailgun.net/v3/%s/messages", mailgunDomain))
                    .post(multipartBuilder.build())
                    .build();
            try {
                Response response = mailgunClient.newCall(request).execute();
                if(response.code() != 200){
                    throw new IOException("Non-200 response: "+response.toString() + ", body: "+response.body().string());
                }
                String responseString = response.body().string();
                log.info("Message sent, response: "+responseString);
                JsonNode responseJson = objectMapper.readValue(responseString, JsonNode.class);
                return responseJson.get("id").textValue();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new CompletionException(e);
            }
        }, executorService);
    }
}
