package fr.inria.corese.core.stats;

import fr.inria.corese.kgram.sorter.impl.qpv1.QPGNodeCostModel;
import fr.inria.corese.kgram.sorter.core.QPGraph;
import fr.inria.corese.kgram.sorter.core.QPGNode;
import fr.inria.corese.kgram.sorter.core.IEstimate;
import static fr.inria.corese.kgram.api.core.ExpType.FILTER;
import fr.inria.corese.kgram.api.core.Filter;
import fr.inria.corese.kgram.api.core.Node;
import static fr.inria.corese.kgram.api.core.Node.OBJECT;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.sorter.core.QPGEdge;
import fr.inria.corese.kgram.sorter.impl.qpv1.BasicPatternGenerator;
import static fr.inria.corese.core.stats.IStats.PREDICATE;
import static fr.inria.corese.core.stats.IStats.SUBJECT;
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
 * @deprecated stats-based QP is not supported and not recommended to use 22 Oct. 2014
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 19 mai 2014
 */
public class StatsBasedEstimation implements IEstimate {
    
    private IStats meta;
    private QPGraph g;
    private List<Exp> bindings;
    
    private final static double SEL_FILTER = 1.0;
    
    public StatsBasedEstimation() {
        //todo 
        //this();
    }
    
    public StatsBasedEstimation(IStats meta) {
        if (!this.meta.statsEnabled()) {
            System.err.println("!! Meta deta statistics not enabled, unable to estimate selectivity and sort !!");
            return;
        }
        this.meta = meta;
    }
    
    @Override
    public void estimate(QPGraph g, Producer producer, Object utility) {
        //**1 check the producer
        if (producer instanceof IStats) {
            this.meta = (IStats) producer;
            if (!this.meta.statsEnabled()) {
                System.err.println("!! Meta deta statistics not enabled, unable to estimate selectivity and sort !!");
                return;
            }
            
            if ((utility instanceof List)) {
                this.bindings = (List<Exp>) utility;
            } else {
                System.err.println("!! List of binding values are not available, unable to estimate selectivity !!");
                return;
            }
        } else {
            System.err.println("!! Producer type not compitable, cannot get statstics data !!");
            return;
        }
        
        this.g = g;

        //**2 iterate the nodes and assign selectivity
        for (QPGNode node : g.getAllNodes()) {
            double sel = selTriplePattern(node);
            node.setCost(sel);
        }

        //**3 add the selectivity of filter to linked variables
        //@deprecated, to be removed
        for (QPGNode node : g.getAllNodes()) {
            if (node.getType() == FILTER) {
                double sf = this.getSel(node.getExp().getFilter());
                node.setCost(sf);
                for (QPGNode n : this.g.getLinkedNodes(node)) {
                    //set the new selectivity
                    n.setCost(n.getCost() * sf);
                }
            }
        }

        //** 4 assign weights to edges
        join();
    }
    
    public double selTriplePattern(QPGNode n) {
        QPGNodeCostModel pattern = n.getCostModel();
        if (pattern == null) {//not triples pattern, maybe filter
            return Integer.MAX_VALUE;
        }

        // if all variables in a triple pattern are bound, then selectiviy is set to app_0
        switch (pattern.match(BasicPatternGenerator.generateBasicPattern())) {
            //{0, 0, 0}, {0, 1, 0}, {1, 0, 0}, {0, 0, 1}, {1, 1, 0}, {0, 1, 1}, {1, 0, 1}, {1, 1, 1}
            case 0:// {s p o}
                return MIN_COST_0;
            case 1://{s ? o}
                //if the value <0, means that the algorithm for evaluating the whole
                //triple is not available, so use the other method
                double p = getSel(n, PREDICATE);
                if (p > 0) {
                    return p;
                }
            case 2://{? p o}
                double s = getSel(n, SUBJECT);
                if (s > 0) {
                    return s;
                }
            case 3://{s p ?}
                double o = getSel(n, OBJECT);
                if (o > 0) {
                    return o;
                }
            case 4://{? ? o}
                return getSel(n.getExpNode(OBJECT), OBJECT);
            case 5://{s ?  d?}
                return getSel(n.getExpNode(SUBJECT), SUBJECT);
            case 6://{? p ?}
                return getSel(n.getExpNode(PREDICATE), PREDICATE);
            case 7://{? ? ?}
                return MAX_COST;
            default:
                return NA_COST;
        }
    }

    //Get selectivity according to a whole triple
    private double getSel(QPGNode n, int type) {
        return meta.getCountByTriple(n.getExp().getEdge(), type) * 1.0 / meta.getAllTriplesNumber();
    }

    //Get selectivity by single variable and its type(sub, pre, obj)
    private double getSel(Node varNode, int type) {
        double sel = MAX_COST;
        //1 the variable is bound
        if (!varNode.isVariable()) {
            sel = meta.getCountByValue(varNode, type) * 1.0 / meta.getAllTriplesNumber();
            //2 unbound variable, check if bound to a list of constant values
        }
        /*
         else {
         //if bound to some values, then cumulate the selectivity of each
         List<Node> lBind = this.isBound(varNode);
         if (lBind != null) {
         sel = 0;
         for (Node v : lBind) {
         sel += meta.getCountByValue(v, type) * 1.0 / meta.getAllTriplesNumber();
         }
         }
         }*/
        return sel;
    }

    //Get selectivity for a filter
    private double getSel(Filter f) {
        //todo: find a way to evaluate the selectivity of filter
        return SEL_FILTER;
    }

    // todo
//    private List<Node> isBound(Node var) {
//        for (Exp exp : bindings) {
//            if (var.getLabel().equalsIgnoreCase(exp.get(0).getNode().getLabel())) {
//                List<Node> lBind = new ArrayList<Node>();
//                Object o = exp.getObject();
////                for (Expr ex : (List<Expr>) exp.getObject()) {
////                    lBind.add(ex.);
////                }
//                return lBind;
//            }
//        }
//        return null;
//    }
    //Calculate the weight of an edge between two triple patterns
    //just simply multiply the selectivity of the two nodes, can be improved
    private void join() {
//        for (QPGEdge edge : g.getAllEdges()) {
//            edge.setCost(1.0);
////            double s1 = edge.get(0).getSelectivity();
////            double s2 = edge.get(1).getSelectivity();
////            edge.setWeight(s1 * s2);
//        }
    }
}
