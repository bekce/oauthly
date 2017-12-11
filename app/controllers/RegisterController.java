package controllers;

import config.RecaptchaProtected;
import play.mvc.Controller;
import play.mvc.Result;

public class RegisterController extends Controller {

    public Result get() {
//        if(state == 2){
//
//        }
        return ok();
    }

    @RecaptchaProtected
    public Result post1() {
        return ok();
    }
}
