package fr.inria.corese.core.producer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;

/**
 * from, from named dataset for edge iteration
 * getNamed().from|minus(list|node).iterate()
 * getDefault().from|minus(list|node).iterate()
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class DataFrom extends DataFilter {
    static final List<Node> emptyNode = new ArrayList<Node>(0);

    private List<Node> from;
    private Node fromNode;
    Graph graph;

    boolean hasFrom;
    private boolean oneFrom;
    boolean isMember = true;

    DataFrom(Graph g) {
        graph = g;
    }

    DataFrom from(List<Node> list, Node source) {
        if (source == null) {
            from(list);
        }
        from(source);
        return this;
    }

    DataFrom from(Node node) {
        setOneFrom(true);
        setFromNode(node);
        if (getFrom() == null) {
            setFrom(emptyNode);
        }
        return this;
    }

    DataFrom from(List<Node> list) {
        setFrom(list);
        if (list != null && list.size() == 1) {
            setOneFrom(true);
            setFromNode(graph.getGraphNode(getFrom().get(0)));
            if (getFromNode() == null) {
                setOneFrom(false);
            }
        }
        return this;
    }

    public DataFrom minus(List<Node> list) {
        from(list);
        isMember = false;
        return this;
    }

    public DataFrom minus(Node node) {
        from(node);
        isMember = false;
        return this;
    }

    boolean isEmpty() {
        return getFrom().isEmpty();
    }

    @Override
    boolean eval(Edge ent) {
        boolean b = result(isFrom(ent));
        return b;
    }

    boolean isFrom(Edge ent) {
        if (isOneFrom()) {
            return same(fromNode, ent.getGraph());
        } else {
            Node g = ent.getGraph();
            int res = find(from, g);
            return res != -1;
        }
    }

    /**
     * isMember = false means skip graph in from clause
     */
    @Override
    boolean result(boolean found) {
        if (isMember) {
            return found;
        }
        return !found;
    }

    boolean same(Node n1, Node n2) {
        return n1.getIndex() == n2.getIndex();
    }

    boolean isFromOK(List<Node> from) {
        for (Node node : from) {
            Node tfrom = graph.getNode(node);
            if (tfrom != null && graph.containsCoreseNode(tfrom)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFrom(List<Node> from, Node node) {
        int res = find(from, node);
        return res != -1;
    }

    int find(List<Node> list, Node node) {
        int res = find(list, node, 0, list.size());
        if (res >= 0 && res < list.size() && list.get(res).same(node)) {
            return res;
        }
        return -1;
    }

    /**
     * Find the index of node in list of Node by dichotomy
     */
    int find(List<Node> list, Node node, int first, int last) {
        if (first >= last) {
            return first;
        } else {
            int mid = (first + last) / 2;
            int res = list.get(mid).compare(node);
            if (res >= 0) {
                return find(list, node, first, mid);
            } else {
                return find(list, node, mid + 1, last);
            }
        }
    }

    /**
     * @return the from
     */
    public List<Node> getFrom() {
        return from;
    }

    /**
     * @param from the from to set
     */
    public void setFrom(List<Node> from) {
        this.from = from;
        Collections.sort(this.from, new Comparator<Node>() {
            public int compare(Node o1, Node o2) {
                return o1.compare(o2);
            }
        });
    }

    /**
     * @return the oneFrom
     */
    public boolean isOneFrom() {
        return oneFrom;
    }

    /**
     * @param oneFrom the oneFrom to set
     */
    public void setOneFrom(boolean oneFrom) {
        this.oneFrom = oneFrom;
    }

    /**
     * @return the fromNode
     */
    public Node getFromNode() {
        return fromNode;
    }

    /**
     * @param fromNode the fromNode to set
     */
    public void setFromNode(Node fromNode) {
        this.fromNode = fromNode;
    }

}
