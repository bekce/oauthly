package com.sebworks.oauthly.controller;

import com.sebworks.oauthly.common.DiscourseDto;
import com.sebworks.oauthly.common.SessionDataAccessor;
import com.sebworks.oauthly.entity.User;
import com.sebworks.oauthly.repository.UserRepository;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by Selim Eren Bekçe on 25.08.2017.
 */
@Controller
@RequestMapping("/discourse")
public class DiscourseController {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private SessionDataAccessor sessionDataAccessor;
    @Autowired
    private UserRepository userRepository;

    public DiscourseDto getDto(){
        List<DiscourseDto> settings = mongoTemplate.findAll(DiscourseDto.class, "settings");
        if(settings.size() > 1){
            throw new IllegalStateException("There cannot be more than one Discourse setting");
        }
        return settings.isEmpty() ? new DiscourseDto() : settings.get(0);
    }

    @PostMapping("/settings")
    public String postSettings(@RequestParam(value = "enabled", required = false) boolean enabled,
                               @RequestParam("redirectUri") String redirectUri){
        User user = userRepository.findOne(sessionDataAccessor.access().getUserId());
        if(!user.isAdmin()){
            return "error";
        }

        DiscourseDto dto = getDto();
        dto.setRedirectUri(redirectUri);
        dto.setEnabled(enabled);
        if(enabled && StringUtils.isBlank(dto.getSecret())){
            dto.setSecret(UUID.randomUUID().toString().replace("-",""));
        }
        if(!enabled){
            dto.setRedirectUri(null);
            dto.setSecret(null);
        }
        mongoTemplate.save(dto, "settings");
        return "redirect:/profile";
    }
//
//    public static void main(String[] args) {
//        String secret = "d836444a9e4084d5b224a60c208dce14";
//        String sso = "bm9uY2U9Y2I2ODI1MWVlZmI1MjExZTU4YzAwZmYxMzk1ZjBjMGI=\n";
//        String sig = "2828aa29899722b35a2f191d34ef9b3ce695e0e6eeec47deb46d588d70c7cb56";
//        String username = "eren";
//        String userid = "1231321321";
//        String email = "eren@example.com";
//        String redirecturi = "http://discuss.example.com/session/sso_login";
//
//        try {
//            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
//            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256");
//            sha256_HMAC.init(secret_key);
//            String hexString = Hex.encodeHexString(sha256_HMAC.doFinal(sso.getBytes("UTF-8")));
//            if(!Objects.equals(hexString, sig)){
//                System.out.println("Signature mismatch: "+hexString);
//            }
//
//            String nonce = new String(Base64.decodeBase64(sso), StandardCharsets.UTF_8);
//            String payload = String.format("%s&name=%s&username=%s&email=%s&external_id=%s&require_activation=false", nonce, URLEncoder.encode(username, "utf-8"), URLEncoder.encode(username, "utf-8"), URLEncoder.encode(email, "utf-8"), URLEncoder.encode(userid, "utf-8"));
//            System.out.println("payload:"+payload);
//            payload = Base64.encodeBase64String(payload.getBytes(StandardCharsets.UTF_8));
//            String sig_new = Hex.encodeHexString(sha256_HMAC.doFinal(payload.getBytes("UTF-8")));
//            String ret = "redirect:"+redirecturi+"?sso="+URLEncoder.encode(payload, "utf-8")+"&sig="+sig_new;
//            System.out.println(ret);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    @GetMapping("/sso")
    public String sso(@RequestParam("sso") String sso, @RequestParam("sig") String sig){

        DiscourseDto dto = getDto();
        if(!dto.isEnabled()){
            return "error";
        }
        User user = userRepository.findOne(sessionDataAccessor.access().getUserId());
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(dto.getSecret().getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            String hexString = Hex.encodeHexString(sha256_HMAC.doFinal(sso.getBytes("UTF-8")));
            if(!Objects.equals(hexString, sig)){
                System.out.println("Signature mismatch");
                return "error";
            }

            String nonce = new String(Base64.decodeBase64(sso), StandardCharsets.UTF_8);
            String payload = String.format("%s&name=%s&username=%s&email=%s&external_id=%s&require_activation=false", nonce, URLEncoder.encode(user.getUsername(), "utf-8"), URLEncoder.encode(user.getUsername(), "utf-8"), URLEncoder.encode(user.getEmail(), "utf-8"), URLEncoder.encode(user.getId(), "utf-8"));
            payload = Base64.encodeBase64String(payload.getBytes(StandardCharsets.UTF_8));
            String sig_new = Hex.encodeHexString(sha256_HMAC.doFinal(payload.getBytes("UTF-8")));
            return "redirect:"+dto.getRedirectUri()+"?sso="+URLEncoder.encode(payload, "utf-8")+"&sig="+sig_new;

        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }

    }

}
