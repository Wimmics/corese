
package fr.inria.edelweiss.kgraph.core;

import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2013
 *
 */
public class GraphStore extends Graph {
    
    HashMap <String, Graph> store;
    
    GraphStore(){
        store = new HashMap <String, Graph>();
    }
    
    public static GraphStore create(){
        return new GraphStore();
    }
    
    public Graph getNamedGraph(String name){
        return store.get(name);
    }
    
    public Graph createNamedGraph(String name){
        Graph g = Graph.create();
        store.put(name, g);
        return g;
    }
    
    public void setNamedGraph(String name, Graph g){
        store.put(name, g);
    }
    
    public Collection<Graph> getNamedGraphs(){
        return store.values();
    }
    
    public Graph getDefaultGraph(){
        return this;
    }
    

}
