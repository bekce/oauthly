package controllers;

import config.AuthorizationServerSecure;
import dtos.ClientDto;
import dtos.Utils;
import models.Client;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.*;
import repositories.ClientRepository;

import javax.inject.Inject;
import java.util.List;

@AuthorizationServerSecure
public class ProfileController extends Controller {

    @Inject
    private ClientRepository clientRepository;
    @Inject
    private FormFactory formFactory;

    public Result get() {
        User user = request().attrs().get(AuthorizationServerSecure.USER);
        List<Client> clients = null;
        if (user.isAdmin()) {
            clients = clientRepository.findByOwnerId(user.getId());
        }
        return ok(views.html.profile.render(user, clients));
    }

    public Result addUpdateClient() {
        User user = request().attrs().get(AuthorizationServerSecure.USER);
        if (!user.isAdmin()) {
            return unauthorized(Json.newObject().put("message", "you don't have permission for this operation"));
        }
        try {
            Form<ClientDto> form = formFactory.form(ClientDto.class).bindFromRequest();
            ClientDto dto = form.get();
            if(dto.id == null){
                Client client = new Client();
                client.setId(Utils.newId());
                client.setSecret(Utils.newId());
                client.setOwnerId(user.getId());
                client.setName(dto.name);
                client.setRedirectUri(dto.redirectUri);
                clientRepository.save(client);
                flash("info", "Create client successful");
            } else {
                Client client = clientRepository.findById(dto.id);
                if(!client.getOwnerId().equals(user.getId())){
                    throw new IllegalAccessException();
                }
                client.setName(dto.name);
                client.setRedirectUri(dto.redirectUri);
                clientRepository.save(client);
                flash("info", "Update successful");
            }
        } catch (Exception e) {
            flash("error", e.getMessage());
        }
        return redirect(routes.ProfileController.get());
    }

}
