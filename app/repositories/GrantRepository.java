package repositories;

import models.Grant;
import org.jongo.MongoCollection;
import uk.co.panaxiom.playjongo.PlayJongo;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GrantRepository {

    private MongoCollection collection;

    @Inject
    public GrantRepository(PlayJongo playJongo) {
        this.collection = playJongo.jongo().getCollection("grant");
    }

    public Grant findById(String id) {
        return collection.findOne("{_id:#}", id).as(Grant.class);
    }

    public void save(Grant u){
        collection.save(u);
    }

    public Grant findByClientAndUser(String clientId, String userId) {
        return collection.findOne("{clientId:#, userId:#}", clientId, userId).as(Grant.class);
    }
}
