package repositories;

import config.Utils;
import models.User;
import org.jongo.MongoCollection;
import uk.co.panaxiom.playjongo.PlayJongo;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Singleton
public class UserRepository {

    private MongoCollection collection;

    @Inject
    public UserRepository(PlayJongo playJongo) {
        this.collection = playJongo.jongo().getCollection("user");
    }

    public User findById(String id) {
        return collection.findOne("{_id:#}", id).as(User.class);
    }

    public void save(User u){
        collection.save(u);
    }

    public User findByUsernameNormalized(String usernameNormalized) {
        return collection.findOne("{usernameNormalized:#}",usernameNormalized).as(User.class);
    }

    public User findByEmail(String email) {
        return collection.findOne("{email:#}",email).as(User.class);
    }

    public User findByUsernameOrEmail(String login) {
        String normalizedUsername = Utils.normalizeUsername(login);
        User user = findByUsernameNormalized(normalizedUsername);
        if (user == null) {
            user = findByEmail(Utils.normalizeEmail(login));
        }
        return user;
    }

    public List<User> findByUsernameOrEmailMulti(String login) {
        String normalizedUsername = Utils.normalizeUsername(login);
        String email = Utils.normalizeEmail(login);
        List<User> list = new ArrayList<>();
        Iterator<User> iterator = collection.find("{usernameNormalized:#}", normalizedUsername).as(User.class).iterator();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        iterator = collection.find("{email:#}", email).as(User.class).iterator();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    public Iterable<User> findAll(){
        return collection.find().as(User.class);
    }

    public long count() {
        return collection.count();
    }
}
