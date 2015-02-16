package fr.inria.acacia.corese.persistent.api;

import fr.inria.acacia.corese.persistent.ondisk.StringOnDiskManager;

/**
 * Generate the persistent manager
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 29 janv. 2015
 */
public class PersistentFactory {

    /**
     * create manager instance by given storage type
     *
     * @param type
     * @param params
     * @return
     */
    public static IOperation create(int type, Parameters params) {
        switch (type) {
            case IOperation.STORAGE_FILE:
                return StringOnDiskManager.create(params);
            default:
                return StringOnDiskManager.create();
        }
    }

    /**
     * Create persistent manager using default parameters
     * 
     * @param type
     * @return 
     */
    public static IOperation create(int type) {
        return create(type, null);
    }

//    /**
//     * Get the lastest created manager instance
//     * 
//     * @param type
//     * @return 
//     */
//    public static IOperation getManager(int type) {
//        switch (type) {
//            case IOperation.STORAGE_FILE:
//                return StringOnDiskManager.getInstance();
//            default:
//                return StringOnDiskManager.getInstance();
//        }
//    }
}
