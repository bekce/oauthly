package com.sebworks.oauthly;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by Selim Eren Bekçe on 16.08.2017.
 */
@Component
public class SessionDataAccessor implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public SessionData access(){
        return applicationContext.getBean(SessionData.class);
    }
}
