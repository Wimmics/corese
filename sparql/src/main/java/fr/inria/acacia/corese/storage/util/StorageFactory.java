package fr.inria.acacia.corese.storage.util;

import fr.inria.acacia.corese.storage.api.IStorage;
import fr.inria.acacia.corese.storage.api.Parameters;
import fr.inria.acacia.corese.storage.fs.StringManager;

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
