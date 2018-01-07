package controllers;

import config.AuthorizationServerSecure;
import config.Utils;
import models.DiscourseSetting;
import models.User;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import repositories.SettingRepository;
import repositories.UserRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Created by Selim Eren Bekçe on 25.08.2017.
 */
public class DiscourseController extends Controller {

    @Inject
    private SettingRepository settingRepository;
    @Inject
    private UserRepository userRepository;
    @Inject
    private FormFactory formFactory;

    private DiscourseSetting getSetting() {
        DiscourseSetting setting = settingRepository.findById(DiscourseSetting.class);
        if(setting == null) setting = new DiscourseSetting();
        return setting;
    }

    @AuthorizationServerSecure(requireAdmin = true)
    public Result get() {
        User user = request().attrs().get(AuthorizationServerSecure.USER);
        DiscourseSetting setting = getSetting();
        return ok(views.html.discourse.render(user, setting));
    }


    @AuthorizationServerSecure(requireAdmin = true)
    public Result updateSettings() {
        Form<DiscourseSetting> form = formFactory.form(DiscourseSetting.class).bindFromRequest("enabled", "redirectUri");
        DiscourseSetting setting = getSetting();
        setting.setRedirectUri(form.get().getRedirectUri());
        setting.setEnabled(form.get().isEnabled());
        Logger.info(form.get().toString());

        if(form.get().isEnabled() && StringUtils.isBlank(setting.getSecret())){
            setting.setSecret(Utils.newSecret());
        }
        if(!form.get().isEnabled()){
            setting.setRedirectUri(null);
            setting.setSecret(null);
        }
        settingRepository.save(setting);
        flash("info", "Settings updated");
        return redirect(routes.DiscourseController.get());
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

    @AuthorizationServerSecure
    public Result sso(String sso, String sig){
        DiscourseSetting setting = getSetting();
        if(!setting.isEnabled()){
            return notFound();
        }
        User user = request().attrs().get(AuthorizationServerSecure.USER);
        if(!user.isEmailVerified()) {
            return redirect(routes.ProfileController.changeEmailPage(ctx().request().uri()));
        }

        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(setting.getSecret().getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            String hexString = Hex.encodeHexString(sha256_HMAC.doFinal(sso.getBytes("UTF-8")));
            if(!Objects.equals(hexString, sig)){
                return badRequest(Json.newObject().put("message", "signature mismatch"));
            }

            String nonce = new String(Base64.decodeBase64(sso), StandardCharsets.UTF_8);
            String payload = String.format("%s&name=%s&username=%s&email=%s&external_id=%s&require_activation=false", nonce, URLEncoder.encode(user.getUsername(), "utf-8"), URLEncoder.encode(user.getUsername(), "utf-8"), URLEncoder.encode(user.getEmail(), "utf-8"), URLEncoder.encode(user.getId(), "utf-8"));
            payload = Base64.encodeBase64String(payload.getBytes(StandardCharsets.UTF_8));
            String sig_new = Hex.encodeHexString(sha256_HMAC.doFinal(payload.getBytes("UTF-8")));

            return redirect(setting.getRedirectUri()+"?sso="+URLEncoder.encode(payload, "utf-8")+"&sig="+sig_new);

        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
            return internalServerError(Json.newObject().put("message", e.getMessage()));
        }

    }

}
