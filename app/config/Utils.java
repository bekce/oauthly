package config;

import java.security.SecureRandom;
import java.util.Locale;

/**
 * Created by Selim Eren Bekçe on 25.08.2017.
 */
public class Utils {
    public static String normalizeUsername(String username){
        if(username == null) return null;
        return username.replaceAll("[-\\\\.]","_").toLowerCase(Locale.ENGLISH);
    }

    public static String normalizeEmail(String email){
        if(email == null) return null;
        return email.toLowerCase(Locale.ENGLISH);
    }

    public static String newPasswordCheck(String newPassword, String newPassword2){
        if (newPassword.length() < 4 || newPassword.length() > 32) {
            return "Please use between 4 and 32";
        }
        else if (!newPassword.equals(newPassword2)) {
            return "These passwords don't match";
        }
        return null;
    }

    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom rnd = new SecureRandom();

    private static String randomString( int len ){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }

    public static String newId() {
        return randomString(20);
//        return RandomStringUtils.randomAlphanumeric(20);
    }
    public static String newSecret() {
        return randomString(32);
    }
}
