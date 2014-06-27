package fr.inria.edelweiss.kgram.sorter.core;

import fr.inria.edelweiss.kgram.api.core.Edge;
import static fr.inria.edelweiss.kgram.api.core.ExpType.EDGE;
import static fr.inria.edelweiss.kgram.api.core.ExpType.FILTER;
import static fr.inria.edelweiss.kgram.api.core.ExpType.VALUES;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Exp;
import java.util.List;

/**
 * The node for triple pattern graph, which encapsualtes an expression (contain
 * an object exp) with selectivity
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 19 mai 2014
 */
public class BPGNode {

    public final static int BOUND = 0, UNBOUND = 1;

    // the expression that the node encapsulates
    private final Exp exp;
    //the value of the selectivity that the expression represents
    private double selectivity = -1;

    private TriplePattern pattern = null;

    //private Iestimate
    public BPGNode(Exp exp) {
        this.exp = exp;

        if (exp.type() == EDGE) {
            Edge e = exp.getEdge();

            int s = e.getNode(0).isVariable() ? UNBOUND : BOUND;
            int p = e.getEdgeNode().isVariable() ? UNBOUND : BOUND;
            int o = e.getNode(1).isVariable() ? UNBOUND : BOUND;
            pattern = new TriplePattern(this, s, p, o);
        } else {
            //set the selectivity of filter very big
            //so that it can be postioned at the end 
            this.selectivity = Integer.MAX_VALUE;
        }
    }

    public TriplePattern getPattern() {
        return pattern;
    }

    public Exp getExp() {
        return this.exp;
    }

    public int getType() {
        return this.exp.type();
    }

    public Node getSubject() {
        return getType() == EDGE ? this.exp.getEdge().getNode(0) : null;
    }

    public Node getPredicate() {
        return getType() == EDGE ? this.exp.getEdge().getEdgeNode() : null;
    }

    public Node getObject() {
        return getType() == EDGE ? this.exp.getEdge().getNode(1) : null;
    }

    public double getSelectivity() {
        return selectivity;
    }

    public void setSelectivity(double selectivity) {
        this.selectivity = selectivity;
    }

    /**
     * Check if two BP node share same variables
     *
     * @param n BP node
     * @return true: shared; false: not share
     */
    public boolean isShared(BPGNode n) {
        return isShared(this, n);
    }

    public boolean isShared(BPGNode bpn1, BPGNode bpn2) {
        int type1 = bpn1.exp.type();
        int type2 = bpn2.exp.type();

        switch (type1) {
            case EDGE:
                switch (type2) {
                    case EDGE:
                        return isShared(bpn1.exp.getEdge(), bpn2.exp.getEdge());
                    default: ;
                }
                break;
            case FILTER:
                switch (type2) {
                    case EDGE:
                        return isShared(bpn1.exp.getFilter(), bpn2.exp.getEdge());
                    default: ;
                }
                break;
            case VALUES:
                switch (type2) {
                    case EDGE:
                        return this.isShared(bpn1.exp.getNodeList(), bpn2.exp.getEdge());
                    default:
                }
            default:
                break;
        }

        return false;
    }

    public boolean isShared(List<Node> values, Edge e) {
        Node n0 = e.getEdgeNode();
        Node n1 = e.getNode(0);
        Node n2 = e.getNode(1);

        for (Node node : values) {
            String var = node.getLabel();
            if (var.equalsIgnoreCase(n0.toString())
                    || var.equalsIgnoreCase(n1.toString())
                    || var.equalsIgnoreCase(n2.toString())) {
                return true;
            }
        }

        return false;
    }

    //check between edge and filter
    public boolean isShared(Filter f, Edge e) {
        Node n0 = e.getEdgeNode();
        Node n1 = e.getNode(0);
        Node n2 = e.getNode(1);

        //get list of variable names
        List<String> vars = f.getVariables();
        for (String var : vars) {
            if (var.equalsIgnoreCase(n0.toString())
                    || var.equalsIgnoreCase(n1.toString())
                    || var.equalsIgnoreCase(n2.toString())) {
                return true;
            }
        }

        return false;
    }

    //check between two edges
    public boolean isShared(Edge e1, Edge e2) {
        Node n0 = e2.getEdgeNode();
        Node n1 = e2.getNode(0);
        Node n2 = e2.getNode(1);

        return e1.getEdgeNode().same(n0) || e1.contains(n1) || e1.contains(n2);
    }

    /**
     * Check if two types of node can be compared and connected
     *
     * @param n
     * @return
     */
    public boolean isCompitable(BPGNode n) {
        if (n == null) {
            return false;
        }
        if (this.exp.type() == EDGE && n.exp.type() == EDGE) {
            return true;
        } else if (this.exp.type() == FILTER && n.exp.type() == EDGE) {
            return true;
        } else if (this.exp.type() == VALUES && n.exp.type() == EDGE) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return exp.toString() + "," + this.selectivity;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BPGNode)) {
            return false;
        } else {
            return this.exp.equals(((BPGNode) obj).exp);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.exp != null ? this.exp.hashCode() : 0);
        return hash;
    }
}
