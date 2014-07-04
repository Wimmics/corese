package fr.inria.edelweiss.kgram.sorter.impl;

import fr.inria.edelweiss.kgram.sorter.core.BPGraph;
import fr.inria.edelweiss.kgram.sorter.core.BPGNode;
import fr.inria.edelweiss.kgram.sorter.core.IEstimateSelectivity;
import static fr.inria.edelweiss.kgram.api.core.ExpType.FILTER;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import static fr.inria.edelweiss.kgram.api.core.Node.OBJECT;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.sorter.core.BPGEdge;
import fr.inria.edelweiss.kgram.sorter.core.IProducer;
import static fr.inria.edelweiss.kgram.sorter.core.IProducer.NA;
import static fr.inria.edelweiss.kgram.sorter.core.IProducer.PREDICATE;
import static fr.inria.edelweiss.kgram.sorter.core.IProducer.SUBJECT;
import fr.inria.edelweiss.kgram.sorter.core.TriplePattern;
import java.util.ArrayList;
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

    private IProducer meta;
    private BPGraph g;
    private List<Exp> bindings;

    private final static double SEL_FILTER = 1.0;

    public StatsBasedEstimation() {
        //todo 
        //this();
    }

    @Override
    public void estimate(BPGraph g, Producer producer, Object utility) {
        //**1 check the producer
        if (producer instanceof Producer) {
            this.meta = (IProducer) producer;
            if (!this.meta.enabled()) {
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
        for (BPGNode node : g.getNodeList()) {
            double sel = selTriplePattern(node);
            node.setSelectivity(sel);
        }

        //**3 add the selectivity of filter to linked variables
        for (BPGNode node : g.getNodeList()) {
            if (node.getType() == FILTER) {
                double sf = this.getSel(node.getExp().getFilter());
                node.setSelectivity(sf);
                for (BPGNode n : this.g.getNodeList(node)) {
                    //set the new selectivity
                    n.setSelectivity(n.getSelectivity() * sf);
                }
            }
        }

        //** 4 assign weights to edges
        join();
    }

    private double selTriplePattern(BPGNode n) {
        TriplePattern pattern = n.getPattern();
        if (pattern == null) {//not triples pattern, maybe filter
            return Integer.MAX_VALUE;
        }

        //** patterns **
        // pat = 0 :(s p o)
        // pat = 1 :(s p ?) | (s ? o) | (? p o)
        // pat = 2 :(s ? ?) | (? ? o) | (? p ?)
        // pat = 3 :(? ? ?)
        // if all variables in a triple pattern are bound, then selectiviy is set to app_0
        switch (pattern.getUnboundNumber()) {
            case 0:
                return MIN_SEL_APP;
            case 1:
                return getSel(n);
            case 2:
                double ss = getSel(n.getSubject(), SUBJECT);
                double sp = getSel(n.getPredicate(), PREDICATE);
                double so = getSel(n.getObject(), OBJECT);

                //two of them equal to 1.0
                return ss * sp * so;
            case 3:
                return MAX_SEL;
            default:
                return NA;
        }
    }

    //Get selectivity according to a whole triple
    private double getSel(BPGNode n) {
        return meta.getCountByTriple(n.getExp().getEdge()) * 1.0 / meta.getAllTriplesNumber();
    }

    //Get selectivity by single variable and its type(sub, pre, obj)
    private double getSel(Node varNode, int type) {
        double sel = MAX_SEL;
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
    private List<Node> isBound(Node var) {
        for (Exp exp : bindings) {
            if (var.getLabel().equalsIgnoreCase(exp.get(0).getNode().getLabel())) {
                List<Node> lBind = new ArrayList<Node>();
                Object o = exp.getObject();
//                for (Expr ex : (List<Expr>) exp.getObject()) {
//                    lBind.add(ex.);
//                }
                return lBind;
            }
        }
        return null;
    }

    //Calculate the weight of an edge between two triple patterns
    //just simply multiply the selectivity of the two nodes, can be improved
    private void join() {
        for (BPGEdge edge : g.getEdgeList()) {
            double s1 = edge.get(0).getSelectivity();
            double s2 = edge.get(1).getSelectivity();
            edge.setWeight(s1 * s2);
        }
    }
}
