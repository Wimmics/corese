
package fr.inria.corese.core.query;

import java.util.Collection;
import java.util.HashMap;

import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.sparql.triple.parser.URLServer;

/**
 *
 */
public class StorageFactory {

    private HashMap<String, DataManager> map;
    private static StorageFactory singleton;

    static {
        setSingleton(new StorageFactory());
    }

    public StorageFactory() {
        setMap(new HashMap<>());
    }

    public static void defineDataManager(String id, DataManager man) {
        defineDataManager(new URLServer(id), man);
    }

    public static void defineDataManager(URLServer id, DataManager man) {
        getSingleton().getMap().put(id.getServer(), man);
        man.getCreateMetadataManager();
        man.start(id.getMap());
        if (man.hasMetadataManager()) {
            man.getMetadataManager().startDataManager();
        }
    }

    public static DataManager getDataManager(String id) {
        return getSingleton().getMap().get(id);
    }

    public static Collection<DataManager> getDataManagerList() {
        return getSingleton().getMap().values();
    }

    public HashMap<String, DataManager> getMap() {
        return map;
    }

    public void setMap(HashMap<String, DataManager> map) {
        this.map = map;
    }

    public static StorageFactory getSingleton() {
        return singleton;
    }

    public static void setSingleton(StorageFactory aSingleton) {
        singleton = aSingleton;
    }

}
