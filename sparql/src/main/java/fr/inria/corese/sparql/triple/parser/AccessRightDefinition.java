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
    
    private static AccessRightDefinition singleton;
    
    private AccessMap nodeAccess;
    private AccessMap graphAccess;
    private AccessMap predicateAccess;
    
    private boolean debug = false;
    
    
    
    static {
        setSingleton(new AccessRightDefinition());
    }
    
    public class AccessMap extends HashMap<String, Byte> {

        public void define(String uri, Byte b) {
            put(uri, b);
        }

        Byte getAccess(Node node) {
            if (isEmpty()) {
                return null;
            }
            Byte b = get(node.getLabel());
            if (b != null) {
                return b;
            }
            String ns = namespace(node);
            return get(ns);
        }
        
    }
    
    public AccessRightDefinition() {
        init();
    }
    
    void init() {
        nodeAccess      = new AccessMap();
        graphAccess     = new AccessMap();
        predicateAccess = new AccessMap();
    }
    
    
    int size() {
        return getNode().size() + getPredicate().size() + getGraph().size();
    }
    
    
    /**
     * def is the default access right granted
     * res is the URI|namespace access right granted for edge 
     */
    Byte getAccess(Edge edge, byte def) {
        Byte res = getAccessOrDefault(edge);
        if (res == null) {
            return def;
        }
        return res;
    }
    
    Byte getAccess(Edge edge) {
        if (size() > 0) {
            Byte node   = combine(getSubject(edge),   getObject(edge));
            Byte access = combine(getPredicate(edge), getGraph(edge));
            Byte res    = combine(node, access);           
            return res;
        }
        return null;
    }
    
    Byte combine(Byte b1, Byte b2) {
        if (AccessRight.getMode() == AccessRight.BI_MODE) {
            return combineBinary(b1, b2);
        }
        return moreRestricted(b1, b2);
    }
    
    Byte combineBinary(Byte b1, Byte b2) {
        if (b1 == null) {
            return b2;
        }
        if (b2 == null) {
            return b1;
        }
        return (byte)(b1 | b2) ;
    }
    
    Byte getAccessOrDefault(Edge edge) {
        Byte res = getAccess(edge);
        if (res == null) {
            return getSingleton().getAccess(edge);
        }
        return res;
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

  
    // return null when there is no uri access right
    Byte getPredicate(Edge edge) {
        return getPredicate().getAccess(edge.getPredicate());
    }
    
    Byte getGraph(Edge edge) {
         if (edge.getGraph() == null) {
            return null;
        }
        return getGraph().getAccess(edge.getGraph());
    }
    
    Byte getSubject(Edge edge) {
        return getNode().getAccess(edge.getNode(0));
    }
    
    Byte getObject(Edge edge) {
        return getNode().getAccess(edge.getNode(1));
    }
    
   
    
    String namespace(Node node) {
        return NSManager.namespace(node.getLabel());
    }
    
     /**
     * @return the nodeAccess
     */
    public AccessMap getNode() {
        return nodeAccess;
    }

    /**
     * @param nodeAccess the nodeAccess to set
     */
    public void setNode(AccessMap nodeAccess) {
        this.nodeAccess = nodeAccess;
    }

    /**
     * @return the graphAccess
     */
    public AccessMap getGraph() {
        return graphAccess;
    }

    /**
     * @param graphAccess the graphAccess to set
     */
    public void setGraph(AccessMap graphAccess) {
        this.graphAccess = graphAccess;
    }

    /**
     * @return the predicateAccess
     */
    public AccessMap getPredicate() {
        return predicateAccess;
    }

    /**
     * @param predicateAccess the predicateAccess to set
     */
    public void setPredicate(AccessMap predicateAccess) {
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

    /**
     * @return the singleton
     */
    public static AccessRightDefinition getSingleton() {
        return singleton;
    }

    /**
     * @param aSingleton the singleton to set
     */
    public static void setSingleton(AccessRightDefinition aSingleton) {
        singleton = aSingleton;
    }
    
    
    
    
    
    
}
