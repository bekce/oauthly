package controllers;

import config.ResourceServerSecure;
import dtos.MeDto;
import models.Grant;
import models.ProviderLink;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import repositories.ProviderLinkRepository;
import repositories.UserRepository;
import scala.Tuple2;

import javax.inject.Inject;
import java.util.stream.Collectors;

public class MeController extends Controller {

    @Inject
    private UserRepository userRepository;
    @Inject
    private ProviderLinkRepository providerLinkRepository;

    @ResourceServerSecure(scope = "profile")
    public Result get(){
        Grant grant = request().attrs().get(ResourceServerSecure.GRANT);
        User user = userRepository.findById(grant.getUserId());
        MeDto dto = new MeDto();
        if(grant.getScopes().contains("user_social_links")){
            dto.setSocialLinks(providerLinkRepository.findByUserId(user.getId()).stream().collect(
                    Collectors.toMap(ProviderLink::getProviderKey, c-> Tuple2.apply(c.getRemoteUserId(), c.getToken()))
            ));
        }
        dto.setName(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setId(user.getId());
        return ok(Json.toJson(dto));
    }
}
