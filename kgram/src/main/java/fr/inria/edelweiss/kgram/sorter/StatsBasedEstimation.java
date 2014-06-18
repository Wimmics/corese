package fr.inria.edelweiss.kgram.sorter;

import fr.inria.edelweiss.kgram.sorter.core.BPGraph;
import fr.inria.edelweiss.kgram.sorter.core.BPGNode;
import fr.inria.edelweiss.kgram.sorter.core.IStatistics;
import fr.inria.edelweiss.kgram.sorter.core.IEstimateSelectivity;
import static fr.inria.edelweiss.kgram.api.core.ExpType.FILTER;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import static fr.inria.edelweiss.kgram.api.core.Node.OBJECT;
import fr.inria.edelweiss.kgram.api.query.Producer;
import static fr.inria.edelweiss.kgram.sorter.core.BPGNode.BOUND;
import static fr.inria.edelweiss.kgram.sorter.core.IStatistics.PREDICATE;
import static fr.inria.edelweiss.kgram.sorter.core.IStatistics.SUBJECT;
import java.util.List;

/**
 * An implementation for estimating the selectivity based on statistics data
 *
 * This implementation utilize simple multiplication of each variable (bound or
 * unbound) in a triple pattern, namely, sel(triple) = sel(s) * sel(p) * sel(o)
 *
 * (TODO: A more sophisticated implementation by considering jointed triple
 * pattern should be implemented to imporve the drawback of simple
 * multiplication)
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 19 mai 2014
 */
public class StatsBasedEstimation implements IEstimateSelectivity {

    private IStatistics meta;
    private BPGraph g;

    private final static double SEL_FILTER = 0.5;

    public StatsBasedEstimation() {
        //todo 
        //this();
    }

    @Override
    public void estimate(BPGraph g, Producer producer) {
        //**1 check the producer
        if (producer instanceof IStatistics) {
            this.meta = (IStatistics) producer;
        } else {
            System.err.println("!! Producer type not compitable, cannot get statstics data !!");
            return;
        }

        this.g = g;

        //**2 iterate the nodes and assign selectivity
        for (BPGNode node : g.getNodeList()) {
            double sel = selTriplePattern(node);
            node.setSelectivity(sel);
        }

        //**3 add the selectivity of filter to linked variables
        for (BPGNode node : g.getNodeList()) {
            if (node.getType() == FILTER) {
                double sf = this.getSel(node.getExp().getFilter());
                node.setSelectivity(sf);
                for (BPGNode n : this.g.getGraph().get(node)) {
                    //set the new selectivity
                    n.setSelectivity(n.getSelectivity() * sf);
                }
            }
        }
    }

    private double selTriplePattern(BPGNode n) {
        int[] pattern = n.getPattern();
        if (pattern == null) {//not triples pattern, maybe filter
            return Integer.MAX_VALUE;
        }

        // if all variables in a triple pattern are bound, then selectiviy is set to app_0
        if (pattern[0] + pattern[1] + pattern[2] == BOUND) {
            return MIN_SEL_APP;
        }

        // selectiviy 
        double ss = getSel(n.getSubject(), SUBJECT);
        double sp = getSel(n.getPredicate(), PREDICATE);
        double so = getSel(n.getObject(), OBJECT);

        return ss * sp * so;
    }

    private double getSel(Node varNode, int type) {
        double sel = MAX_SEL;
        if (!varNode.isVariable()) {
            sel = getSel(varNode.getLabel(), type);
        } else {
            List<String> bind = this.g.isBound(varNode);
            if (bind != null) {
                sel = getSel(bind, type);
            }
        }
        return sel;
    }

    private double getSel(List<String> varibles, int type) {
        double sel = 0;
        for (String v : varibles) {
            sel += getSel(v, type);
        }
        return sel;
    }

    //Get selectivity for a given String(node) and its variable type
    private double getSel(String node, int type) {
        return meta.getCountByValue(node, type) * 1.0 / meta.getAllTriplesNumber();
    }

    //Get selectivity for a filter
    private double getSel(Filter f) {
        //todo: find a way to evaluate the selectivity of filter
        return SEL_FILTER;
    }
}
