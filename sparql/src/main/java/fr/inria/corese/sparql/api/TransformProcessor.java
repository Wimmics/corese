package fr.inria.corese.sparql.api;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.exceptions.EngineException;

/**
 * Transformer API
 * 
 * @author Olivier Corby - INRIA - 2018
 */
public interface TransformProcessor {
    
    int getLevel();
    
    void setLevel(int n);
    
    IDatatype tabulate();
    
    boolean isStart();
    
    IDatatype process(String temp, boolean all, String sep, Expr exp, Environment env)
            throws EngineException ;
    
    IDatatype process(String temp, boolean all, String sep, Expr exp, Environment env, 
            IDatatype dt, IDatatype[] args)
            throws EngineException;
    
    boolean isDefined(String name);
    
    Mappings getMappings();
    
}
