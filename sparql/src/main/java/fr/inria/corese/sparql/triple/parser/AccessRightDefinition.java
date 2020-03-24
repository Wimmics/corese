package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import java.util.HashMap;

/**
 * Access right granted for specific URI or namespace
 * It **overloads** the default access right given by AccessRight
 * It may increase or reduce the default access right
 * Semantics: access right given to the user for insert and delete
 * 
 * @author Olivier Corby, INRIA 2020
 */
public class AccessRightDefinition {
    
    private HashMap<String, Byte> nodeAccess;
    private HashMap<String, Byte> graphAccess;
    private HashMap<String, Byte> predicateAccess;
    
    private boolean debug = false;
    
    public AccessRightDefinition() {
        init();
    }
    
    void init() {
        nodeAccess      = new HashMap<>();
        graphAccess     = new HashMap<>();
        predicateAccess = new HashMap<>();
    }
    
    
    int size() {
        return getNode().size() + getPredicate().size() + getGraph().size();
    }
    
    
    /**
     * def is the default access right granted
     * res is the URI|namespace access right granted for edge 
     */
    Byte getAccess(Edge edge, byte def) {
        if (size() > 0) {
            Byte node   = moreRestricted(getSubject(edge), getObject(edge));
            Byte access = moreRestricted(getPredicate(edge), getGraph(edge));
            Byte res    = moreRestricted(node, access);
            //System.out.println("Def: " + edge + " " + res + " " + def);
            if (res != null) {
                return res;
            }
        }
        
        return def;
    }
    
    Byte moreRestricted(Byte b1, Byte b2) {
        if (b1 == null) {
            return b2;
        }
        if (b2 == null) {
            return b1;
        }
        return b1 > b2 ? b1 : b2;
    }
    
    Byte lessRestricted(Byte b1, Byte b2) {
        if (b1 == null) {
            return b2;
        }
        if (b2 == null) {
            return b1;
        }
        return b1 < b2 ? b1 : b2;
    }

    Byte getAccess(Node node, HashMap<String, Byte> map) {
        if (map.isEmpty()) {
            return null;
        }
        Byte b = map.get(node.getLabel());
        if (b != null) {
            return b;
        }
        String ns = namespace(node);
        return map.get(ns);
    }
    
    Byte getPredicate(Edge edge) {
        return getAccess(edge.getPredicate(), getPredicate());
    }
    
    Byte getGraph(Edge edge) {
         if (edge.getGraph() == null) {
            return null;
        }
        return getAccess(edge.getGraph(), getGraph());
    }
    
    Byte getSubject(Edge edge) {
        return getAccess(edge.getNode(0), getNode());
    }
    
    Byte getObject(Edge edge) {
        return getAccess(edge.getNode(1), getNode());
    }
    
   
    
    String namespace(Node node) {
        return NSManager.namespace(node.getLabel());
    }
    
     /**
     * @return the nodeAccess
     */
    public HashMap<String, Byte> getNode() {
        return nodeAccess;
    }

    /**
     * @param nodeAccess the nodeAccess to set
     */
    public void setNode(HashMap<String, Byte> nodeAccess) {
        this.nodeAccess = nodeAccess;
    }

    /**
     * @return the graphAccess
     */
    public HashMap<String, Byte> getGraph() {
        return graphAccess;
    }

    /**
     * @param graphAccess the graphAccess to set
     */
    public void setGraph(HashMap<String, Byte> graphAccess) {
        this.graphAccess = graphAccess;
    }

    /**
     * @return the predicateAccess
     */
    public HashMap<String, Byte> getPredicate() {
        return predicateAccess;
    }

    /**
     * @param predicateAccess the predicateAccess to set
     */
    public void setPredicate(HashMap<String, Byte> predicateAccess) {
        this.predicateAccess = predicateAccess;
    }

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    
    
    
    
    
}
