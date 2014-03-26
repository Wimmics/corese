/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.edelweiss.kgraph.rule;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.ExpType;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Regex;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Distinct;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.event.ResultListener;
import fr.inria.edelweiss.kgram.path.Path;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.Construct;
import java.util.List;

/**
 * Watch kgram query solutions of rules for RuleEngine
 * 1 Check that a solution at loop n contains at least one 
 * edge deduced at loop n-1 (that is a new edge)
 * 2 Eliminate duplicate solution by a select distinct * on construct variables
 * 3 Create Edge directly (without Mapping created)
 * Does not optimize with:
 * exists {}
 * Property Path
 * subquery
 * 
 * @author Olivier Corby, Wimmics Inria I3S, 2014
 *
 */
public class ResultWatcher implements ResultListener {    
      
    int loop = 0, ruleLoop = 0;
    int cpos = 0, cneg = 0;
    int cnode = 0;
    boolean isWatch = true, start = true;
    
    Construct cons;
    Mappings map;
    Rule rule;
    Graph graph;
    Distinct dist;
    
    ResultWatcher(Graph g){
        graph = g;    
    }
    
    
    void setConstruct(Construct c){
        cons = c;
    }
    
    void setMappings(Mappings m){
        map = m;
    }
    
    void setLoop(int n){
        ruleLoop = n;
    }
    
    void start(int n){
        loop = n;
    }

    void start(Rule r){
        rule = r;
        isWatch = true;
        start = true;
        init(r);
    }
    
    /**
     * set up a distinct * on construct variables
     * hence do not apply rule twice on same solution
     */
    void init(Rule r){
        List<Node> list = r.getQuery().getConstructNodes();
        if (list != null && ! list.isEmpty()){
            dist = Distinct.create(list);
        }
    }
    
    void finish(Rule r){
        dist = null;
    }
    
    /**
     * Environment contain a candidate solution
     * Check that environment contains at least one new edge from preceding 
     * RuleEngine loop 
     * Check that this solution is not duplicate: select distinct * on construct variables
     * This function is called by kgram just before returning a solution
     */
    @Override
    public boolean process(Environment env) {
        if (! isWatch){
            cpos += 1;
            return store(env);
        }
      
        if (loop == 0){
            cpos += 1;           
            return store(env);
        }
        
        for (Entity ent : env.getEdges()){
            
            if (ent != null && ent.getEdge().getIndex() >= ruleLoop){
                    cpos += 1;
                    return store(env);
            }
        }
        
        cneg += 1;
        return false;
    }
    
    
    boolean store(Environment env){
        if (dist != null){
            // select distinct * on construct variables
            if (! dist.isDistinct(env)){               
                return false;
            }
        }
        if (cons == null){
            // Mapping created by kgram
            return true;
        }
        else {
            // create Edge 
            // no Mapping created by kgram
          cons.construct(map, env, null);
           return false;
        }
    }

    @Override
    public boolean process(Path path) {
        return true;
    }

    @Override
    public boolean enter(Entity ent, Regex exp, int size) {
         return true;
   }

    @Override
    public boolean leave(Entity ent, Regex exp, int size) {
         return true;
   }

    @Override
    public Exp listen(Exp exp) {      
        switch (exp.type()){
            case Exp.PATH:
            case Exp.QUERY:                
                isWatch = false;
        }
        
        return exp;
    }
    
 
    
     @Override
   public void listen(Expr exp) {
        switch (exp.type()){
            case ExpType.EXIST: 
                isWatch = false;
        }
    }
     
     public String toString(){
         return "positive: " + cpos + "\n"
              + "negative: " + cneg;
     }
     
  
     
     
     
     
     /*********************************************************************
      * 
      * 
      */
     
     
     
    Exp compile(Exp exp){
        Exp e1    = Exp.create(Exp.EDGE, exp.get(1).getEdge());
        Exp e2    = Exp.create(Exp.EDGE, exp.get(0).getEdge());       
        Exp ee    = Exp.create(Exp.AND, e1, e2); 
        exp.get(0).setIndex(ruleLoop);
        e1.setIndex(ruleLoop);
        Exp union = Exp.create(Exp.UNION, exp, ee);
        return union;
    }
    
    boolean isTransitive(Rule r){
        Exp exp = r.getQuery().getBody();              
        if (exp.type() != Exp.AND
            || r.getPredicates().size() != 1
            || exp.size() != 2){
            return false;
        }
        
        for (Exp ee : exp){
            if (! ee.isEdge()){
                return false;
            }
            Edge edge = ee.getEdge();
            if (edge.getEdgeVariable() != null){
                return false;
            }
        }
        
        return true;
    }
     
     
     
     
     
     
     
}
