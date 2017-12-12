package repositories;

import models.Setting;
import org.jongo.MongoCollection;
import uk.co.panaxiom.playjongo.PlayJongo;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SettingRepository {

    private MongoCollection collection;

    @Inject
    public SettingRepository(PlayJongo playJongo) {
        this.collection = playJongo.jongo().getCollection("setting");
    }

    public <T> T findById(Class<T> clazz) {
        return collection.findOne("{_id:#}", clazz.getSimpleName()).as(clazz);
//        return setting != null ? (T) setting.getValue() : null;
    }

    public void save(Setting u){
        u.setId(u.getClass().getSimpleName());
        collection.save(u);
//        Setting setting = collection.findOne("{_id:#}", u.getClass().getSimpleName()).as(Setting.class);
//        if(setting == null)
//            setting = new Setting(u.getClass().getSimpleName(), u);
//        else
//            setting.setValue(u);
//        collection.save(setting);
    }

}
