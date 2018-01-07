package controllers;

import com.typesafe.config.Config;
import config.JwtUtils;
import config.MailgunService;
import config.RecaptchaProtected;
import emails.html.resetPassword;
import models.User;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Result;
import repositories.EventRepository;
import repositories.UserRepository;

import javax.inject.Inject;

public class ResetPasswordController extends Controller {

    @Inject
    private JwtUtils jwtUtils;
    @Inject
    private FormFactory formFactory;
    @Inject
    private UserRepository userRepository;
    @Inject //TODO change this to use generic MailService
    private MailgunService mailService;
    @Inject
    private Config config;
    @Inject
    private EventRepository eventRepository;

    public static class Step1Dto {
        @Constraints.Required
        public String login;
    }

    public static class Step3Dto {
        @Constraints.Required
        @Constraints.MaxLength(32)
        @Constraints.MinLength(4)
        public String password;
    }

    public Result step1(String next){
        return ok(views.html.resetPassword1.render(next, formFactory.form(Step1Dto.class)));
    }

    @RecaptchaProtected
    public Result step2(String next){
        Form<Step1Dto> form = formFactory.form(Step1Dto.class).bindFromRequest();
        if(form.hasErrors()){
            return badRequest(views.html.resetPassword1.render(next, form));
        }
        Step1Dto dto = form.get();
        User user = userRepository.findByUsernameOrEmail(dto.login);
        if(user == null){
            flash("warning", "No account was found with this login");
            return redirect(routes.ResetPasswordController.step1(next));
        }
        String resetCode = jwtUtils.prepareResetCode(user);
        String confirmationUrl = routes.ResetPasswordController.step3(resetCode, next).absoluteURL(request());
        String content = resetPassword.render(
                confirmationUrl,
                user.getEmail(),
                config.getInt("jwt.expire.resetCode") / 3600,
                config.getString("brand.name")).toString();
        Logger.info("Confirmation URL: "+confirmationUrl);
        mailService.sendEmail(user.getEmail(), "Reset your password", content);
        eventRepository.resetPasswordSend(request(), user);
        flash("info", "Your password reset link was just sent to your email address. Please check your inbox and click on the provided link to continue.");
        return redirect(routes.LoginController.get(next));
    }

    public Result step3(String code, String next){
        User user = jwtUtils.validateResetCode(code);
        if(user == null){
            flash("warning", "Invalid reset code, please get another one");
            return redirect(routes.ResetPasswordController.step1(next));
        }
        return ok(views.html.resetPassword3.render(code, user.getEmail(), next, formFactory.form(Step3Dto.class)));
    }

    public Result step4(String code, String next){
        User user = jwtUtils.validateResetCode(code);
        if(user == null){
            flash("warning", "Reset code isn't valid anymore, please get another one");
            return redirect(routes.ResetPasswordController.step1(next));
        }
        Form<Step3Dto> form = formFactory.form(Step3Dto.class).bindFromRequest();
        if(form.hasErrors()){
            return badRequest(views.html.resetPassword3.render(code, user.getEmail(), next, form));
        }
        user.encryptThenSetPassword(form.get().password);
        userRepository.save(user);
        eventRepository.resetPasswordComplete(request(), user);
        flash("success", "Your password was reset! Please login to continue");
        return redirect(routes.LoginController.get(next));
    }
}
