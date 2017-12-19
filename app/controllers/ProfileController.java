package controllers;

import config.AuthorizationServerSecure;
import config.JwtUtils;
import dtos.Utils;
import models.User;
import org.apache.commons.lang3.StringUtils;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import repositories.UserRepository;

import javax.inject.Inject;

public class ProfileController extends Controller {

    @Inject
    private FormFactory formFactory;
    @Inject
    private UserRepository userRepository;
    @Inject
    private JwtUtils jwtUtils;

    @AuthorizationServerSecure
    public Result get() {
        User user = request().attrs().get(AuthorizationServerSecure.USER);
        return ok(views.html.profile.render(user));
    }

    @AuthorizationServerSecure
    public Result changePassword(){
        User user = request().attrs().get(AuthorizationServerSecure.USER);

        String oldPassword = request().body().asFormUrlEncoded().get("oldPassword")[0];
        String newPassword = request().body().asFormUrlEncoded().get("newPassword")[0];

        if(StringUtils.isAnyBlank(oldPassword, newPassword)){
            flash("error", "All fields are required");
            return redirect(routes.ProfileController.get());
        }
        else if(!user.checkPassword(oldPassword)){
            flash("error", "Current password is invalid");
            return redirect(routes.ProfileController.get());
        }
        else if(Utils.newPasswordCheck(newPassword, newPassword) != null){
            flash("error", Utils.newPasswordCheck(newPassword, newPassword));
            return redirect(routes.ProfileController.get());
        }
        else {
            user.encryptThenSetPassword(newPassword);
            userRepository.save(user);
            flash("success", "You have changed your password!");
            // refresh the cookie here
            return jwtUtils.prepareCookieThenRedirect(user, null);
        }
    }



}
