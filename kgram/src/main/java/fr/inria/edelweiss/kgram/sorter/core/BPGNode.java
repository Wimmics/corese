package fr.inria.edelweiss.kgram.sorter.core;

import fr.inria.edelweiss.kgram.api.core.Edge;
import static fr.inria.edelweiss.kgram.api.core.ExpType.AND;
import static fr.inria.edelweiss.kgram.api.core.ExpType.EDGE;
import static fr.inria.edelweiss.kgram.api.core.ExpType.FILTER;
import static fr.inria.edelweiss.kgram.api.core.ExpType.GRAPH;
import static fr.inria.edelweiss.kgram.api.core.ExpType.GRAPHNODE;
import static fr.inria.edelweiss.kgram.api.core.ExpType.VALUES;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Exp;
import static fr.inria.edelweiss.kgram.sorter.core.TriplePattern.O;
import static fr.inria.edelweiss.kgram.sorter.core.TriplePattern.P;
import static fr.inria.edelweiss.kgram.sorter.core.TriplePattern.S;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * The node for triple pattern graph, which encapsualtes an expression (contain
 * an object exp) with selectivity
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 19 mai 2014
 */
public class BPGNode {

    private final static int NAH = -1;
    // the expression that the node encapsulates
    private final Exp exp;
    //INFO: 
    //the smaller the value of unselectivity means the more selective the node
    //the bigger the value of selectivity means the more selective the node
    //unselectivity = 1-selectivity
    //ex, if unselectivity = 0.2 (selectivity = 0.8)
    private double unselectivity = NAH, selectivity = NAH;

    //tripe pattern ?-tuple (S P O G FV FN)
    private TriplePattern pattern = null;

    //private final BPGEdge edge = null;
    //private final Node gNode;
    //private Iestimate
    public BPGNode(Exp exp, List<Exp> bindings) {
        this.exp = exp;
        //this.gNode = gNode;

        if (exp.type() == EDGE) {
            pattern = new TriplePattern(this, exp.getEdge(), bindings);
        } else if (exp.type() == GRAPH) {
            //obtain the graph node
            if (exp.size() > 0 && exp.get(0).type() == GRAPHNODE) {
                //GRAPH{GRAPHNODE{NODE{data:aliceFoaf } } AND...}   
                Node gNode = exp.get(0).get(0).getNode();
                pattern = new TriplePattern(this, gNode, bindings);
            }
        } else {
            //set the unselectivity of filter very big
            //so that it can be postioned at the end 
            this.unselectivity = Integer.MAX_VALUE;
            this.selectivity = Integer.MIN_VALUE;
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

    public double getUnselectivity() {
        return unselectivity;
    }

    public void setUnselectivity(double unselectivity) {
        this.unselectivity = unselectivity;
        this.selectivity = 1 - this.unselectivity;
    }

    public double getSelectivity() {
        return selectivity;
    }

    public void setSelectivity(double selectivity) {
        this.selectivity = selectivity;
        this.unselectivity = 1 - this.selectivity;
    }

    /**
     * Check if two BP node share same variables
     *
     * @param n BP node
     * @return true: shared; false: not share
     */
    public boolean isShared(BPGNode n) {
        return this.shared(this, n).size() > 0;
    }

    public List<String> shared(BPGNode n) {
        return this.shared(this, n);
    }

    //!! to be checked / tested
    public List<String> shared(BPGNode bpn1, BPGNode bpn2) {
        int type1 = bpn1.exp.type();
        int type2 = bpn2.exp.type();

        switch (type1) {
            case EDGE:
                switch (type2) {
                    case EDGE:
                        return this.isShared(bpn1.exp.getEdge(), bpn2.exp.getEdge());
                    case GRAPH:
                        return this.isShared(bpn2.exp, bpn1.exp.getEdge());
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
                    default:;
                }
            case GRAPH:
                switch (type2) {
                    case EDGE:
                        return this.isShared(bpn1.exp, bpn2.exp.getEdge());
                    case GRAPH:
                        return this.isShared(bpn1.exp, bpn2.exp);
                    default:;
                }
            default:
                break;
        }

        return new ArrayList();
    }

    public List<String> isShared(List<Node> values, Edge e) {
        List<String> l = new ArrayList<String>();
        Node n0 = e.getEdgeVariable() == null ? e.getEdgeNode() : e.getEdgeVariable();
        Node n1 = e.getNode(0);
        Node n2 = e.getNode(1);

        for (Node node : values) {
            String var = node.getLabel();
            if (var.equalsIgnoreCase(n0.toString())
                    || var.equalsIgnoreCase(n1.toString())
                    || var.equalsIgnoreCase(n2.toString())) {
                l.add(var);
            }
        }

        return l;
    }

    //check between edge and filter
    public List<String> isShared(Filter f, Edge e) {
        List<String> l = new ArrayList<String>();
        Node n0 = e.getEdgeVariable() == null ? e.getEdgeNode() : e.getEdgeVariable();
        Node n1 = e.getNode(0);
        Node n2 = e.getNode(1);

        //get list of variable names
        List<String> vars = f.getVariables();
        for (String var : vars) {
            if (var.equalsIgnoreCase(n0.toString())
                    || var.equalsIgnoreCase(n1.toString())
                    || var.equalsIgnoreCase(n2.toString())) {
                l.add(var);
            }
        }

        return l;
    }

    //check between two edges
    public List<String> isShared(Edge e1, Edge e2) {
        return this.compare(getVariables(e1), getVariables(e2));
    }

    //check between edge and graph
    public List<String> isShared(Exp graph, Edge e) {
        List<Node> lEdge = getVariables(e);
        List<Node> lGraph = getVariablesByGraph(graph);

        return this.compare(lEdge, lGraph);
    }

    //check between two graphs
    public List<String> isShared(Exp g1, Exp g2) {
        List<Node> lGraph1 = getVariablesByGraph(g1);
        List<Node> lGraph2 = getVariablesByGraph(g2);
        return this.compare(lGraph1, lGraph2);
    }

    private List<Node> getVariables(Edge e) {
        List<Node> l = new ArrayList<Node>();
        if (e.getNode(0).isVariable()) {
            l.add(e.getNode(0));
        }
        if (e.getEdgeVariable() !=null) {
            l.add(e.getEdgeVariable());
        }
        if (e.getNode(1).isVariable()) {
            l.add(e.getNode(1));
        }

        return l;
    }

    //GRAPH{GRAPHNODE{NODE{data:aliceFoaf } } AND{EDGE{?alice foaf:mbox <mailto:alice@work.example>} ...}}
    private List<Node> getVariablesByGraph(Exp graph) {
        List<Node> l = new ArrayList<Node>();
        for (Exp e : graph) {
            if (e.type() == AND) {
                for (Exp ee : e) {
                    if (ee.type() == EDGE) {
                        l.addAll(getVariables(ee.getEdge()));
                    }
                }
            }
        }
        //remove duplicated items
        HashSet h = new HashSet(l);
        l.clear();
        l.addAll(h);

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
        } else if (this.exp.type() == GRAPH && n.exp.type() == EDGE) {
            return true;
        } else if (this.exp.type() == GRAPH && n.exp.type() == GRAPH) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return exp.toString() + "," + this.unselectivity;
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
