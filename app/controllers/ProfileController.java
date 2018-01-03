package controllers;

import config.AuthorizationServerSecure;
import config.JwtUtils;
import dtos.ConstraintGroups;
import dtos.RegistrationDto;
import models.ProviderLink;
import models.User;
import play.data.Form;
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
        return ok(views.html.profile.render(user, authorizationServerManager.getProviders(), providerLinkRepository.findMapByUserId(user.getId()), formFactory.form(RegistrationDto.class)));
    }

    @AuthorizationServerSecure
    public Result changePassword(){
        User user = request().attrs().get(AuthorizationServerSecure.USER);

        Form<RegistrationDto> form;
        if(user.getPassword() == null){
            form = formFactory.form(RegistrationDto.class, ConstraintGroups.SetPassword.class).bindFromRequest();
        } else {
            form = formFactory.form(RegistrationDto.class, ConstraintGroups.ChangePassword.class).bindFromRequest();
            if(!user.checkPassword(form.value().get().getOldPassword())){
                form = form.withError("oldPassword", "Current password is invalid");
            }
        }
        if(form.hasErrors()) {
            flash("warning", "Form has errors");
            return badRequest(views.html.profile.render(user, authorizationServerManager.getProviders(), providerLinkRepository.findMapByUserId(user.getId()), form));
        }

        user.encryptThenSetPassword(form.get().getPassword());
        userRepository.save(user);
        flash("success", "You have changed your password!");
        // refresh the cookie here
        return jwtUtils.prepareCookieThenRedirect(user, null);
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
