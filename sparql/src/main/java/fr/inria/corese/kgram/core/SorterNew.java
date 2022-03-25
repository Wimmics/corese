package fr.inria.corese.kgram.core;

import static fr.inria.corese.kgram.api.core.ExpType.BIND;
import static fr.inria.corese.kgram.api.core.ExpType.GRAPH;
import fr.inria.corese.kgram.api.query.Producer;
import static fr.inria.corese.kgram.sorter.core.Const.plannable;
import fr.inria.corese.kgram.sorter.core.QPGraph;
import fr.inria.corese.kgram.sorter.core.IEstimate;
import fr.inria.corese.kgram.sorter.core.ISort;
import fr.inria.corese.kgram.sorter.core.QPGNode;
import fr.inria.corese.kgram.sorter.impl.qpv1.DepthFirstBestSearch;
import fr.inria.corese.kgram.sorter.impl.qpv1.HeuristicsBasedEstimation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A new sorter for QP
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 5 juin 2014
 */
public class SorterNew extends Sorter {

    boolean print = false;

    public void sort(Exp expression, List<Exp> bindings, Producer prod, int planType) {
        if (expression.size() < 2) return;

        Map<Integer, List<Exp>> ESGs = tokenize(expression);
        if (ESGs.isEmpty()) return;

        message("** Before sorting [whole]" + expression);
        long start = System.currentTimeMillis();

        // === Iterate each sub set and create ESG and sorting === 
        for (Entry<Integer, List<Exp>> entrySet : ESGs.entrySet()) {
            Integer startIndex = entrySet.getKey();
            List<Exp> exps = entrySet.getValue();

            message(" -- Sorting [before], " + exps.size() + " :" + exps);

            // ** 1 create graph (list of nodes and their edges) **
            QPGraph bpg = new QPGraph(exps, bindings);
            long stop1 = System.currentTimeMillis();

            // ** 2 estimate cost/selectivity **
            // ** 2.1 find the corresponding algorithm **
            IEstimate ies;
            switch (planType) {
                case Query.QP_HEURISTICS_BASED:
                    ies = new HeuristicsBasedEstimation();
                    break;
                default:
                    ies = new HeuristicsBasedEstimation();
            }

            // ** 2.2 estimate **
            ies.estimate(bpg, prod, bindings);

            // ** 3 sort and find the order **
            ISort is;
            switch (planType) {
                case Query.QP_HEURISTICS_BASED:
                default:
                    is = new DepthFirstBestSearch();
            }

            List l = is.sort(bpg);

            //** For the following case where a graph has a bound value, put the BIND before 
            //** the graph pattern (because normally we put BIND just after where its variable 
            //** is used.
            //** bind (<uri> as ?g)
            //** graph ?g {?x ?p ?y}
            List<QPGNode> graphs = bpg.getAllNodes(GRAPH);
            for (QPGNode graph : graphs) {
                List<QPGNode> linkedNodes = bpg.getLinkedNodes(graph);
                for (QPGNode bind : linkedNodes) {
                    if (bind.getType() == BIND) {
                        int iBind = l.indexOf(bind);
                        int iGraph = l.indexOf(graph);
                        if (iBind > iGraph) {
                            l.remove(bind);
                            l.add(iGraph, bind);
                        }
                    }
                }
            }

            message(" -- Sorting time:" + (System.currentTimeMillis() - stop1) + "ms");
            message(" -- Sorting [after] :" + exps+ "\n");
            
            // ** 4 rewrite **
            is.rewrite(expression, l, startIndex);
        }

        message("** After sorting [whole]:" + expression);
        message("** Query sorting time:" + (System.currentTimeMillis() - start) + "ms **\n");

    }

    // === Split one expression into several subsets that can be sorted
    //ex. T1, T2, F, OPT, T3, T4, VA, UNION, T5, T6 will be splited
    // (0, <T1, T2, F>)
    // (4, <T3, T4, VA>)
    // (8, <T5, T6>)
    private Map<Integer, List<Exp>> tokenize(Exp e) {
        List<Exp> aESG = new ArrayList<Exp>();
        Map<Integer, List<Exp>> ESGs = new LinkedHashMap<Integer, List<Exp>>();
        ESGs.put(0, aESG);

        // == 1. split the expressions
        List<Exp> exps = e.getExpList();
        for (int i = 0; i < exps.size(); i++) {
            Exp ee = exps.get(i);
            if (plannable(ee.type())) {
                if (i > 0 && !plannable(exps.get(i - 1).type())) {
                    aESG = new ArrayList<Exp>();
                    ESGs.put(i, aESG);
                }

                aESG.add(ee);
            }

            if (ee.type() == GRAPH) {

            }
        }

        // == 2.remove the ones containing less that 2 expressions
        Iterator<Entry<Integer, List<Exp>>> it = ESGs.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().size() < 2) {
                it.remove();
            }
        }

        return ESGs;
    }

    private void message(String msg) {
        if (print) {
            System.out.println(msg);
        }
    }
}
