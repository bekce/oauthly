package repositories;

import models.Client;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import uk.co.panaxiom.playjongo.PlayJongo;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
public class ClientRepository {

    private MongoCollection collection;

    @Inject
    public ClientRepository(PlayJongo playJongo) {
        this.collection = playJongo.jongo().getCollection("client");
    }

    public Client findById(String id) {
        return collection.findOne("{_id:#}", id).as(Client.class);
    }

    public void save(Client u){
        collection.save(u);
    }

    public List<Client> findByOwnerId(String ownerId){
        MongoCursor<Client> cursor = collection.find("{ownerId:#}", ownerId).as(Client.class);
        return StreamSupport.stream(cursor.spliterator(), false).collect(Collectors.toList());
    }
}
