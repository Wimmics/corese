package fr.inria.edelweiss.kgraph.persistent;

import fr.inria.acacia.corese.cg.datatype.persistent.IOperation;
import fr.inria.edelweiss.kgraph.persistent.ondisk.StringOnDiskManager;

/**
 * 
 * LiteralManager.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 29 janv. 2015
 */
public class PersistentManager {

    /**
     * Get manager instance by given storage type
     *
     * @param type
     * @return
     */
    public static IOperation getManager(int type) {
        switch (type) {
            case IOperation.STORAGE_FILE:
                return StringOnDiskManager.getInstance();
//            case DB:
//                return null;
            default:
                return StringOnDiskManager.getInstance();
        }
    }
}
