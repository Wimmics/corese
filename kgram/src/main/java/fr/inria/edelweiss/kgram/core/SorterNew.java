package fr.inria.edelweiss.kgram.core;

import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.sorter.core.BPGraph;
import fr.inria.edelweiss.kgram.sorter.core.IEstimateSelectivity;
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

    public void sort(Exp exp, Producer prod, List<Exp> parameters) {
        if (exp.size() < 2) {
            return;
        }

        if (print) {
            System.out.println("##EXP BEFORE:" + exp);
        }
        long start = System.currentTimeMillis();
        
        //choose one
        //this.sortWithStats(exp, prod, parameters);
        this.sortWithoutStats(exp, prod, parameters);
        
        long end = System.currentTimeMillis();
        if (print) {
            System.out.println("##EXP AFTER:" + exp);
            System.out.println("##Optimize time (total):" + (end - start) + "ms");
        }
    }

    public void sortWithoutStats(Exp exp, Producer prod, List<Exp> bindings) {
        //1 create graph (list of nodes and their relations)
        BPGraph bpg = new BPGraph(exp);

        // 2 assign selectivity
        IEstimateSelectivity ies = new HeuristicsBasedEstimation();
        ies.estimate(bpg, prod, bindings);

        //3 find order
        ISort is = new SortBySelectivity();
        List l = is.sort(bpg);

        //4 rewrite
        is.rewrite(exp, l);
    }

    public void sortWithStats(Exp exp, Producer prod, List<Exp> bindings) {

        //1 create graph (list of nodes and their relations)
        BPGraph bpg = new BPGraph(exp);

        // 2 assign selectivity
        IEstimateSelectivity ies = new StatsBasedEstimation();
        ies.estimate(bpg, prod, bindings);

        //3 find order
        ISort is = new SortBySelectivity();
        List l = is.sort(bpg);

        //4 rewrite
        is.rewrite(exp, l);
    }
}
