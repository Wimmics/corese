package fr.inria.edelweiss.kgram.sorter.core;

import static fr.inria.edelweiss.kgram.api.core.ExpType.EDGE;
import static fr.inria.edelweiss.kgram.api.core.ExpType.EMPTY;
import static fr.inria.edelweiss.kgram.api.core.ExpType.FILTER;
import static fr.inria.edelweiss.kgram.api.core.ExpType.GRAPH;
import static fr.inria.edelweiss.kgram.api.core.ExpType.VALUES;
import fr.inria.edelweiss.kgram.core.Exp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic triple pattern graph, mainly contains a list of nodes and a map of
 * nodes and their connected nodes
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 19 mai 2014
 */
public class BPGraph {

    // private final static Node DEFAULT = null;
    // list of nodes
    private List<BPGNode> nodes = null;

    // list of nodes that not planned
    //private List<Exp> nodesNonPlanned = null;
    // list of edges
    private List<BPGEdge> edges = null;
    // data structure that used to represents the graph
    // map(node, edges)
    private Map<BPGNode, List<BPGEdge>> graph = null;

    private final List<Exp> bindings;

    //private List<BPGraph> graphs = new ArrayList<BPGraph>();
    //for the moment, we just consider the AND and atomic relation 
    public BPGraph(Exp exp, List<Exp> bindings) {
        //can be extended for the other exp types
        this.bindings = bindings;
        nodes = new ArrayList<BPGNode>();
        edges = new ArrayList<BPGEdge>();

        createNodes(exp);
        createEdges();
    }

    // Encapsulate expression into BPGNode and add them to a list
    private void createNodes(Exp exp) {
        for (Exp ee : exp) {
            nodes.add(new BPGNode(ee, this.bindings));
        }
    }

    //Create graph structure by finding variables sharing between nodes
    private void createEdges() {
        // Graph Structure:
        // Edge 1: connected edge11, edge 12, ...
        // Edge 2: connected edge 21, edge 22, ...\
        // ...
        // Filter 1: Connected edge 111, edge 112, ...
        // Filter 2...
        // ...
        //values ...
        graph = new HashMap<BPGNode, List<BPGEdge>>();

        for (BPGNode bpn : nodes) {
            List<BPGEdge> ledges = new ArrayList<BPGEdge>();
            for (BPGNode bpn2 : nodes) {
                //make sure not repeated
                if (!bpn.equals(bpn2) && bpn.isShared(bpn2)) {
                    //check weather the edge already existed
                    BPGEdge edge = this.exist(bpn, bpn2);
                    if (edge == null) {
                        edge = new BPGEdge(bpn, bpn2);
                        if (bpn.getType() == FILTER) {
                            edge.setDirected(true);
                        }
                        //set list of shared vairables
                        edge.setVariables(bpn.shared(bpn2));
                        //add edge to graph and edge list
                        edges.add(edge);
                    }

                    ledges.add(edge);
                }
            }
            graph.put(bpn, ledges);
        }
    }

    //check weather the edge is existing
    private BPGEdge exist(BPGNode n1, BPGNode n2) {
        for (BPGEdge bpe : edges) {
            if ((bpe.get(0).equals(n1) && bpe.get(1).equals(n2))
                    || (bpe.get(0).equals(n2) && bpe.get(1).equals(n1))) {
                return bpe;
            }
        }
        return null;
    }

    /**
     * Return all the edges contained in the graph
     *
     * @return
     */
    public List<BPGEdge> getAllEdges() {
        return this.edges;
    }

    /**
     * Return all the edges with certain type contained in the graph
     *
     * @param type EDGE, else return all
     * @return
     */
    public List<BPGEdge> getAllEdges(int type) {
        if (type == EDGE) {
            List<BPGEdge> lEdges = new ArrayList<BPGEdge>();
            for (BPGEdge e : this.edges) {
                if (e.get(0).getType() == EDGE && e.get(1).getType() == EDGE) {
                    lEdges.add(e);
                }
            }
            return lEdges;
        }

        return this.edges;
    }
    
    /**
     * Get all edges linked to a given node
     * @param node
     * @return 
     */
    public List<BPGEdge> getEdges(BPGNode node) {
        return this.graph.get(node);
    }

    /**
     * Return list of nodes contained in the triple pattern graph
     *
     * @return
     */
    public List<BPGNode> getAllNodes() {
        return this.getAllNodes(EMPTY);
    }

    /**
     * Return list of nodes that contain triple pattern expression (edge)
     *
     * @param type EDGE, VALUES, FILTER, otherwise return all
     * @return
     */
    public List<BPGNode> getAllNodes(int type) {
        if (type != EDGE && type != VALUES && type != FILTER && type != GRAPH) {
            return this.nodes;
        }

        List<BPGNode> list = new ArrayList<BPGNode>();
        for (BPGNode node : this.nodes) {
            if (node.getType() == type) {
                list.add(node);
            }
        }
        return list;
    }

    /**
     * Get nodes linked to the given node
     *
     * @param n node
     * @return list
     */
    public List<BPGNode> getLinkedNodes(BPGNode n) {
        List<BPGNode> l = new ArrayList<BPGNode>();
        for (BPGEdge e : graph.get(n)) {
            l.add(e.get(n));
        }
        return l;
    }

    /**
     * Get the list of vairable bound to constants
     *
     * @return
     */
    public List<Exp> getBindings() {
        return this.bindings;
    }
}
