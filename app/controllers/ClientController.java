package controllers;

import config.AuthorizationServerSecure;
import dtos.ClientDto;
import config.Utils;
import models.Client;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import repositories.ClientRepository;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

@AuthorizationServerSecure(requireAdmin = true)
public class ClientController extends Controller {

    @Inject
    private ClientRepository clientRepository;
    @Inject
    private FormFactory formFactory;

    public Result get() {
        User user = request().attrs().get(AuthorizationServerSecure.USER);
        List<Client> clients = clientRepository.findByOwnerId(user.getId());
        return ok(views.html.clients.render(user, clients));
    }

    public Result create() {
        User user = request().attrs().get(AuthorizationServerSecure.USER);
        return ok(views.html.client.render(user, new Client()));
    }

    public Result edit(String id) {
        User user = request().attrs().get(AuthorizationServerSecure.USER);
        Client client = clientRepository.findById(id);
        if(client == null)
            return badRequest("client not found");
        if(!Objects.equals(client.getOwnerId(), user.getId()))
            return badRequest("not allowed");
        return ok(views.html.client.render(user, client));
    }

    public Result addUpdateClient(String id) {
        User user = request().attrs().get(AuthorizationServerSecure.USER);
        try {
            Form<ClientDto> form = formFactory.form(ClientDto.class).bindFromRequest();
            ClientDto dto = form.get();
            if(id == null || id.isEmpty()){
                Client client = new Client();
                client.setId(Utils.newId());
                client.setSecret(Utils.newId());
                client.setOwnerId(user.getId());
                client.setName(dto.name);
                client.setRedirectUri(dto.redirectUri);
                client.setAllowedOrigin(dto.allowedOrigin);
                clientRepository.save(client);
                flash("info", "Create client successful");
            } else {
                Client client = clientRepository.findById(id);
                if(!client.getOwnerId().equals(user.getId())){
                    throw new IllegalAccessException();
                }
                client.setName(dto.name);
                client.setRedirectUri(dto.redirectUri);
                client.setAllowedOrigin(dto.allowedOrigin);
                clientRepository.save(client);
                flash("info", "Update successful");
            }
            return redirect(routes.ClientController.get());
        } catch (Exception e) {
            flash("error", e.getMessage());
            if(id == null || id.isEmpty()) {
                return redirect(routes.ClientController.create());
            } else {
                return redirect(routes.ClientController.edit(id));
            }
        }
    }

}
