package fr.inria.edelweiss.kgraph.core;

import fr.inria.edelweiss.kgraph.core.edge.EdgeQuad;
import java.util.Iterator;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.edge.EdgeGeneric;
import java.util.ArrayList;

/**
 *
 * Eliminate successive similar edges (when no graph ?g {}) Check from & from
 * named Check graph ?g
 */
public class EdgeIterator implements Iterable<Entity>, Iterator<Entity> {

    static final List<Entity> empty = new ArrayList<Entity>(0);
    Iterable<Entity> iter;
    Iterator<Entity> it;
    EdgeGeneric glast;
    Edge last;
    Node graphNode;
    //Graph graph;
    List<Node> from;
    Node fromNode;
    boolean hasGraph, hasFrom, hasOneFrom;
    private boolean hasTag = false;
    private int level = -1;
    boolean hasLevel = false;

    EdgeIterator(Graph g) {
        //graph = g;
        init();
    }

    // eliminate duplicate edges due to same source
    EdgeIterator(Graph g, Iterable<Entity> i) {
        //graph = g;
        iter = i;
        hasGraph = false;
        hasFrom = false;
        hasOneFrom = false;
        init();
    }

    public static EdgeIterator create(Graph g) {
        EdgeIterator ei = new EdgeIterator(g);
        ei.setTag(g.hasTag());
        return ei;
    }

    public static EdgeIterator create(Graph g, Iterable<Entity> i) {
        EdgeIterator ei = new EdgeIterator(g, i);
        ei.setTag(g.hasTag());
        return ei;
    }

    void init() {
    }

    public EdgeIterator(Graph g, Iterable<Entity> i, List<Node> list, boolean hasGraph) {
        iter = i;
        from = list;
        this.hasGraph = hasGraph;
        hasFrom = from.size() > 0;
        hasOneFrom = from.size() == 1;
        if (hasOneFrom) {
            fromNode = g.getGraphNode(from.get(0).getLabel());
            if (fromNode == null) {
                iter = empty;
            }
        }
        setTag(g.hasTag());
        //graph = g;
        init();
    }

    void setTag(boolean b) {
        hasTag = b;
    }

    void setGraph(Node g) {
        hasGraph = true;
        graphNode = g;
    }

    @Override
    public Iterator<Entity> iterator() {
        // TODO Auto-generated method stub
        it = iter.iterator();
        last = null;
        return this;
    }

    @Override
    public boolean hasNext() {
        // TODO Auto-generated method stub
        return it.hasNext();
    }

    boolean same(Node n1, Node n2) {
        return n1.getIndex() == n2.getIndex();
    }
    
//     boolean same2(Node n1, Node n2) {
//        return graph.getIndex().same(n1, n2);
//    }

    @Override
    public Entity next() {

        while (hasNext()) {
            Entity ent = it.next();
            boolean ok = true;

            if (hasGraph) {
                if (graphNode == null) {
                    // keep duplicates
                    ok = true;
                } else {
                    // check same graph node
                    if (!same(ent.getGraph(), graphNode)) {
                        ok = false;
                    }
                }
            } else if (last != null) {
                // eliminate successive duplicates
                ok = different(last, ent.getEdge());
            }

            if (ok && hasFrom) {
                ok = isFrom(ent, from);
            }

            if (ok) {
                record(ent);
                if (hasLevel && last.getIndex() < level) {
                    // use case: Rule Engine requires edges with level >= this.level
                    it = empty.iterator();
                    return null;
                }
                return ent;
            }
        }
        return null;
    }
      
    
    boolean different(Edge last, Edge edge) {
        if (edge.getEdgeNode() == null || ! same(last.getEdgeNode(), edge.getEdgeNode())) {
            // different properties: ok
            return true;
        } else {
            int size = last.nbNode();
            if (size == edge.nbNode()) {
                // draft: third argument is a tag, skip it
                // @deprecated (Kolflow CRDT)
                if (hasTag && size == 3) {
                    size = 2;
                }
                for (int i = 0; i < size; i++) {
                    if (!same(last.getNode(i), edge.getNode(i))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    void record(Entity ent) {
        if (EdgeIndexer.test){
            record2(ent);
        }
        else {
            last = ent.getEdge();
        }
    }

    // record a copy of ent for last
    void record2(Entity ent) {
        if (glast == null) {
            glast = new EdgeGeneric();
            last = glast;
        }
        glast.duplicate(ent);
    }

    @Override
    public void remove() {
        // TODO Auto-generated method stub
    }

    /**
     *
     * Check if entity graph node is member of from by dichotomy
     */
    boolean isFrom(Entity ent, List<Node> from) {
        if (hasOneFrom) {
            return same(fromNode, ent.getGraph());
        } else {
            Node g = ent.getGraph();
            int res = find(from, g);
            return res != -1;
        }
    }

    public boolean isFrom(List<Node> from, Node node) {
        int res = find(from, node);
        return res != -1;
    }

    int find(List<Node> list, Node node) {
        int res = find(list, node, 0, list.size());
        if (res >= 0 && res < list.size()
                && list.get(res).same(node)) {
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

    boolean isFrom2(Entity ent, List<Node> from) {
        Node g = ent.getGraph();
        for (Node node : from) {
            if (g.same(node)) {
                return true;
            }
        }
        return false;
    }

    // draft unused
    private void provenance(Entity ent) {
        ent.setProvenance(graph(ent));
    }

    Graph graph(Entity ent) {
        Graph g = Graph.create();
        g.copy(ent);
        return g;
    }

    /**
     * @return the level
     */
    public int getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(int level) {
        this.level = level;
        hasLevel = level != -1;
    }

}
