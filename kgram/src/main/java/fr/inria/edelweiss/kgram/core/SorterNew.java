package fr.inria.edelweiss.kgram.core;

import static fr.inria.edelweiss.kgram.api.core.ExpType.EDGE;
import static fr.inria.edelweiss.kgram.api.core.ExpType.GRAPH;
import static fr.inria.edelweiss.kgram.api.core.ExpType.VALUES;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.sorter.core.BPGraph;
import fr.inria.edelweiss.kgram.sorter.core.IEstimate;
import fr.inria.edelweiss.kgram.sorter.core.ISort;
import fr.inria.edelweiss.kgram.sorter.impl.RuleBasedEstimation;
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

    public void sort(Exp exp, List<Exp> bindings, Producer prod, int planType) {
        //0 sortable?
        if (!sortable(exp)) {
            return;
        }
        message("Before sorting:" + exp);

        long start = System.currentTimeMillis();
        //1 create graph (list of nodes and their relations)
        BPGraph bpg = new BPGraph(exp, bindings);

        // 2 assign selectivity
        IEstimate ies ;
        switch (planType) {
            //case Query.PLAN_COMBINED:
            case Query.PLAN_RULE_BASED:
                ies = new RuleBasedEstimation();
                break;
            case Query.PLAN_STATS_BASED:
                ies = new StatsBasedEstimation();
                break;
            default:
                ies = new RuleBasedEstimation();
        }

        ies.estimate(bpg, prod, bindings);

        //3 find order
        ISort is = new SortBySelectivity();
        List l = is.sort(bpg);

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
    private boolean sortable(Exp e) {

        //TODO !!! find a solution when the conditions can not be satisfied..
        if (e.type() == Exp.AND && e.size() >1) {
           
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
    
    private void message(String msg){
        if (print) System.out.println(msg);
    }
}
