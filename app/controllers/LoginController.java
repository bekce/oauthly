package controllers;

import config.AuthorizationServerSecure;
import config.JwtUtils;
import dtos.ConstraintGroups;
import dtos.RegistrationDto;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.*;
import repositories.UserRepository;

import javax.inject.Inject;
import java.util.Optional;

public class LoginController extends Controller {
    private final FormFactory formFactory;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    @Inject
    public LoginController(FormFactory formFactory, UserRepository userRepository, JwtUtils jwtUtils) {
        this.formFactory = formFactory;
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
    }

    public Result get(String next) {
        Optional<User> user = request().attrs().getOptional(AuthorizationServerSecure.USER);
        if(user.isPresent()){ // already authenticated
            if(next != null && next.matches("^/.*$"))
                return redirect(next);
            else
                return redirect(routes.ProfileController.get());
        }
        return ok(views.html.login.render(next));
    }

    public Result post(String next) {
        Form<RegistrationDto> form = formFactory.form(RegistrationDto.class, ConstraintGroups.Login.class).bindFromRequest();
        if(form.hasErrors()) {
            flash("warning", "Please fill in the form");
            return redirect(routes.LoginController.get(next));
        }
        User user = userRepository.findByUsernameOrEmail(form.get().getEmail());
        if(user != null && user.checkPassword(form.get().getPassword())){
            flash("info", "Login successful");
            return jwtUtils.prepareCookieThenRedirect(user, next);
        } else {
            flash("error", "Invalid login");
            return redirect(routes.LoginController.get(next));
        }
    }

    public Result logout(){
        Http.Cookie ltat = Http.Cookie.builder("ltat", "")
                .withPath("/").withHttpOnly(true).withMaxAge(0).build();
        Optional<String> referer = request().header("Referer");
        flash("info", "Logout successful");
        if(referer.isPresent()){
            return redirect(referer.get()).withCookies(ltat);
        } else {
            return redirect(routes.LoginController.get(null));
        }
    }

}
