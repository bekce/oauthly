package controllers;

import config.JwtUtils;
import config.RecaptchaProtected;
import dtos.RegistrationDto;
import dtos.Utils;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import repositories.UserRepository;

import javax.inject.Inject;
import java.util.Locale;

public class RegisterController extends Controller {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final FormFactory formFactory;

    @Inject
    public RegisterController(FormFactory formFactory, UserRepository userRepository, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.formFactory = formFactory;
    }

    public Result step1(String next) {
        return ok(views.html.register1.render(formFactory.form(RegistrationDto.class), next));
    }

    public Result step2(String next) {
        return ok(views.html.register2.render(flash("email")));
    }

    @RecaptchaProtected
    public Result post1(String next) {

        Form<RegistrationDto> form = formFactory.form(RegistrationDto.class).bindFromRequest();
        if(form.hasErrors()){
            flash("warning", "Form has errors");
            return badRequest(views.html.register1.render(form, next));
        }
        RegistrationDto dto = form.get();
        // normalize username
        dto.setUsernameNormalized(Utils.normalizeUsername(dto.getUsername()));
        // normalize email
        dto.setEmail(dto.getEmail().toLowerCase(Locale.ENGLISH));
        if (userRepository.findByUsernameNormalized(dto.getUsernameNormalized()) != null) {
            form = form.withError("username", "Username already exists");
        }
        if (userRepository.findByEmail(dto.getEmail()) != null) {
            form = form.withError("email", "Email is already registered");
        }
        // advanced email validation with commons-validator
//        if(!EmailValidator.getInstance().isValid(dto.getEmail())){
//            form = form.withError("email", "Invalid.userForm.email");
//        }
        if(form.hasErrors()){
            flash("warning", "Form has errors");
            return badRequest(views.html.register1.render(form, next));
        }

        User user = new User();
        user.setId(Utils.newId());
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setUsernameNormalized(dto.getUsernameNormalized());
        user.setCreationTime(System.currentTimeMillis());
        user.encryptThenSetPassword(dto.getPassword());
        if(userRepository.count() == 0)
            user.setAdmin(true);
        userRepository.save(user);

        String cookieValue = jwtUtils.prepareCookie(user);
        Http.Cookie ltat = Http.Cookie.builder("ltat", cookieValue).withPath("/").withHttpOnly(true).withMaxAge(jwtUtils.getExpireCookie()).build();
        flash("info", "Registration successful");
        if(next != null && next.matches("^/.*$"))
            return redirect(next).withCookies(ltat);
        else
            return redirect(routes.ProfileController.get()).withCookies(ltat);

    }
}
