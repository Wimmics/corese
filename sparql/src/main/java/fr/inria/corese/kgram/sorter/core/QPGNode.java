package fr.inria.corese.kgram.sorter.core;

import fr.inria.corese.kgram.sorter.impl.qpv1.QPGNodeCostModel;
import fr.inria.corese.kgram.api.core.Edge;
import static fr.inria.corese.kgram.api.core.ExpType.BIND;
import static fr.inria.corese.kgram.api.core.ExpType.EDGE;
import static fr.inria.corese.kgram.api.core.ExpType.FILTER;
import static fr.inria.corese.kgram.api.core.ExpType.GRAPH;
import static fr.inria.corese.kgram.api.core.ExpType.NODE;
import static fr.inria.corese.kgram.api.core.ExpType.VALUES;
import fr.inria.corese.kgram.api.core.Filter;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Exp;
import static fr.inria.corese.kgram.sorter.core.Const.OBJECT;
import static fr.inria.corese.kgram.sorter.core.Const.PREDICATE;
import static fr.inria.corese.kgram.sorter.core.Const.SUBJECT;
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

    // the expression that the node encapsulates
    private final Exp exp;
    private final int type;
    private QPGNodeCostModel costModel = null;
    private double cost = -1;
    //the nested QPG in a QPG node, ex, GRAPH
    //and for future extionson, ex, UNION
    //private QPGraph nested = null;

    public QPGNode(Exp exp, List<Exp> bindings) {
        this.exp = exp;
        this.type = exp.type();

        this.costModel = new QPGNodeCostModel(this, bindings);
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

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    /**
     * Get the Kgram Node in the triple pattern (EDGE) according to the type
     * subject, predicate, or object
     *
     * @param i type
     * @return
     */
    public Node getExpNode(int i) {
        if (this.type != EDGE) {
            return null;
        }

        switch (i) {
            case SUBJECT:
                return this.exp.getEdge().getNode(0);
            case PREDICATE:
                Edge e = this.exp.getEdge();
                return (e.getEdgeVariable() == null ? e.getEdgeNode() : e.getEdgeVariable());
            case OBJECT:
                return this.exp.getEdge().getNode(1);
            default:
                return null;
        }
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
                    case FILTER:
                        return this.isShared(bpn2.exp.getFilter(), bpn1.exp.getEdge());
                    case VALUES:
                        return this.isShared(bpn2.exp.getNodeList(), bpn1.exp.getEdge());
                    case BIND:
                        return this.isShared(bpn2.exp.getFilter(), bpn1.exp.getEdge());
                    default: ;
                }
                break;
            case GRAPH:
                switch (type2) {
                    case EDGE:
                        return this.isShared(bpn1.exp, bpn2.exp.getEdge());
                    case GRAPH:
                        return this.isShared(bpn1.exp, bpn2.exp);
                    case FILTER:
                        return this.isShared(bpn2.exp.getFilter(), bpn1.exp);
                    case VALUES:
                        return this.isShared(bpn2.exp.getNodeList(), bpn1.exp);
                    case BIND:
                        if (bpn2.exp.hasNodeList()){
                             return this.isShared(bpn2.exp.getNodeList(), bpn1.exp);
                        }
                        else {
                            return this.isShared(bpn2.exp.getNode(), bpn1.exp);
                        }
                    default:;
                }
            case BIND:
                switch (type2) {
                    case FILTER:
                        return this.isShared(bpn2.exp.getFilter(), bpn1.exp.getNode());
                    case VALUES:
                        return this.isShared(bpn2.exp.getNodeList(), bpn1.exp.getNode());//td
                    case BIND:
                        if (bpn2.exp.hasNodeList()){
                            return this.compare(bpn2.exp.getNodeList(), bpn1.exp.getNode());
                        }
                        else {
                            return this.compare(bpn2.exp.getNode(), bpn1.exp.getNode());
                        }
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
        return this.compareString(f.getVariables(), getVariablesInEdge(e));
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
        return this.compareString(f.getVariables(), getVariablesInGraph(graph));
    }

    //check between bind and filter
    public List<String> isShared(Filter f, Node n) {
        return this.compareString(f.getVariables(), n);
    }

    //check between bind and values
    public List<String> isShared(List<Node> values, Node n) {
        return this.compare(values, n);
    }

    //check between graph and filter
    public List<String> isShared(Node n, Exp graph) {
        return this.compare(getVariablesInGraph(graph), n);
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

    //compare between a list of strings and a list of nodes
    private List<String> compareString(List<String> l1, List<Node> l2) {
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

    //compare between a list of strings and a list of nodes
    private List<String> compareString(List<String> l1, Node n) {
        List<Node> l2 = new ArrayList<Node>();
        l2.add(n);
        return compareString(l1, l2);
    }

    //compare between two lists of nodes
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

    //compare between a single node and a list of nodes
    private List<String> compare(List<Node> l2, Node n) {
        List<Node> l1 = new ArrayList<Node>();
        l1.add(n);
        return this.compare(l1, l2);
    }

    private List<String> compare(Node n1, Node n2) {
        List<String> l = new ArrayList<String>();
        if (n1.same(n2)) {
            l.add(n1.getLabel());
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
