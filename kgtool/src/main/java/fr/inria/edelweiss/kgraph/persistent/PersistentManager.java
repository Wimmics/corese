package fr.inria.edelweiss.kgraph.persistent;

import fr.inria.acacia.corese.cg.datatype.persistent.IOperation;
import fr.inria.edelweiss.kgraph.persistent.ondisk.LiteralOnDiskManager;

/**
 * LiteralManager.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 29 janv. 2015
 */
public class PersistentManager {


    public static IOperation getManager(int type) {
        switch (type) {
            case IOperation.STORAGE_FILE:
                return LiteralOnDiskManager.getInstance();
//            case DB:
//                return null;
            default:
                return LiteralOnDiskManager.getInstance();
        }
    }
}
