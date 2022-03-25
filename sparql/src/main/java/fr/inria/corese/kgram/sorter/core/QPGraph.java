package fr.inria.corese.kgram.sorter.core;

import static fr.inria.corese.kgram.api.core.ExpType.EMPTY;
import fr.inria.corese.kgram.core.Exp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Query Pattern Graph (QPG) used to encapsulate a SPARQL statement and
 * represent exp in a graph
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
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        
        createNodes(exp);
        createEdges();
    }

    public QPGraph(List<Exp> exps, List<Exp> bindings) {
        this.bindings = bindings;
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        
        createNodes(exps);
        createEdges();
    }
    
    // Encapsulate expression into BPGNode and add them to a list
    private void createNodes(Exp exp) {
        for (Exp ee : exp) {
            nodes.add(new QPGNode(ee, this.bindings));
        }
    }

      // Encapsulate expression into BPGNode and add them to a list
    private void createNodes(List<Exp> exps) {
        for (Exp ee : exps) {
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
        graph = new HashMap<>();
        for (QPGNode bpn1 : nodes) {
            for (QPGNode bpn2 : nodes) {
                if (bpn1.equals(bpn2)) continue;
                
                //make sure not repeated
                if (bpn1.isShared(bpn2)) {
                    QPGEdge edge = new QPGEdge(bpn1, bpn2);
                    edges.add(edge);
                    createIndex(edge);
                }
            }
        }
    }

    private void createIndex(QPGEdge edge) {
        QPGNode n1 = edge.get(0);
        QPGNode n2 = edge.get(1);

        //Node 1
        if (graph.containsKey(n1)) {
            graph.get(n1).add(edge);
        } else {
            List<QPGEdge> ledges = new ArrayList<>();
            ledges.add(edge);
            graph.put(n1, ledges);
        }

        //Node 2
        if (graph.containsKey(n2)) {
            graph.get(n2).add(edge);
        } else {
            List<QPGEdge> ledges = new ArrayList<>();
            ledges.add(edge);
            graph.put(n2, ledges);
        }
    }

    /**
     * Return all the edges contained in the graph
     *
     */
    public List<QPGEdge> getAllEdges() {
        return this.edges;
    }

    /**
     * Return all the edges with certain type contained in the graph
     *
     * @param edgeType type of edge: SIMPLE | BI_DIRECT
     */
    public List<QPGEdge> getEdges(int edgeType) {
        List<QPGEdge> lEdges = new ArrayList<>();
        for (QPGEdge e : this.edges) {
            if (e.getType() == edgeType) {
                lEdges.add(e);
            }
        }

        return lEdges;
    }

    /**
     * Get all edges linked to a given node
     *
     * @param node
     * @param edgeType: SIMPLE | BI_DIRECT
     * @return
     */
    public List<QPGEdge> getEdges(QPGNode node, int edgeType) {
        List<QPGEdge> lEdges = new ArrayList<>();
        if (this.graph.get(node) == null) {
            return lEdges;
        }

        for (QPGEdge e : this.graph.get(node)) {
            if (e.getType() == edgeType) {
                lEdges.add(e);
            }
        }

        return lEdges;
    }

    /**
     * Return the edges linked to the given node
     *
     * @param node
     * @return
     */
    public List<QPGEdge> getEdges(QPGNode node) {
        return this.graph.get(node);
    }

    public QPGEdge getEdge(QPGNode n1, QPGNode n2) {
        for (QPGEdge e : this.graph.get(n1)) {
            if (e.get(n1).equals(n2)) {
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
        if (Const.plannable(type)) {
            List<QPGNode> list = new ArrayList<>();
            for (QPGNode node : this.nodes) {
                if (node.getType() == type) {
                    list.add(node);
                }
            }
            return list;
        }
        return this.nodes;
    }

    /**
     * Get nodes linked to the given node
     *
     * @param n node
     * @return list
     */
    public List<QPGNode> getLinkedNodes(QPGNode n) {
        List<QPGNode> l = new ArrayList<>();
        if(!graph.containsKey(n)){
            return l;
        }
        
        for (QPGEdge e : graph.get(n)) {
            l.add(e.get(n));
        }
        return l;
    }
    
    /**
     * Get nodes linked to the given node
     *
     * @param n node
     * @param directed
     * @param in
     * @return list
     */
    public List<QPGNode> getLinkedNodes(QPGNode n, boolean directed, boolean in) {
        if (!directed) {
            return this.getLinkedNodes(n);
        }

        List<QPGNode> l = new ArrayList<>();
        if (!graph.containsKey(n)) {
            return l;
        }

        for (QPGEdge e : graph.get(n)) {
            if (in) {
                if (e.get(1).equals(n)) {
                    l.add(e.get(0));
                }
            } else {
                if (e.get(0).equals(n)) {
                    l.add(e.get(1));
                }
            }

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
