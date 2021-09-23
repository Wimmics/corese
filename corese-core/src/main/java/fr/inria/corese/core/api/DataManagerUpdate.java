package fr.inria.corese.core.api;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.update.Basic;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface to corese graph OR external graph implementation
 * For sparql update
 */ 
public interface DataManagerUpdate {
    
    
    default Edge insert(Edge edge) {
        System.out.println("insert: " + edge);
        return edge;
    }
    
    /**
     * If Edge have a named graph: delete this occurrence
     * Otherwise: delete all occurrences of edge 
     * Return list of deleted edge
     */
    
    default List<Edge> delete(Edge edge) {
        System.out.println("delete: " + edge);
        return new ArrayList<>(0);        
    }
    
    /**
     * Delete occurrences of edge in named graphs of from list
     * keep other occurrences
     * edge has no named graph
     * Return list of deleted edges
     * @todo: Constant -> IDatatype as Node
     */  
    
    default List<Edge> delete(Edge edge, List<Node> from) {
        System.out.println("delete: " + edge + " " + from);
        return new ArrayList<>(0);
    }
    
    
    
    default boolean load(Query q, Basic ope) throws EngineException {
        System.out.println("load: " + ope.getURI());
        return true;
    }
    
}
