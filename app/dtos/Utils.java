package dtos;

import java.util.Locale;

/**
 * Created by Selim Eren Bekçe on 25.08.2017.
 */
public class Utils {
    public static String normalizeUsername(String username){
        return username.replaceAll("[-\\\\.]","_").toLowerCase(Locale.ENGLISH);
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
}
