package fr.inria.edelweiss.kgraph.core;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.edelweiss.kgram.api.core.Entity;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.logic.Entailment;

/*
 * Factory creates Edges
 * Quad : EdgeQuad
 * Tuple: EdgeImpl
 * @author Olivier Corby, Edelweiss INRIA 2011
 *
 */
public class EdgeFactory {

    Graph graph;
    boolean isOptim = false,
            isTag = false,
            isGraph = false;
    int count = 0;
    String key;

    EdgeFactory(Graph g) {
        graph = g;
        key = hashCode() + ".";
    }



    public Entity create(Node source, Node subject, Node predicate, Node value) {
        if (graph.isTuple()){
             return EdgeImpl.create(source, subject, predicate, value);
        }
        else {
            return EdgeQuad.create(source, subject, predicate, value);
        }
    }
    
    public Entity create(Node source, Node predicate, List<Node> list) {
        Entity ee = EdgeImpl.create(source, predicate, list);
        return ee;
    }

    /**
     * Piece of code specific to EdgeImpl
     */
    public void setGraph(Entity ent, Node g) {
        if (ent instanceof EdgeTop) {
            EdgeTop e = (EdgeTop) ent;
            e.setGraph(g);
        }
 
    }

    public Entity copy(Entity ent) {
        if (ent instanceof EdgeTop) {
            EdgeTop e = (EdgeTop) ent;
            return e.copy();
        }       
        else {
            return create(ent.getGraph(), ent.getEdge().getEdgeNode(), ent.getNode(0), ent.getNode(1));
        }
    }

    Entity tag(Entity ent) {
        if (ent instanceof EdgeImpl) {
            EdgeImpl ee = (EdgeImpl) ent;
            ee.setTag(graph.tag());
        }
        return ent;
    }

    public Entity createDelete(Node source, Node subject, Node predicate, Node value) {
        return EdgeQuad.create(source, subject, predicate, value);
    }

    /**
     * Tuple
     */
    public Entity createDelete(Node source, Node predicate, List<Node> list) {
        return create(source, predicate, list);
    }
    
    
    boolean hasTag() {
        return graph.hasTag();
    }

    boolean hasTime() {
        return false;
    }

    // not with rules
    public void setGraph(boolean b) {
        isGraph = b;
    }

    Entailment proxy() {
        return graph.getProxy();
    }

 
    public Entity stdCreate(Node source, Node subject, Node predicate, Node value) {
        Entity edge = EdgeImpl.create(source, subject, predicate, value);
        return edge;
    }

    // with time stamp
    public Entity timeCreate(Node source, Node subject, Node predicate, Node value) {
        Node time = graph.getNode(DatatypeMap.newDate(), true, true);
        Entity edge = new EdgeImpl(source, predicate, subject, value, time);
        return edge;
    }
}
