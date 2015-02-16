package test.persistent;

import fr.inria.acacia.corese.persistent.api.IOperation;
import fr.inria.acacia.corese.persistent.api.PersistentManager;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.load.Load;


/**
 * LoadLiteralsTest.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 9 févr. 2015
 */
public class LoadLiteralsTest {

    public static void main(String[] args){
        String path = "/Users/fsong/NetBeansProjects/bsbm/data/scale100.ttl";
         //path = "/Users/fsong/Documents/nquads.nq";
        Graph g = Graph.create();
        g.setPersistent(IOperation.STORAGE_FILE, null);
        Load l = Load.create(g);
        l.load(path);
        System.out.println(g);
        System.out.println(PersistentManager.getManager(IOperation.STORAGE_FILE));
    }
}
