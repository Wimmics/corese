package fr.inria.corese.core.rule;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Evaluator;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.kgram.api.query.ProcessVisitor;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import java.io.IOException;
import java.util.Date;

/**
 * Remove redundant bnodes from an RDF/OWL graph
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class Cleaner {
    public static final int OWL = 0;
    static final String data = "/query/clean/";
    //static final String[] queries = {"ui2.rq", "ui3.rq", "ui4.rq", "allsome.rq", "card.rq"};
    static final String[] queries = { "allsome.rq", "card.rq", "ui2.rq", "ui3.rq", "ui4.rq"};
    
    Graph graph;
    private ProcessVisitor visitor;
    
    public Cleaner(Graph g){
        graph = g;
    }
    
    void clean(int mode) throws IOException, EngineException{
        switch (mode){
            
            case OWL: 
                clean();
                break;
        }
    }
    
    public void clean() throws IOException, EngineException {
        clean(graph, queries);
    }
      
    /**
     * Replace different bnodes that represent same OWL expression
     * by same bnode
     */
    void clean(Graph g, String[] lq) throws IOException, EngineException{
        Date d1 = new Date();
         QueryLoad ql = QueryLoad.create();
         QueryProcess exec = QueryProcess.create(g);
         // escape QueryProcess write lock in case 
         // RuleEngine was run by Workflow Manager by init() by query()
         // because query() have read lock
         // it works because init() is also synchronized
         exec.setSynchronized(true);
         for (String q : lq){
             String qq = ql.getResource(data + q); 
             //DatatypeValue dt = getVisitor().prepareEntailment(DatatypeMap.newInstance(qq));
             Mappings map = exec.query(qq, createMapping(getVisitor()));
         }
         Date d2 = new Date();
         System.out.println("Clean: " + ((d2.getTime() - d1.getTime()) / 1000.0));
   }
    
    Mapping createMapping(ProcessVisitor vis) {
        Binding b = Binding.create();
        b.setVisitor(vis);
        return Mapping.create(b);
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

    /**
     * @return the visitor
     */
    public ProcessVisitor getVisitor() {
        return visitor;
    }

    /**
     * @param visitor the visitor to set
     */
    public void setVisitor(ProcessVisitor visitor) {
        this.visitor = visitor;
    }

}
