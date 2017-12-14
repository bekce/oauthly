package controllers;

import config.ResourceServerSecure;
import dtos.MeDto;
import models.Grant;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import repositories.UserRepository;

import javax.inject.Inject;

public class MeController extends Controller {

    @Inject
    private UserRepository userRepository;

    @ResourceServerSecure(scope = "read")
    public Result get(){
        Grant grant = request().attrs().get(ResourceServerSecure.GRANT);
        User user = userRepository.findById(grant.getUserId());
        MeDto dto = new MeDto();
        dto.setName(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setId(user.getId());
        return ok(Json.toJson(dto));
    }
}
