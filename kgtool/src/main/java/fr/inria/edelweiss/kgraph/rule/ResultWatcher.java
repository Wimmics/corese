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
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.event.ResultListener;
import fr.inria.edelweiss.kgram.path.Path;
import fr.inria.edelweiss.kgraph.api.GraphListener;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.RDFS;
import fr.inria.edelweiss.kgraph.query.Construct;
import fr.inria.edelweiss.kgraph.rule.RuleEngine.ITable;
import java.util.ArrayList;
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
public class ResultWatcher implements ResultListener, GraphListener {    
      
    int loop = 0, ruleLoop = 0;
    int cpos = 0, cneg = 0;
    int cnode = 0;
    boolean isWatch = true, start = true;
    private boolean isDistinct = true;
    private boolean isSkipPath = false;
    private boolean test = false;
    boolean isNew = false;
    int index = -1;
    
    Construct cons;
    Mappings map;
    Rule rule;
    Graph graph;
    Distinct dist;
    ArrayList<Entity> list;
    private boolean trace;
    
    ResultWatcher(Graph g){
        graph = g;  
        list = new ArrayList<Entity>();
    }
    
    public Distinct getDistinct(){
        return dist;
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
        isNew = false;
        start = true;
        init(r);
    }
    
   /**
    * If there is only one predicate in where with new edges
    * (Only one occurrence of this predicate in where)
    * We can focus on these new edge using listen(Edge, Entity)
    */
    void start(ITable t) {
        if (t.getCount() == 1 
                && rule.getQuery().nbPredicate(t.getPredicate()) == 1) {
            index = rule.getQuery().getEdge(t.getPredicate()).getIndex();
            if (index != -1) {
                isNew = true;
            }            
        }
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
            return store(env);
        }
      
        if (loop == 0 || isNew){
            return store(env);
        }
        
        for (Entity ent : env.getEdges()){
            
            if (ent != null && ent.getEdge().getIndex() >= ruleLoop){
                    return store(env);
            }
        }
        
        cneg += 1;
        return false;
    }
    
    
    boolean store(Environment env){
        if (isDistinct && dist != null){
            // select distinct * on construct variables
            if (! dist.isDistinct(env)){  
                cneg += 1;
                return false;
            }
        }
        cpos+= 1;
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
    public Exp listen(Exp exp, int n) {
        switch (exp.type()) {
            case Exp.PATH:
                if (isSkipPath){
                    // skip path to check if a solution has new edges
                }
                else {
                    // do not skip path to check if a solution has new edges
                   isWatch = false;   
                }
                break;
                
            case Exp.QUERY:
                isWatch = false;
                break;
                
            case Exp.UNION:
            case Exp.OPTION:
                // because we may not go through the branch with new edges
                // check new at the end as usual
                isNew = false;
                break;
        }

        if (n == 0 && exp.type() == Exp.AND
                && rule.isGTransitive()) {
            if (rule.getQuery().getEdgeList() != null) {
                // exp = where { ?p a owl:TransitiveProperty . ?x ?p ?y . ?y ?p ?z }
                // there is a list of candidates for ?x ?p ?y
                // skip first query edge: skip exp.get(0)
                exp = Exp.create(Exp.AND, exp.get(1), exp.get(2));
            }
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
    
    
    
    /**************************************************************
     * 
     *  GraphListener
     * 
     *************************************************************/
    
    
    public void clear(){
        list.clear();
    }
    
    public List<Entity> getList(){
        return list;
    }

    @Override
    public void addSource(Graph g) {
    }

    @Override
    public boolean onInsert(Graph g, Entity ent) {
        return true;   
    }

    @Override
    public void insert(Graph g, Entity ent) {
        // TODO
        if (ent.getEdge().getLabel().equals(RDFS.SUBCLASSOF)){
            list.add(ent);
        }
    }

    @Override
    public void delete(Graph g, Entity ent) {
    }

    @Override
    public void start(Graph g, Query q) {
    }

    @Override
    public void finish(Graph g, Query q, Mappings m) {
    }

    @Override
    public void load(String path) {
    }

    /**
     * @return the isSkipPath
     */
    public boolean isSkipPath() {
        return isSkipPath;
    }

    /**
     * @param isSkipPath the isSkipPath to set
     */
    public void setSkipPath(boolean isSkipPath) {
        this.isSkipPath = isSkipPath;
    }

    /**
     * @return the isDistinct
     */
    public boolean isDistinct() {
        return isDistinct;
    }

    /**
     * @param isDistinct the isDistinct to set
     */
    public void setDistinct(boolean isDistinct) {
        this.isDistinct = isDistinct;
    }

    /**
     * @return the test
     */
    public boolean isTest() {
        return test;
    }

    /**
     * @param test the test to set
     */
    public void setTest(boolean test) {
        this.test = test;
    }

    @Override
    public boolean listen(Edge edge, Entity ent) {
        if (isNew  
                && edge.getIndex() == index 
                && ent.getEdge().getIndex() < ruleLoop){
             return false;           
        }
        return true;
    }

    /**
     * @return the trace
     */
    public boolean isTrace() {
        return trace;
    }

    /**
     * @param trace the trace to set
     */
    public void setTrace(boolean trace) {
        this.trace = trace;
    }
    
   
     public boolean isNew(){
         return isNew;
     }
     
     
     
     
     
}
