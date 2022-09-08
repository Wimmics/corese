
package fr.inria.corese.core.query;

import fr.inria.corese.core.api.DataManager;
import java.util.Collection;
import java.util.HashMap;

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
    
    public static void defineDataManager(String path, DataManager man) {
        man.getCreateMetadataManager();
        getSingleton().getMap().put(path, man);
    }
    
    public static DataManager getDataManager(String path) {
        return getSingleton().getMap().get(path);
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
