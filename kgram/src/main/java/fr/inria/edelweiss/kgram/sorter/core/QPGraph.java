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
 * Query Pattern Graph (QPG)
 * used to encapsulate a SPARQL statement and represent exp in a graph
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 19 mai 2014
 */
public class QPGraph {

    // list of nodes
    private List<QPGNode> nodes = null;
    // list of edges
    private List<QPGEdge> edges = null;
    // identified bound values
    private List<Exp> bindings = null;

    // data structure that used to represents the graph
    // map(node, edges)
    private Map<QPGNode, List<QPGEdge>> graph = null;

    public QPGraph(Exp exp, List<Exp> bindings) {
        this.bindings = bindings;
        this.nodes = new ArrayList<QPGNode>();
        this.edges = new ArrayList<QPGEdge>();

        //this.bindings = qs.findBindings(exp);
        
        createNodes(exp);
        createEdges();
    }

    // Encapsulate expression into BPGNode and add them to a list
    private void createNodes(Exp exp) {
        for (Exp ee : exp) {
            nodes.add(new QPGNode(ee, this.bindings));
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
        graph = new HashMap<QPGNode, List<QPGEdge>>();

        for (QPGNode bpn : nodes) {
            List<QPGEdge> ledges = new ArrayList<QPGEdge>();
            for (QPGNode bpn2 : nodes) {
                //make sure not repeated
                if (!bpn.equals(bpn2) && bpn.isShared(bpn2)) {
                    //check weather the edge already existed
                    QPGEdge edge = this.exist(bpn, bpn2);
                    if (edge == null) {
                        edge = new QPGEdge(bpn, bpn2);
                        edges.add(edge);
                    }

                    ledges.add(edge);
                }
            }
            graph.put(bpn, ledges);
        }
    }

    //check whether the edge is existing
    private QPGEdge exist(QPGNode n1, QPGNode n2) {
        for (QPGEdge bpe : edges) {
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
    public List<QPGEdge> getAllEdges() {
        return this.edges;
    }

    /**
     * Return all the edges with certain type contained in the graph
     *
     * @param type of edge
     * @return
     */
    public List<QPGEdge> getEdges(int type) {
        List<QPGEdge> lEdges = new ArrayList<QPGEdge>();
        for (QPGEdge e : this.edges) {
            if (e.getType() == type) {
                lEdges.add(e);
            }

        }

        return lEdges;
    }

    /**
     * Get all edges linked to a given node
     *
     * @param node
     * @param type: SIMPLE | BI_DIRECT
     * @return
     */
    public List<QPGEdge> getEdges(QPGNode node, int type) {
        List<QPGEdge> lEdges = new ArrayList<QPGEdge>();
        if(this.graph.get(node) == null) return lEdges;
        
        for (QPGEdge e : this.graph.get(node)) {
            if (e.getType() == type) {
                lEdges.add(e);
            }
        }

        return lEdges;
    }

    /**
     * Return the edges linked to the given node
     * @param node
     * @return 
     */
    public List<QPGEdge> getEdges(QPGNode node) {
        return this.graph.get(node);
    }
    
    public QPGEdge getEdge(QPGNode n1, QPGNode n2){
        for (QPGEdge e : this.graph.get(n1)) {
            if(e.get(n1).equals(n2)){
                return e;
            }
        }
             
        return null;
    }

    /**
     * Return list of nodes contained in the triple pattern graph
     *
     * @return
     */
    public List<QPGNode> getAllNodes() {
        return this.getAllNodes(EMPTY);
    }

    /**
     * Return list of nodes that contain triple pattern expression (edge)
     *
     * @param type EDGE, VALUES, FILTER, GRAPH, otherwise return all
     * @return
     */
    public List<QPGNode> getAllNodes(int type) {
        if (type != EDGE && type != VALUES && type != FILTER && type != GRAPH) {
            return this.nodes;
        }

        List<QPGNode> list = new ArrayList<QPGNode>();
        for (QPGNode node : this.nodes) {
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
    public List<QPGNode> getLinkedNodes(QPGNode n) {
        List<QPGNode> l = new ArrayList<QPGNode>();
        for (QPGEdge e : graph.get(n)) {
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
