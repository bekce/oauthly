package controllers;

import config.AuthorizationServerSecure;
import config.JwtUtils;
import config.LoggingFilter;
import dtos.LoginDto;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.*;
import repositories.UserRepository;

import javax.inject.Inject;
import java.util.Optional;

public class LoginController extends Controller {
    private final Form<LoginDto> form;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    @Inject
    public LoginController(FormFactory formFactory, UserRepository userRepository, JwtUtils jwtUtils) {
        this.form = formFactory.form(LoginDto.class);
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
    }

    public Result get(String next) {
//        Object key = request().attrs().get(LoggingFilter.KEY);
//        System.out.println(key);
//        User user = userRepository.findById(session("user"));
        Optional<User> user = request().attrs().getOptional(AuthorizationServerSecure.USER);
        if(user.isPresent()){ // already authenticated
            if(next != null && next.matches("^/.*$"))
                return redirect(next);
            else
                return redirect(routes.ProfileController.get());
//            flash("info", "Authenticated user id: "+user.getId());
//            ok(views.html.login.render(flash()));
        }
        return ok(views.html.login.render(next, flash()));
    }

    public Result post(String next) {
        Form<LoginDto> loginForm = form.bindFromRequest();
        String info = String.format("received %s:%s", loginForm.get().getUsername(), loginForm.get().getPassword());
        System.out.println(info);
        User user = userRepository.findByUsernameOrEmail(loginForm.get().getUsername());
        if(user != null && user.checkPassword(loginForm.get().getPassword())){
            System.err.println("login success");
            String cookieValue = jwtUtils.prepareCookie(user);
            Http.Cookie ltat = Http.Cookie.builder("ltat", cookieValue).withPath("/").withHttpOnly(true).withMaxAge(jwtUtils.getExpireCookie()).build();
            flash("info", "Login successful");
            if(next != null && next.matches("^/.*$"))
                return redirect(next).withCookies(ltat);
            else
                return redirect(routes.ProfileController.get()).withCookies(ltat);
        } else {
//            flash("info", info);
            flash("error", "Invalid login");
            return redirect(routes.LoginController.get(next));
        }
    }

    public Result logout(){
        Http.Cookie ltat = Http.Cookie.builder("ltat", "")
                .withPath("/").withHttpOnly(true).withMaxAge(0).build();
        Optional<String> referer = request().header("Referer");
        if(referer.isPresent()){
            return redirect(referer.get()).withCookies(ltat);
        } else {
            return redirect(routes.LoginController.get(null));
        }
    }

}
