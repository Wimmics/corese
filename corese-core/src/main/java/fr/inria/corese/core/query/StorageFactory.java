
package fr.inria.corese.core.query;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.api.DataManager;
import fr.inria.corese.core.producer.DataProducer;
import fr.inria.corese.sparql.triple.parser.URLServer;
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
    
    public static void defineDataManager(String url, DataManager man) {
        getSingleton().getMap().put(url, man);
    }
    
    public static DataManager getDataManager(String url) {
        return getSingleton().getMap().get(url);
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
