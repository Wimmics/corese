package fr.inria.corese.core.api;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface to corese graph OR external graph implementation
 * For sparql update
 */ 
public interface DataManagerUpdate {
    
    
    default Edge insert(Edge edge) {
        return edge;
    }
    
    /**
     * If Edge have a named graph: delete this occurrence
     * Otherwise: delete all occurrences of edge 
     * Return list of deleted edge
     */
    
    default List<Edge> delete(Edge edge) {
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
        return new ArrayList<>(0);
    }
    
    // remove edges from named graph
    default void clear(String name, boolean silent) {
    }

    // remove named graph name (after it has been cleared)
    default void deleteGraph(String name) {
    }

    // apply clear on every named graph
    default void clearNamed() {
    }

    // apply deleteGraph on every named graph
    default void dropGraphNames() {
    }

    // remove edges from default graph
    default void clearDefault() {
    }

    // add edges of source named graph into target named graph
    default boolean add(String source, String target, boolean silent) {
        return true;
    }

    // clear target named graph
    // add edges of source named graph  into target named graph
    // clear source named graph
    default boolean move(String source, String target, boolean silent) {
        return true;
    }

    // clear target named graph
    // add edges of source named graph into target named graph
    default boolean copy(String source, String target, boolean silent) {
        return true;
    }

    // declare and create new named graph
    default void addGraph(String uri) {      
    }
      
}
