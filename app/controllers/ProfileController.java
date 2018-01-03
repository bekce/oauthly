package controllers;

import com.typesafe.config.Config;
import config.AuthorizationServerSecure;
import config.JwtUtils;
import config.MailgunService;
import config.RecaptchaProtected;
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
import scala.Tuple2;

import javax.inject.Inject;
import java.util.Objects;

@AuthorizationServerSecure
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
    @Inject
    //TODO change this to use generic MailService
    private MailgunService mailService;
    @Inject
    private Config config;

    public Result get() {
        User user = request().attrs().get(AuthorizationServerSecure.USER);
        return ok(views.html.profile.render(user, authorizationServerManager.getProviders(), providerLinkRepository.findMapByUserId(user.getId()), formFactory.form(RegistrationDto.class), formFactory.form(RegistrationDto.class)));
    }

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
            return badRequest(views.html.profile.render(user, authorizationServerManager.getProviders(), providerLinkRepository.findMapByUserId(user.getId()), form, formFactory.form(RegistrationDto.class)));
        }

        user.encryptThenSetPassword(form.get().getPassword());
        userRepository.save(user);
        flash("success", "Success, your password was changed.");
        // refresh the cookie here
        return jwtUtils.prepareCookieThenRedirect(user, null);
    }

    @RecaptchaProtected(fallback = "/profile")
    public Result changeEmail(){
        User user = request().attrs().get(AuthorizationServerSecure.USER);
        Form<RegistrationDto> form = formFactory.form(RegistrationDto.class, ConstraintGroups.ChangeEmail.class).bindFromRequest();
        if(form.hasErrors()) {
            flash("warning", "Form has errors");
            return badRequest(views.html.profile.render(user, authorizationServerManager.getProviders(), providerLinkRepository.findMapByUserId(user.getId()), formFactory.form(RegistrationDto.class), form));
        }
        String confirmationCode = jwtUtils.prepareEmailChangeConfirmationCode(user, form.get().getEmail());
        String content = emails.html.confirm.render(
                routes.ProfileController.changeEmailConfirm(confirmationCode).absoluteURL(request()),
                config.getString("brand.name")).toString();
        mailService.sendEmail(form.get().getEmail(), "Confirm your new email address", content);
        flash("info", "A confirmation email has been sent to "+form.get().getEmail()+". The change will not be in effect until you click the confirmation link.");
        return redirect(routes.ProfileController.get());
    }

    public Result changeEmailConfirm(String code){
        User user = request().attrs().get(AuthorizationServerSecure.USER);
        Tuple2<User, String> tuple2 = jwtUtils.validateEmailChangeConfirmationCode(code);
        if(tuple2 == null){
            flash("error", "Email change confirm code was invalid, please try again");
            return redirect(routes.ProfileController.get());
        }
        if(!user.getId().equals(tuple2._1.getId())){
            return badRequest("users don't match");
        }
        user.setEmail(tuple2._2);
        user.setEmailValidated(true);
        userRepository.save(user);
        flash("success", "Success, your email address was changed to "+user.getEmail()+". Please use the new address to login from now on.");
        // refresh the cookie here
        return jwtUtils.prepareCookieThenRedirect(user, null);
    }

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

    public Result unlinkProvider(String linkId){
        User user = request().attrs().get(AuthorizationServerSecure.USER);
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
