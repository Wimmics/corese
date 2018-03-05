package fr.inria.edelweiss.kgraph.rule;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import java.io.IOException;

/**
 * Remove redundant bnodes from an RDF/OWL graph
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
         // escape QueryProcess write lock in case 
         // RuleEngine was run by Workflow Manager by init() by query()
         // because query() have read lock
         // it works because init() is also synchronized
         exec.setSynchronized(true);
         for (String q : lq){
             String qq = ql.getResource(data + q); 
             exec.query(qq);            
         }
   }
    
      /**
     * Replace duplicate OWL expressions by one of them
     * DRAFT
     */
    void owlrlFull() throws IOException, EngineException{
        QueryLoad ql = QueryLoad.create();
        QueryProcess exec = QueryProcess.create(graph);
        String unify = ql.getResource("/query/unify2.rq");
        // remove triples with obsolete bnodes as subject
        String clean = ql.getResource("/query/clean.rq");
        // tell Transformer to cache st:hash transformation result
        exec.getEvaluator().setMode(Evaluator.CACHE_MODE);
        // replace duplicate OWL expressions by one of them
        Mappings m1 = exec.query(unify);        
        Mappings m2 = exec.query(clean);       
        exec.getEvaluator().setMode(Evaluator.NO_CACHE_MODE);
    }

}
