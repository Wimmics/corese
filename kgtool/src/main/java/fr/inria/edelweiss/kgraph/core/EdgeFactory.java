package fr.inria.edelweiss.kgraph.core;

import fr.inria.edelweiss.kgraph.core.edge.EdgeQuad;
import fr.inria.edelweiss.kgraph.core.edge.EdgeImpl;
import fr.inria.edelweiss.kgraph.core.edge.EdgeTop;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.edelweiss.kgram.api.core.Entity;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.edge.EdgeRule;
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
            return genCreate(source, subject, predicate, value);
        }
    }       
    
    public Entity genCreate(Node source, Node subject, Node predicate, Node value) {
        if (graph.isRuleGraphNode(source)){
            // Rule edge: source MUST be created using graph.addRuleGraphNode()
            return EdgeRule.create(source, subject, predicate, value);
        }
        else {
            return quad(source, subject, predicate, value);
        }
    }
    
    public Entity quad(Node source, Node subject, Node predicate, Node value) {
            return EdgeQuad.create(source, subject, predicate, value);
   }
    
    public Entity create(Node source, Node predicate, List<Node> list) {
        Entity ee = EdgeImpl.create(source, predicate, list);
        return ee;
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

    /**
     * Piece of code specific to EdgeImpl
     */
    public void setGraph(Entity ent, Node g) {
        if (ent instanceof EdgeTop) {
            EdgeTop e = (EdgeTop) ent;
            e.setGraph(g);
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

    // with time stamp
    public Entity timeCreate(Node source, Node subject, Node predicate, Node value) {
        Node time = graph.getNode(DatatypeMap.newDate(), true, true);
        Entity edge = new EdgeImpl(source, predicate, subject, value, time);
        return edge;
    }
}
