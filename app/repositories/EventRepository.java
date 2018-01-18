package repositories;

import models.Event;
import models.EventType;
import models.ProviderLink;
import models.User;
import org.jongo.MongoCollection;
import play.mvc.Http.Request;
import uk.co.panaxiom.playjongo.PlayJongo;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EventRepository {

    private MongoCollection collection;

    @Inject
    public EventRepository(PlayJongo playJongo) {
        this.collection = playJongo.jongo().getCollection("event");
    }

    public void save(Event u){
        collection.save(u);
    }

    private void fillAndSave(Event event, Request request){
        if(request != null){
            event.setIpAddress(request.remoteAddress());
            event.setUserAgent(request.header("User-Agent").orElse(null));
        }
        this.save(event);
    }

    public void login(Request request, User user, String login) {
        fillAndSave(new Event(user.getId(), null, null, EventType.LOGIN), request);
    }
    public void badLogin(Request request, String userId, String login) {
        fillAndSave(new Event(userId, login, null, EventType.BAD_LOGIN), request);
    }
    public void register(Request request, User user) {
        fillAndSave(new Event(user.getId(), null, user, EventType.REGISTER), request);
    }
    public void logout(Request request, User user) {
        fillAndSave(new Event(user == null ? null : user.getId(), null, null, EventType.LOGOUT), request);
    }
    public void resetPasswordSend(Request request, User user) {
        fillAndSave(new Event(user.getId(), null, null, EventType.RESET_PASSWORD_SEND), request);
    }
    public void resetPasswordComplete(Request request, User user) {
        fillAndSave(new Event(user.getId(), null, user.getPassword(), EventType.RESET_PASSWORD_COMPLETE), request);
    }
    public void changePassword(Request request, User user){
        fillAndSave(new Event(user.getId(), null, user.getPassword(), EventType.CHANGE_PASSWORD), request);
    }
    public void changeEmail(Request request, User user, String oldEmail){
        fillAndSave(new Event(user.getId(), oldEmail, user.getEmail(), EventType.CHANGE_EMAIL), request);
    }
    public void providerLink(Request request, User user, ProviderLink providerLink){
        fillAndSave(new Event(user.getId(), null, providerLink, EventType.PROVIDER_LINK), request);
    }
    public void providerUnlink(Request request, User user, ProviderLink providerLink){
        fillAndSave(new Event(user.getId(), providerLink, null, EventType.PROVIDER_UNLINK), request);
    }
    public void addUpdateUserViaApi(Request request, User oldValue, User newValue){
        fillAndSave(new Event(newValue.getId(), oldValue, newValue, EventType.ADD_UPDATE_USER_API), request);
    }
}
