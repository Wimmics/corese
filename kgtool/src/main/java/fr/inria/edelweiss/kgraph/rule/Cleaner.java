/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.edelweiss.kgraph.rule;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import java.io.IOException;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class Cleaner {
    public static final int OWL = 0;
    static final String data = "/query/clean/";
    static final String[] queries = {"ui2.rq", "ui3.rq", "ui4.rq", "allsome.rq", "card.rq"};
    
    Graph graph;
    
    Cleaner(Graph g){
        graph = g;
    }
    
    void clean(int mode) throws IOException, EngineException{
        switch (mode){
            
            case OWL: 
                clean(graph, queries);
                break;
        }
    }
      
    /**
     * Replace different bnodes that represent same OWL expression
     * by same bnode
     */
    void clean(Graph g, String[] lq) throws IOException, EngineException{
         QueryLoad ql = QueryLoad.create();
         QueryProcess exec = QueryProcess.create(g);
         for (String q : lq){
             String qq = ql.getResource(data + q);           
             exec.query(qq);            
         }
   }

}
