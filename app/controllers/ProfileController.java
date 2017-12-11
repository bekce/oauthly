package controllers;

import config.AuthorizationServerSecure;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.*;

@AuthorizationServerSecure
public class ProfileController extends Controller {

    public Result get(){
        User user = request().attrs().get(AuthorizationServerSecure.USER);
//        User user = (User) ctx().args.get("user");
        return ok(views.html.profile.render(user, flash()));
    }
}
