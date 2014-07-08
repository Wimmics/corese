package fr.inria.edelweiss.kgram.core;

import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.sorter.core.BPGraph;
import fr.inria.edelweiss.kgram.sorter.core.IEstimate;
import fr.inria.edelweiss.kgram.sorter.core.ISort;
import fr.inria.edelweiss.kgram.sorter.impl.HeuristicsBasedEstimation;
import fr.inria.edelweiss.kgram.sorter.impl.SortBySelectivity;
import fr.inria.edelweiss.kgram.sorter.impl.StatsBasedEstimation;
import java.util.List;

/**
 * A new sorter, based on triple pattern graph and selectivity
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 5 juin 2014
 */
public class SorterNew extends Sorter {

    boolean print = false;

    public void sort(Exp exp, Producer prod, Object utility, boolean stats) {
        if(exp.size()<2) return;
        
        long start = System.currentTimeMillis();
        //1 create graph (list of nodes and their relations)
        BPGraph bpg = new BPGraph(exp);

        // 2 assign selectivity
        IEstimate ies = new HeuristicsBasedEstimation();
        if(stats) ies = new StatsBasedEstimation();
        
        ies.estimate(bpg, prod, utility);

        //3 find order
        ISort is = new SortBySelectivity();
        List l = is.sort(bpg);

        //4 rewrite
        is.rewrite(exp, l);
        if(print) System.out.println("== Query sorting time:" + (System.currentTimeMillis() - start) + "ms ==");
    }
}
