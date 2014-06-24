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
public class BPGNode implements Node {

    public final static int BOUND = 0;
    public final static int UNBOUND = 1;
    public final static int NULL = -1;

    //same with the expression that it contains
    //now it has to be either EDGE or FILTER
    private final int type;
    int index = -1;
    // the expression that the node encapsulates
    private Exp exp;
    //the value of the selectivity that the expression represents
    private double selectivity = -1;
    //0: bound 1:unbound -1:undefined
    private int[] pattern = null;
    //the (s, p, o) node if the type == EDGE, otherwise is null
    private Node subject = null, predicate = null, object = null;

    //private Iestimate
    public BPGNode(Exp exp) {
        this.exp = exp;
        this.type = exp.type();

        if (exp.type() == EDGE) {
            Edge e = exp.getEdge();
            pattern = new int[5];
            this.pattern[0] = e.getNode(0).isVariable() ? UNBOUND : BOUND;
            this.pattern[1] = e.getEdgeNode().isVariable() ? UNBOUND : BOUND;
            this.pattern[2] = e.getNode(1).isVariable() ? UNBOUND : BOUND;

            this.subject = e.getNode(0);
            this.predicate = e.getEdgeNode();
            this.object = e.getNode(1);
        } else {
            //set the selectivity of filter very big
            //so that it can be postioned at the end 
            this.selectivity = Integer.MAX_VALUE;
        }
    }

    public int[] getPattern() {
        return pattern;
    }

    public void setPattern(int[] pattern) {
        this.pattern = pattern;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }

    public double getSelectivity() {
        return selectivity;
    }

    public void setSelectivity(double selectivity) {
        this.selectivity = selectivity;
    }

    public Exp getExp() {
        return this.exp;
    }

    public int getType() {
        return type;
    }

    public Node getSubject() {
        return subject;
    }

    public Node getPredicate() {
        return predicate;
    }

    @Override
    public Node getObject() {
        return object;
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
                    //case FILTER:
                    //do not link from edge to filter
                    // return false;
                    //return isShared(bpn1.exp.getEdge(), bpn2.exp.getFilter());
                    default:
                        ;
                }
                break;
            case FILTER:
                switch (type2) {
                    case EDGE:
                        return isShared(bpn1.exp.getFilter(), bpn2.exp.getEdge());
                    //case FILTER:
                    //do not link between filters
                    //return false;
                    //return isShared(bpn1.exp.getFilter(), bpn2.exp.getFilter());
                    default:
                        ;
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

    //check between two filters
    /**
     * public boolean isShared(Filter f1, Filter f2) { //get list of variable
     * names List<String> vars1 = f1.getVariables(); List<String> vars2 =
     * f2.getVariables(); for (String var1 : vars1) { for (String var2 : vars2)
     * { if (var1.equalsIgnoreCase(var2)) { return true; } } }
     *
     * return false; }
     */
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

    /**
     * **** Override methods for super classes, they are not actually used ****
     */
    @Override
    public String toString() {
        return exp.toString() + "," + this.selectivity;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int n) {
        this.index = n;
    }

    @Override
    public String getKey() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setKey(String str) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean same(Node n) {
        throw new UnsupportedOperationException("Not supported yet.");
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

    @Override
    public int compare(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLabel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isVariable() {
        return false;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public boolean isBlank() {
        return false;
    }

    @Override
    public boolean isFuture() {
        return false;
    }

    @Override
    public Object getValue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setObject(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getProperty(int p) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setProperty(int p, Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
