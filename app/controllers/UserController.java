package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import config.AuthorizationServerSecure;
import config.ResourceServerSecure;
import config.Utils;
import models.Grant;
import models.User;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import repositories.EventRepository;
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
    @Inject
    private EventRepository eventRepository;

    @AuthorizationServerSecure(requireAdmin = true)
    public Result get() {
        User user = request().attrs().get(AuthorizationServerSecure.USER);
        List<User> list = StreamSupport.stream(userRepository.findAll().spliterator(), false).collect(Collectors.toList());
        return ok(views.html.users.render(user, list));
    }

    @ResourceServerSecure(scope = "user:create")
    @BodyParser.Of(BodyParser.Json.class)
    public Result apiCreate() {
        Grant grant = request().attrs().get(ResourceServerSecure.GRANT);
        User user = userRepository.findById(grant.getUserId());
        if (!user.isAdmin()) {
            return unauthorized("need admin");
        }
        JsonNode json = request().body().asJson();
        boolean bypassEmailCheck = json.path("options").path("bypassEmailCheck").asBoolean(false);
        boolean bypassUsernameCheck = json.path("options").path("bypassUsernameCheck").asBoolean(false);
        boolean update = json.path("options").path("update").asBoolean(false);
        String id = json.path("user").path("id").asText(Utils.newId());
        String username = json.path("user").path("username").asText(null);
        String email = json.path("user").path("email").asText(null);
        String password = json.path("user").path("password").asText(null);
        boolean emailVerified = json.path("user").path("emailVerified").asBoolean(false);
        long creationTime = json.path("user").path("creationTime").asLong(System.currentTimeMillis());
        long lastUpdateTime = json.path("user").path("lastUpdateTime").asLong(System.currentTimeMillis());
        String disabledReason = json.path("user").path("disabledReason").asText(null);
        User byId = userRepository.findById(id);
        if (!update && byId != null) {
            return badRequest("duplicate id");
        }
        if (update && byId == null) {
            return badRequest("no user found with id " + id);
        }
        User byEmail = userRepository.findByEmail(Utils.normalizeEmail(email));
        if (!bypassEmailCheck && email != null && byEmail != null && !byEmail.getId().equals(id)) {
            return badRequest("duplicate email");
        }
        User byUsernameNormalized = userRepository.findByUsernameNormalized(Utils.normalizeUsername(username));
        if (!bypassUsernameCheck && username != null && byUsernameNormalized != null && !byUsernameNormalized.getId().equals(id)) {
            return badRequest("duplicate username");
        }
        if (email == null && username == null) {
            return badRequest("either username or email is necessary");
        }
        if (password == null && email == null) {
            return badRequest("either email or password is necessary (come on!)");
        }
        if (password != null && password.isEmpty()) {
            return badRequest("password cannot be empty (seriously!)");
        }
        User user1 = update ? byId : new User();
        if (user1.isAdmin()) {
            return badRequest("cannot update admins with this");
        }
        user1.setId(id);
        user1.setUsername(username);
        user1.setUsernameNormalized(Utils.normalizeUsername(username));
        user1.setEmail(Utils.normalizeEmail(email));
        user1.setEmailVerified(emailVerified && email != null);
        user1.setCreationTime(creationTime);
        user1.setLastUpdateTime(lastUpdateTime);
        user1.setAdmin(false);
        user1.setDisabledReason(disabledReason);
        if (password != null) user1.encryptThenSetPassword(password);
        else user1.setPassword(null);
        userRepository.save(user1);
        eventRepository.addUpdateUserViaApi(request(), byId, user1);
        return ok("done");
    }

}
