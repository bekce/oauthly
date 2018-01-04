package controllers;

import config.AuthorizationServerSecure;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import repositories.UserRepository;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by Selim Eren Bekçe on 12.12.2017.
 */
public class UserController extends Controller {
    @Inject
    private UserRepository userRepository;

    @AuthorizationServerSecure(requireAdmin = true)
    public Result get() {
        User user = request().attrs().get(AuthorizationServerSecure.USER);
        List<User> list = StreamSupport.stream(userRepository.findAll().spliterator(), false).collect(Collectors.toList());
        return ok(views.html.users.render(user, list));
    }


}
