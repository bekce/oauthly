package controllers;

import config.AuthorizationServerManager;
import config.AuthorizationServerSecure;
import config.JwtUtils;
import dtos.ConstraintGroups;
import dtos.RegistrationDto;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import repositories.EventRepository;
import repositories.UserRepository;

import javax.inject.Inject;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class LoginController extends Controller {
    private final FormFactory formFactory;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final JwtUtils jwtUtils;
    private final AuthorizationServerManager authorizationServerManager;

    @Inject
    public LoginController(FormFactory formFactory, UserRepository userRepository, EventRepository eventRepository, JwtUtils jwtUtils, AuthorizationServerManager authorizationServerManager) {
        this.formFactory = formFactory;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.jwtUtils = jwtUtils;
        this.authorizationServerManager = authorizationServerManager;
    }

    @AuthorizationServerSecure(optional = true)
    public Result get(String next) {
        Optional<User> user = request().attrs().getOptional(AuthorizationServerSecure.USER);
        if (user.isPresent()) { // already authenticated
            if (next != null && next.matches("^/.*$"))
                return redirect(next);
            else
                return redirect(routes.ProfileController.get());
        }
        return ok(views.html.login.render(next, authorizationServerManager.getProviders()));
    }

    public Result post(String next) {
        Form<RegistrationDto> form = formFactory.form(RegistrationDto.class, ConstraintGroups.Login.class).bindFromRequest();
        if (form.hasErrors()) {
            flash("warning", "Please fill in the form");
            return redirect(routes.LoginController.get(next));
        }
        List<User> list = userRepository.findByUsernameOrEmailMulti(form.get().getEmail());
        User validUser = null;
        boolean disabled = false;
        for (User user : list) {
            if (user != null && user.checkPassword(form.get().getPassword())) {
                if (user.isDisabled()) {
                    disabled = true;
                    continue;
                }
                validUser = user;
                break;
            }
        }
        if (disabled) {
            flash("error", "Your account was disabled.");
            return redirect(routes.LoginController.get(next));
        } else if (validUser == null) {
            flash("error", "Invalid login");
            eventRepository.badLogin(request(), null, form.get().getEmail());
            return redirect(routes.LoginController.get(next));
        } else {
            flash("info", "Login successful");
            eventRepository.login(request(), validUser, form.get().getEmail());
            return jwtUtils.prepareCookieThenRedirect(validUser, next);
        }
    }

    @AuthorizationServerSecure(optional = true)
    public Result logout() {
        Optional<User> user = request().attrs().getOptional(AuthorizationServerSecure.USER);
        Http.Cookie ltat = Http.Cookie.builder("ltat", "")
                .withPath("/").withHttpOnly(true).withMaxAge(Duration.ZERO).build();
        Optional<String> referer = request().header("Referer");
        eventRepository.logout(request(), user.orElse(null));
        flash("info", "Logout successful");
        if (referer.isPresent()) {
            return redirect(referer.get()).withCookies(ltat);
        } else {
            return redirect(routes.LoginController.get(null));
        }
    }

}
