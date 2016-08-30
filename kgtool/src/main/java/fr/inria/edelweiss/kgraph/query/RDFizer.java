/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.edelweiss.kgraph.query;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.kgram.api.query.Graphable;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.util.SPINProcess;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class RDFizer {
    
    
     public boolean isGraphAble(Object obj) {
        if (obj instanceof Graphable) {
                return true;
        }  
        return false;
    }

    public Graph getGraph(Object obj) {
        if (obj instanceof Graphable) {
            return getGraph((Graphable) obj);
        }
        return null;
    }
    
    Graph getGraph(Graphable gg) {
        Graph g = (Graph) gg.getGraph();
        if (g != null){
            return g;
        }
        String rdf = gg.toGraph();
        try {
            g = getGraph(rdf);
            gg.setGraph(g);
        } catch (EngineException ex) {
           
        }
        return g;
    }
    
 
    
    
      Graph getGraph(String rdf) throws EngineException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        try {
            ld.loadString(rdf, Load.TURTLE_FORMAT);
        } catch (LoadException ex) {
            LogManager.getLogger(SPINProcess.class.getName()).log(Level.ERROR, "", ex);
        } 
        return g;
     }
     
     
     
       Graph getGraph(Query q) {
        try {
            Graph g = (Graph) q.getGraph();
            if (g == null){
                ASTQuery ast = (ASTQuery) q.getAST();
                SPINProcess sp = SPINProcess.create();
                g = sp.toSpinGraph(ast);
                q.setGraph(g);
            }           
            return g;
        } catch (EngineException ex) {
        }
        return null;
    }

}
