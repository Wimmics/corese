package fr.inria.corese.core;

import java.util.ArrayList;
import java.util.HashMap;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.tool.DistinctNode;
import fr.inria.corese.kgram.tool.MetaIterator;

/**
 *
 * Manage Nodes of a graph, for each graph name
 *
 * @author Olivier Corby, Wimmics INRIA 2018
 *
 */
public class NodeGraphIndex {

    class NodeTable extends HashMap<Node, Node> {
    }

    class GraphTable extends HashMap<Node, NodeTable> {
    }

    GraphTable table;

    NodeGraphIndex() {
        table = new GraphTable();
    }

    static NodeGraphIndex create() {
        return new NodeGraphIndex();
    }

    int size() {
        return table.size();
    }

    void clear() {
        table.clear();
    }

    void add(Edge edge) {
        for (int i = 0; i < edge.nbGraphNode(); i++) {
            add(edge.getGraph(), edge.getNode(i));
        }
    }

    void add(Node graph, Node node) {
        NodeTable gt = table.get(graph);
        if (gt == null) {
            gt = new NodeTable();
            table.put(graph, gt);
        }
        if (!gt.containsKey(node)) {
            gt.put(node, new NodeGraph(node, graph));
        }
    }
    
    boolean contains(Node graph, Node node) {
        NodeTable gt = table.get(graph);
        if (gt == null) {
            return false;
        }
        return gt.containsKey(node);
    }

    // return iterable of NodeGraph(node, graph)
    // MUST perform n.getNode() to get the node
    public Iterable<Node> getNodes(Node graph) {
        NodeTable gt = table.get(graph);
        if (gt == null) {
            return new ArrayList<>();
        }
        return gt.values();
    }

    // return iterable of NodeGraph(node, graph)
    // MUST perform n.getNode() to get the node
    // return all pairs (node, graph)
    public Iterable<Node> getNodes() {
        MetaIterator<Node> meta = null;
        for (NodeTable gt : table.values()) {
            if (meta == null) {
                meta = new MetaIterator<>(gt.values());
            } else {
                meta.next(gt.values());
            }
        }
        if (meta == null) {
            return new ArrayList<>();
        }
        return meta;
    }
    
    // return iterable of NodeGraph(node, graph)
    // MUST perform n.getNode() to get the node
    // return distinct nodes
    public Iterable<Node> getDistinctNodes() {
        return new DistinctNode(getNodes());
    }

    
    public HashMap<Node, NodeTable> getGraphTable() {
        return table;
    }

}
