package repositories;

//import static config.MongoConfig.datastore;

import dtos.Utils;
import models.User;
import org.jongo.MongoCollection;
import uk.co.panaxiom.playjongo.PlayJongo;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Locale;

@Singleton
public class UserRepository {

    private MongoCollection collection;

    @Inject
    public UserRepository(PlayJongo playJongo) {
        this.collection = playJongo.jongo().getCollection("user");
    }

    public User findById(String id) {
        System.err.println("id:"+id);
        return collection.findOne("{_id:#}", id).as(User.class);
//        return datastore().createQuery(User.class).field("id").equal(id).get();
    }

    public void save(User u){
        collection.save(u);
//        datastore().save(u);
    }

    public User findByUsernameNormalized(String normalizedUsername) {
        return collection.findOne("{normalizedUsername:#}",normalizedUsername).as(User.class);
//        return datastore().createQuery(User.class).field("normalizedUsername").equal(normalizedUsername).get();
    }

    public User findByEmail(String email) {
        return collection.findOne("{email:#}",email).as(User.class);
//        return datastore().createQuery(User.class).field("email").equal(email).get();
    }

    public User findByUsernameOrEmail(String login) {
        String normalizedUsername = Utils.normalizeUsername(login);
        User user = findByUsernameNormalized(normalizedUsername);
        if (user == null) {
            user = findByEmail(login.toLowerCase(Locale.ENGLISH));
        }
        return user;
    }

}
