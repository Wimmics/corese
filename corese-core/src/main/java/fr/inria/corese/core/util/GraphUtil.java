package fr.inria.corese.core.util;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class GraphUtil {
    
    Graph gs;
    
    public GraphUtil(Graph g){
        gs = g;
    }
    
    public Graph shoot(){
        Graph g = Graph.create();
        try {
            Load ld = Load.create(g);
            ld.loadString(gs.toString(), Load.TURTLE_FORMAT);
        } catch (LoadException ex) {
            LoggerFactory.getLogger(GraphUtil.class.getName()).error( "", ex);
        }
        return g;
    }   
    
    public Graph shoot(String name){
        Graph g = shoot();
        gs.setNamedGraph(name, g);
        return g;
    }

}
