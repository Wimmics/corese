package fr.inria.edelweiss.kgram.core;

import static fr.inria.edelweiss.kgram.api.core.ExpType.EDGE;
import static fr.inria.edelweiss.kgram.api.core.ExpType.GRAPH;
import static fr.inria.edelweiss.kgram.api.core.ExpType.VALUES;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.sorter.core.QPGraph;
import fr.inria.edelweiss.kgram.sorter.core.IEstimate;
import fr.inria.edelweiss.kgram.sorter.core.ISort;
import fr.inria.edelweiss.kgram.sorter.impl.qpv1.DepthFirstBestSearch;
import fr.inria.edelweiss.kgram.sorter.impl.qpv1.HeuristicsBasedEstimation;
import java.util.List;

/**
 * A new sorter for QP
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 5 juin 2014
 */
public class SorterNew extends Sorter {

    boolean print = false;

    public void sort(Exp exp, List<Exp> bindings, Producer prod, int planType) {
        //0 sortable?
        if (!sortable(exp)) {
            return;
        }
        message("Before sorting:" + exp);

        long start = System.currentTimeMillis();
        //1 create graph (list of nodes and their edges)
        QPGraph bpg = new QPGraph(exp, bindings);
        long stop1 = System.currentTimeMillis();
        message("==BPG creating time:" + (stop1 - start) + "ms");
        // 2 estimate cost/selectivity
        // 2.1 find the corresponding algorithm

        IEstimate ies;
        switch (planType) {
            case Query.QP_HEURISTICS_BASED:
            default:
                ies = new HeuristicsBasedEstimation();
        }

        // 2.2 estimate
        ies.estimate(bpg, prod, bindings);
        long stop2 = System.currentTimeMillis();
        message("==Cost estimation time:" + (stop2 - stop1) + "ms");

        //3 sort and find the order
        ISort is ;
         switch (planType) {
            case Query.QP_HEURISTICS_BASED:
            default:
                is = new DepthFirstBestSearch();
        }
         
        List l = is.sort(bpg);
        long stop3 = System.currentTimeMillis();
        message("==Sorting time:" + (stop3 - stop2) + "ms");

        //4 rewrite
        is.rewrite(exp, l);
        message("After sorting:" + exp);
        message("== Query sorting time:" + (System.currentTimeMillis() - start) + "ms ==");
    }

    /**
     * Check weather an expression can be sorted, now only support sorting AND
     * with sub-expression: FILTER, EDGE, VALUES and length >= 2
     *
     * @param e expression to be sorted
     * @return
     */
    public boolean sortable(Exp e) {
        if (e.type() == Exp.AND && e.size() > 1) {

            //check all sub expression type
            for (Exp ee : e) {
                if (!(ee.type() == Exp.FILTER || ee.type() == EDGE || ee.type() == VALUES || ee.type() == GRAPH)) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    private void message(String msg) {
        if (print) {
            System.out.println(msg);
        }
    }
}
