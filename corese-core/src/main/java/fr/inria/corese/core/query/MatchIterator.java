package fr.inria.corese.core.query;

import java.util.HashMap;
import java.util.Iterator;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Matcher;
import fr.inria.corese.core.Graph;

/**
 * Iterator of Entity that perform match.match() It checks subsumption in the
 * Producer Return one occurrence of each resource for ?x rdf:type aClass
 *
 * @author Olivier Corby, Wimmics INRIA 2012
 *
 */
public class MatchIterator implements Iterable<Edge>, Iterator<Edge> {

    Iterable<Edge> ii;
    Iterator<Edge> it;
    Matcher match;
    Environment env;
    Edge edge;
    Node gNode;
    Graph graph;
    Table table;
    GTable gtable;

    boolean isCheckType = false;

    MatchIterator(Iterable<Edge> it, Node g, Edge e, Graph graph, Environment env, Matcher m) {
        ii = it;
        match = m;
        edge = e;
        gNode = g;
        this.graph = graph;
        this.env = env;
        if (gNode == null) {
            table = new Table();
        } else {
            gtable = new GTable();
        }
        isCheckType
                = graph.getProxy().isType(e)
                && m.getMode() != Matcher.RELAX
                && e.getNode(1).isConstant();
    }

    @Override
    public Iterator<Edge> iterator() {
        it = ii.iterator();
        return this;
    }

    @Override
    public boolean hasNext() {
        return it.hasNext();
    }

    @Override
    public Edge next() {
        while (it.hasNext()) {
            Edge ent = it.next();
            if (ent != null && match.match(edge, ent, env)) {
                if (isCheckType) {
                    // ?x rdf:type ex:Person
                    // keep one occurrence of each resource
                    if (isFirst(ent)) {
                        return ent;
                    }
                } else {
                    return ent;
                }
            }
        }
        return null;
    }

    @Override
    public void remove() {
    }

    Table getTable(Edge ent) {
        if (gNode == null) {
            return table;
        } else {
            Table t = gtable.get(ent.getGraph());
            if (t == null) {
                t = new Table();
                gtable.put(ent.getGraph(), t);
            }
            return t;
        }
    }

    /**
     * ?x rdf:type ex:Person Keep one occurrence of each value of ?x graph ?g {
     * ?x rdf:type ex:Person } Keep one occurrence of each value of ?x for each
     * graph 
	 *
     */
    boolean isFirst(Edge ent) {
        Table t = getTable(ent);
        boolean b = t.containsKey(ent.getNode(0));
        if (b) {
            return false;
        }
        t.put(ent.getNode(0), ent.getNode(0));
        return true;
    }

    class Table extends HashMap<Node, Node> {
    }

    // gNode -> Table
    class GTable extends HashMap<Node, Table> {
    }

}
