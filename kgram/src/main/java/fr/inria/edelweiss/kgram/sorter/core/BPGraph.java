package fr.inria.edelweiss.kgram.sorter.core;

import fr.inria.edelweiss.kgram.api.core.ExpType;
import static fr.inria.edelweiss.kgram.api.core.ExpType.EDGE;
import static fr.inria.edelweiss.kgram.api.core.ExpType.EMPTY;
import static fr.inria.edelweiss.kgram.api.core.ExpType.FILTER;
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

    // list of nodes
    private List<BPGNode> nodes = null;
    // list of edges
    private List<BPGEdge> edges = null;
    // data structure that used to represents the graph
    // map(node, edges)
    private Map<BPGNode, List<BPGEdge>> graph = null;

    //for the moment, we just consider the AND and atomic relation 
    public BPGraph(Exp exp) {
        //can be extended for the other exp types
        if (exp.type() != ExpType.AND) {
            return;
        }

        nodes = new ArrayList<BPGNode>();
        edges = new ArrayList<BPGEdge>();

        createNodeList(exp);
        createGraph();
    }

    // Encapsulate expression into BPGNode and add them to a list
    private void createNodeList(Exp exp) {
        for (Exp ee : exp) {
            //TODO other types
            if (ee.type() == Exp.FILTER || ee.type() == EDGE || ee.type() == Exp.VALUES) {
                nodes.add(new BPGNode(ee));
            }
        }
    }


    //Create graph structure by finding variables sharing between nodes
    private void createGraph() {
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
     * Return list of nodes contained in the triple pattern graph
     *
     * @return
     */
    public List<BPGNode> getNodeList() {
        return this.getNodeList(EMPTY);
    }

    public List<BPGEdge> getEdgeList() {
        return this.edges;
    }

    /**
     * Return list of nodes that contain triple pattern expression (edge)
     *
     * @param type EDGE, VALUES, FILTER, otherwise return all
     * @return
     */
    public List<BPGNode> getNodeList(int type) {
        if (type != EDGE && type != VALUES && type != FILTER) {
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
    public List<BPGNode> getNodeList(BPGNode n) {
        List<BPGNode> l = new ArrayList<BPGNode>();
        for (BPGEdge e : graph.get(n)) {
            l.add(e.get(n));
        }
        return l;
    }
}
