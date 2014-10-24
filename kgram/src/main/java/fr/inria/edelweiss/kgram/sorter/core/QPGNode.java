package fr.inria.edelweiss.kgram.sorter.core;

import fr.inria.edelweiss.kgram.sorter.impl.qpv1.QPGNodeCostModel;
import fr.inria.edelweiss.kgram.api.core.Edge;
import static fr.inria.edelweiss.kgram.api.core.ExpType.EDGE;
import static fr.inria.edelweiss.kgram.api.core.ExpType.FILTER;
import static fr.inria.edelweiss.kgram.api.core.ExpType.GRAPH;
import static fr.inria.edelweiss.kgram.api.core.ExpType.NODE;
import static fr.inria.edelweiss.kgram.api.core.ExpType.VALUES;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Exp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * The node for triple pattern graph, which encapsualtes an expression (contain
 * an object exp) with cost
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 19 mai 2014
 */
public class QPGNode {

    public final static int S = 0, P = 1, O = 2;
    // the expression that the node encapsulates
    private final Exp exp;
    private final int type;
    private QPGNodeCostModel costModel = null;
    private double cost = -1;

    public QPGNode(Exp exp, List<Exp> bindings) {
        this.exp = exp;
        this.type = exp.type();
        this.costModel = new QPGNodeCostModel(this, exp.getEdge(), bindings);
    }

    public QPGNodeCostModel getCostModel() {
        return costModel;
    }

    public Exp getExp() {
        return this.exp;
    }

    public int getType() {
        return this.type;
    }

    public Node get(int i) {
        switch (i) {
            case S:
                return getSubject();
            case P:
                return getPredicate();
            case O:
                return getObject();
            default:
                return null;
        }
    }

    public Node getSubject() {
        return getType() == EDGE ? this.exp.getEdge().getNode(0) : null;
    }

    public Node getPredicate() {
        Edge e = this.exp.getEdge();
        return getType() == EDGE ? (e.getEdgeVariable() == null ? e.getEdgeNode() : e.getEdgeVariable()) : null;
    }

    public Node getObject() {
        return getType() == EDGE ? this.exp.getEdge().getNode(1) : null;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    /**
     * Check if two QPG node share same variables
     *
     * @param n BP node
     * @return true: shared; false: not share
     */
    public boolean isShared(QPGNode n) {
        return this.shared(n).size() > 0;
    }

    public List<String> shared(QPGNode n) {
        return this.shared(this, n);
    }

    public List<String> shared(QPGNode bpn1, QPGNode bpn2) {
        int type1 = bpn1.exp.type();
        int type2 = bpn2.exp.type();

        switch (type1) {
            case EDGE:
                switch (type2) {
                    case EDGE:
                        return this.isShared(bpn1.exp.getEdge(), bpn2.exp.getEdge());
                    case GRAPH:
                        return this.isShared(bpn2.exp, bpn1.exp.getEdge());
//                    case FILTER:
//                        return this.isShared(bpn2.exp.getFilter(), bpn1.exp.getEdge());
//                    case VALUES:
//                        return this.isShared(bpn2.exp.getNodeList(), bpn1.exp.getEdge());
                    default: ;
                }
                break;
            case FILTER:
                switch (type2) {
                    case EDGE:
                        return this.isShared(bpn1.exp.getFilter(), bpn2.exp.getEdge());
                    case GRAPH:
                        return this.isShared(bpn1.exp.getFilter(), bpn2.exp);
                    default: ;
                }
                break;
            case VALUES:
                switch (type2) {
                    case EDGE:
                        return this.isShared(bpn1.exp.getNodeList(), bpn2.exp.getEdge());
                    case GRAPH:
                        return this.isShared(bpn1.exp.getNodeList(), bpn2.exp);
                    default:;
                }
            case GRAPH:
                switch (type2) {
                    case EDGE:
                        return this.isShared(bpn1.exp, bpn2.exp.getEdge());
                    case GRAPH:
                        return this.isShared(bpn1.exp, bpn2.exp);
//                    case FILTER:
//                        return this.isShared(bpn2.exp.getFilter(), bpn1.exp);
//                    case VALUES:
//                        return this.isShared(bpn2.exp.getNodeList(), bpn1.exp);
                    default:;
                }
            default:
                break;
        }

        return new ArrayList();
    }

    //check between edge and values
    public List<String> isShared(List<Node> values, Edge e) {
        return this.compare(values, getVariablesInEdge(e));
    }

    //check between edge and filter
    public List<String> isShared(Filter f, Edge e) {
        return this.compare2(f.getVariables(), getVariablesInEdge(e));
    }

    //check between two edges
    public List<String> isShared(Edge e1, Edge e2) {
        return this.compare(getVariablesInEdge(e1), getVariablesInEdge(e2));
    }

    //check between edge and graph
    public List<String> isShared(Exp graph, Edge e) {
        return this.compare(getVariablesInEdge(e), getVariablesInGraph(graph));
    }

    //check between two graphs
    public List<String> isShared(Exp g1, Exp g2) {
        return this.compare(getVariablesInGraph(g1), getVariablesInGraph(g2));
    }

    //check between graph and values
    public List<String> isShared(List<Node> values, Exp graph) {
        return this.compare(values, getVariablesInGraph(graph));
    }

    //check between graph and filter
    public List<String> isShared(Filter f, Exp graph) {
        return this.compare2(f.getVariables(), getVariablesInGraph(graph));
    }

    private List<Node> getVariablesInEdge(Edge e) {
        List<Node> l = new ArrayList<Node>();
        if (e.getNode(0).isVariable()) {
            l.add(e.getNode(0));
        }
        if (e.getEdgeVariable() != null) {
            l.add(e.getEdgeVariable());
        }
        if (e.getNode(1).isVariable()) {
            l.add(e.getNode(1));
        }

        //remove duplicated items
        HashSet h = new HashSet(l);
        l.clear();
        l.addAll(h);

        return l;
    }

    //GRAPH{GRAPHNODE{NODE{data:aliceFoaf } } AND{EDGE{?alice foaf:mbox <mailto:alice@work.example>} ...}}
    private List<Node> getVariablesInGraph(Exp graph) {
        //todo
        List<Node> l = new ArrayList<Node>();
        for (Exp e : graph) {
            for (Exp ee : e) {
                if (ee.type() == NODE) {
                    l.add(ee.getNode());
                }
                if (ee.type() == EDGE) {
                    l.addAll(getVariablesInEdge(ee.getEdge()));
                }
            }
        }
        //remove duplicated items
        HashSet h = new HashSet(l);
        l.clear();
        l.addAll(h);

        return l;
    }

    private List<String> compare2(List<String> l1, List<Node> l2) {
        List<String> l = new ArrayList<String>();
        for (String n1 : l1) {
            for (Node n2 : l2) {
                if (n1.equalsIgnoreCase(n2.getLabel())) {
                    l.add(n1);
                    break;
                }
            }
        }

        return l;
    }

    private List<String> compare(List<Node> l1, List<Node> l2) {
        List<String> l = new ArrayList<String>();
        for (Node n1 : l1) {
            for (Node n2 : l2) {
                if (n1.same(n2)) {
                    l.add(n1.getLabel());
                    break;
                }
            }
        }

        return l;
    }

    @Override
    public String toString() {
        return exp.toString() + "," + this.cost;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof QPGNode)) {
            return false;
        } else {
            return this.exp.equals(((QPGNode) obj).exp);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.exp != null ? this.exp.hashCode() : 0);
        return hash;
    }
}
