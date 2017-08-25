package com.sebworks.oauthly.common;

import java.util.Locale;

/**
 * Created by Selim Eren Bekçe on 25.08.2017.
 */
public class Utils {
    public static String normalizeUsername(String username){
        return username.replaceAll("[-\\\\.]","_").toLowerCase(Locale.ENGLISH);
    }
}
