package controllers;

import config.AuthorizationServerSecure;
import config.JwtUtils;
import config.Utils;
import models.ProviderLink;
import models.User;
import org.apache.commons.lang3.StringUtils;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import repositories.ProviderLinkRepository;
import repositories.UserRepository;

import javax.inject.Inject;
import java.util.Objects;

public class ProfileController extends Controller {

    @Inject
    private FormFactory formFactory;
    @Inject
    private UserRepository userRepository;
    @Inject
    private JwtUtils jwtUtils;
    @Inject
    private ProviderLinkRepository providerLinkRepository;
    @Inject
    private config.AuthorizationServerManager authorizationServerManager;

    @AuthorizationServerSecure
    public Result get() {
        User user = request().attrs().get(AuthorizationServerSecure.USER);
        return ok(views.html.profile.render(user, authorizationServerManager.getProviders(), providerLinkRepository.findMapByUserId(user.getId())));
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

    @AuthorizationServerSecure
    public Result linkProvider(String linkId) {
        User user = request().attrs().get(AuthorizationServerSecure.USER);
        ProviderLink link = providerLinkRepository.findById(linkId);
        if(link == null){
            return badRequest("bad parameter");
        }
        if(link.getUserId() != null){
            flash("error", "Account is already linked");
            return redirect(routes.ProfileController.get());
        }
        link.setUserId(user.getId());
        providerLinkRepository.save(link);
        flash("info", "You have successfully linked your "+link.getProviderKey()+" with us. You can now login with it");
        return redirect(routes.ProfileController.get());
    }

    @AuthorizationServerSecure
    public Result unlinkProvider(String linkId){
        User user = request().attrs().get(config.AuthorizationServerSecure.USER);
        ProviderLink link = providerLinkRepository.findById(linkId);
        if(link == null || !Objects.equals(link.getUserId(), user.getId())){
            return badRequest("bad parameter");
        }
        if(user.getPassword() == null){
            flash("warning", "Please set a password to unlink");
            return redirect(routes.ProfileController.get());
        }
        providerLinkRepository.delete(linkId);
        flash("info", "Account unlinked");
        return redirect(routes.ProfileController.get());
    }


}
