/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.edelweiss.kgraph.rule;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.ExpType;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.Regex;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.event.ResultListener;
import fr.inria.edelweiss.kgram.path.Path;

/**
 * Watch query solutions of rules for RuleEngine
 * Check that a solution at loop n contains at least one 
 * edge deduced at loop n-1 (that is a new edge)
 * Does not optimize with:
 * exists {}
 * Property Path
 * subquery
 * 
 * @author Olivier Corby, Wimmics Inria I3S, 2014
 *
 */
public class ResultWatcher implements ResultListener {
    
    int loop = 0;
    boolean isWatch = true;
    
    void setLoop(int n){
        loop = n;
    }

    void start(Rule r){
        isWatch = true;
    }
    
    void finish(Rule r){
        
    }
    
    /**
     * Check that environment contains at least one edge from preceding 
     * RuleEngine loop just before returning a solution
     */
    @Override
    public boolean process(Environment env) {
        if (loop == 0 || ! isWatch){
            return true;
        }
        for (Entity ent : env.getEdges()){
            if (ent != null && ent.getEdge().getIndex() == loop){
                return true;
            }
        }
        return false;
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
    public void listen(Exp exp) {
        switch (exp.type()){
            case Exp.PATH:
            case Exp.QUERY:                
                isWatch = false;
        }
    }
    
     @Override
   public void listen(Expr exp) {
        switch (exp.type()){
            case ExpType.EXIST: 
                isWatch = false;
        }
    }
}
