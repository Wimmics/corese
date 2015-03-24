package test.persistent;

import fr.inria.acacia.corese.cg.datatype.CoreseURIHelper;
import fr.inria.acacia.corese.persistent.api.IOperation;
import fr.inria.acacia.corese.persistent.api.Parameters;
import fr.inria.acacia.corese.persistent.ondisk.StringOnDiskManager;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.load.Load;

/**
 * TestLoadFile.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 25 févr. 2015
 */
public class TestLoadFile {

    public static void main(String[] args) {
        Graph g = Graph.create();
        Parameters p = Parameters.create();
        p.add(Parameters.type.MAX_LIT_LEN, 100);
        g.setPersistent(IOperation.STORAGE_FILE, p);
        Load ld = Load.create(g);
        ld.load("/Users/fsong/NetBeansProjects/bsbm/data/scale100.ttl");
        //System.out.println(g);
        System.out.println(((StringOnDiskManager) g.getPersistent()).getLiteralsOnDiskSize() * 1.0d);
        System.out.println(CoreseURIHelper.getInstance().toString());
    }
}
