package com.sebworks.oauthly.service;

import com.sebworks.oauthly.entity.User;
import freemarker.template.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Selim Eren Bekçe on 11.09.2017.
 */
@Component
public class TemplateService {

    @Value("${brand.name}")
    private String brandName;
    @Value("${jwt.expire.resetCode}")
    private int expireResetCode;

    private final Configuration fmConfig;

    public TemplateService(Configuration fmConfig) {
        this.fmConfig = fmConfig;
    }

    public String getResetPasswordTemplate(HttpServletRequest request, User user, String resetCode) {
        try {
            Map<String, Object> context = new HashMap<>();
            context.put("brandName", brandName);
            context.put("username", user.getUsername());
            context.put("expireHours", expireResetCode / 3600);
            URI reqUri = new URI(request.getRequestURL().toString());
            URI uri = new URI(reqUri.getScheme(), null, reqUri.getHost(), reqUri.getPort(), "/reset-password", "reset_code=" + resetCode, null);
            context.put("url", uri.toURL().toString());
            return FreeMarkerTemplateUtils.processTemplateIntoString(fmConfig.getTemplate("email/reset-password.html"), context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
