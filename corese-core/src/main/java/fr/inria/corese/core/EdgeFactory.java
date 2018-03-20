package fr.inria.corese.core;

import fr.inria.corese.core.edge.EdgeInternalDefault;
import fr.inria.corese.core.edge.EdgeEntail;
import fr.inria.corese.core.edge.EdgeInternalEntail;
import fr.inria.corese.core.edge.EdgeTop;
import fr.inria.corese.core.edge.EdgeRule;
import fr.inria.corese.core.edge.EdgeQuad;
import fr.inria.corese.core.edge.EdgeBinaryLabel;
import fr.inria.corese.core.edge.EdgeRuleSubclass;
import fr.inria.corese.core.edge.EdgeRuleType;
import fr.inria.corese.core.edge.EdgeBinarySubclass;
import fr.inria.corese.core.edge.EdgeGeneric;
import fr.inria.corese.core.edge.EdgeDefault;
import fr.inria.corese.core.edge.EdgeInternal;
import fr.inria.corese.core.edge.EdgeInternalRule;
import fr.inria.corese.core.edge.EdgeBinaryFirst;
import fr.inria.corese.core.edge.EdgeBinaryType;
import fr.inria.corese.core.edge.EdgeImpl;
import fr.inria.corese.core.edge.EdgeBinaryRest;
import fr.inria.corese.sparql.cg.datatype.DatatypeMap;
import fr.inria.corese.kgram.api.core.Entity;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.logic.Entailment;
import java.util.List;

/*
 * Factory creates Edges
 * Specific Named Graph have specific Edge Class
 * where graph node and/or predicate node are not replicated in each Edge
 * @author Olivier Corby, INRIA 2011-2016
 *
 */
public class EdgeFactory {
    public static int std = 0, def = 0, rul = 0, ent = 0, typ = 0, sub = 0, fst = 0, rst = 0;
    public static boolean trace = false;
    Graph graph;
    boolean isOptim = false,
            isTag = false,
            isGraph = false;
    int count = 0;
    String key;
    private boolean optimize = true;

    public EdgeFactory(Graph g) {
        graph = g;
        key = hashCode() + ".";
    }

    static public void trace(){
        System.out.println("Typ: " + typ);
        System.out.println("Sub: " + sub); 
        System.out.println("Fst: " + fst); 
        System.out.println("Rst: " + rst); 
        System.out.println();
        System.out.println("Def: " + def);
        System.out.println("Rul: " + rul);
        System.out.println("Ent: " + ent);
        System.out.println("Std: " + std);
        System.out.println("Tot: " + (std+def+rul+ent));        
    }   

    public Entity create(Node source, Node subject, Node predicate, Node value) {
        if (graph.isTuple()){
             return EdgeImpl.create(source, subject, predicate, value);
        }
        else if (optimize) {
            return genCreate(source, subject, predicate, value);
        }
        else {
            return EdgeGeneric.create(source, subject, predicate, value);
        }
    } 
    
    public Entity internal(Entity ent){
        switch (ent.getGraph().getIndex()){
            // rule edge must store an index
            case Graph.RULE_INDEX: return ent;
            case Graph.DEFAULT_INDEX:
                return EdgeInternalDefault.create(ent.getGraph(), ent.getNode(0), ent.getEdge().getEdgeNode(), ent.getNode(1));
            case Graph.ENTAIL_INDEX:
                return EdgeInternalEntail.create(ent.getGraph(), ent.getNode(0), ent.getEdge().getEdgeNode(), ent.getNode(1));    
            default: 
                return EdgeInternal.create(ent.getGraph(), ent.getNode(0), ent.getEdge().getEdgeNode(), ent.getNode(1));
        }
    }
    
    public Entity compact(Entity ent){
        switch (ent.getGraph().getIndex()){
            case Graph.RULE_INDEX: 
                return EdgeInternalRule.create(ent.getNode(0), ent.getNode(1));
            default: return ent;
        }
    }
    
    /**
     * Specific named graph and specific properties have specific Edge class
     */
    public Entity genCreate(Node source, Node subject, Node predicate, Node value) {
        switch (source.getIndex()) {
            case Graph.RULE_INDEX:
                rul++;
                return ruleCreate(source, subject, predicate, value);
             case Graph.ENTAIL_INDEX:
                ent++;
                return entailCreate(source, subject, predicate, value);    
            case Graph.DEFAULT_INDEX:
                def++;
                return defaultCreate(source, subject, predicate, value);
            default:
                std++;
                return quad(source, subject, predicate, value);
        }
    }
    
    /**
     * Edge for default graph kg:default
     * Named Graph is not stored in Edge
     * Several properties have specific class: rdf:type, rdf:first, rdf:rest, rdfs:subClassOf
     */
    public Entity defaultCreate(Node source, Node subject, Node predicate, Node value) {
        switch (predicate.getIndex()){
            case Graph.TYPE_INDEX: 
                typ++;
                return EdgeBinaryType.create(source, subject, predicate, value);
             case Graph.SUBCLASS_INDEX: 
                sub++;
                return EdgeBinarySubclass.create(source, subject, predicate, value);
            case Graph.LABEL_INDEX: 
                return EdgeBinaryLabel.create(source, subject, predicate, value);
            case Graph.FIRST_INDEX:
                fst++;
                return EdgeBinaryFirst.create(source, subject, predicate, value); 
             case Graph.REST_INDEX:
                rst++;
                return EdgeBinaryRest.create(source, subject, predicate, value);              
             default:
                 return EdgeDefault.create(source, subject, predicate, value);       
        }       
    }
    
    /**
     * Edge for RDFS entailment graph kg:entail
     */
    public Entity entailCreate(Node source, Node subject, Node predicate, Node value) {
        ent++;
        return EdgeEntail.create(source, subject, predicate, value);
    }

    /**
     * Edge for rule graph kg:rule, store an index and a provenance
     * Named Graph is not stored in Edge
     * Several properties have specific class: rdf:type, rdfs:subClassOf
     */    
    public Entity ruleCreate(Node source, Node subject, Node predicate, Node value) {
        switch (predicate.getIndex()){
            case Graph.TYPE_INDEX: 
                typ++;
                return EdgeRuleType.create(source, subject, predicate, value);
             case Graph.SUBCLASS_INDEX: 
                sub++;
                return EdgeRuleSubclass.create(source, subject, predicate, value); 
             default:
                return EdgeRule.create(source, subject, predicate, value);    
        }       
    }

    /**
     * Edge for user named graph
     */
    public Entity quad(Node source, Node subject, Node predicate, Node value) {
            return EdgeQuad.create(source, subject, predicate, value);
   }
    
    public Entity create(Node source, Node predicate, List<Node> list) {
        Entity ee = EdgeImpl.create(source, predicate, list);
        return ee;
    }
    
    public Entity copy(Node node, Node pred, Entity ent) {
        if (ent instanceof EdgeImpl) {
            EdgeImpl ee = ((EdgeImpl)ent).copy();
            ee.setGraph(node);
            return ee;
        }       
        else {
            return create(node, ent.getNode(0), pred,  ent.getNode(1));
        }
    }
    
    public Entity copy(Entity ent){
        return copy(ent.getGraph(), ent.getEdge().getEdgeNode(), ent);
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
