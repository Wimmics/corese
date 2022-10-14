
package fr.inria.corese.core.query;

import fr.inria.corese.core.api.DataManager;
import fr.inria.corese.sparql.triple.parser.URLServer;
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
    
    // path may have parameter path?key=value
    // data manager recorded with path without parameter
    public static void defineDataManager(String path, DataManager man) {
         defineDataManager(new URLServer(path), man);   
    }
    
    public static void defineDataManager(URLServer url, DataManager man) {
        getSingleton().getMap().put(url.getServer(), man);
        man.getCreateMetadataManager();       
        man.start(url.getMap());
        man.getMetadataManager().startDataManager();
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
