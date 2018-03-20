package fr.inria.corese.core.util;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

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
            LogManager.getLogger(GraphUtil.class.getName()).log(Level.ERROR, "", ex);
        }
        return g;
    }   
    
    public Graph shoot(String name){
        Graph g = shoot();
        gs.setNamedGraph(name, g);
        return g;
    }

}
