package fr.inria.edelweiss.kgram.core;

import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.sorter.core.BPGraph;
import fr.inria.edelweiss.kgram.sorter.core.IEstimateSelectivity;
import fr.inria.edelweiss.kgram.sorter.core.ISort;
import fr.inria.edelweiss.kgram.sorter.SortBySelectivity;
import fr.inria.edelweiss.kgram.sorter.StatsBasedEstimation;
import java.util.List;

/**
 * A new sorter, based on triple pattern graph and selectivity
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 5 juin 2014
 */
public class SorterNew extends Sorter {

    public void sort(Exp exp, Producer prod) {
        if (exp.size() < 2) {
            return;
        }

        boolean print = false;

        if (print) {
            System.out.println("##EXP BEFORE:" + exp);
        }
        long start = System.currentTimeMillis();
        //1 create graph (list of nodes and their relations)
        BPGraph bpg = new BPGraph(exp);
        long bk = System.currentTimeMillis();
        if (print) {
            System.out.println("##Exe time (create graph):" + (bk - start) + "ms");
        }
        
        // 2 assign selectivity
        IEstimateSelectivity ies = new StatsBasedEstimation();
        ies.estimate(bpg, prod);
        bk = System.currentTimeMillis();
        if (print) {
            System.out.println("##Exe time (estimate sel):" + (bk - start) + "ms");
        }

        //3 find order
        ISort is = new SortBySelectivity();
        List l = is.sort(bpg);
        bk = System.currentTimeMillis();
        if (print) {
            System.out.println("##Exe time (sorting):" + (bk - start) + "ms");
        }
        //4 rewrite
        is.rewrite(exp, l);
        //5 service
//        if (bpg.noOfService() > 0) {
//            service(exp, bpg.noOfService());
//        }

        long end = System.currentTimeMillis();
        if (print) {
            System.out.println("##EXP AFTER:" + exp);
            System.out.println("##Optimize time (total):" + (end - start) + "ms");
        }
    }
}
