package com.sebworks.oauthly.service;

import com.sebworks.oauthly.entity.User;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Selim Eren Bekçe on 11.09.2017.
 */
@Component
public class TemplateService {

    private final Configuration fmConfig;

    public TemplateService(Configuration fmConfig) {
        this.fmConfig = fmConfig;
    }


    public String getResetCodeTemplate(User user, String resetCode) {
        Map<String, Object> context = new HashMap<>();
        try {
            return FreeMarkerTemplateUtils.processTemplateIntoString(fmConfig.getTemplate("resetCode.txt"), context);


        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }

        return null;
    }

}
