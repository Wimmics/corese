package fr.inria.corese.sparql.storage.util;

import fr.inria.corese.sparql.storage.api.IStorage;
import fr.inria.corese.sparql.storage.api.Parameters;
import fr.inria.corese.sparql.storage.fs.StringManager;

/**
 * Generate the persistent manager
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 29 janv. 2015
 */
public class StorageFactory {

    /**
     * create storage manager instance by given storage type
     *
     * @param type
     * @param params
     * @return
     */
    public static IStorage create(int type, Parameters params) {
        switch (type) {
            case IStorage.STORAGE_FILE:
                return StringManager.create(params);
            default:
                return StringManager.create();
        }
    }

    /**
     * Create persistent manager using default parameters
     * 
     * @param type
     * @return 
     */
    public static IStorage create(int type) {
        return create(type, null);
    }
}
