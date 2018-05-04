package fr.inria.corese.sparql.api;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.query.Environment;

/**
 *
 * @author Olivier Corby - INRIA - 2018
 */
public interface TransformProcessor {
    
    int getLevel();
    
    void setLevel(int n);
    
    IDatatype tabulate();
    
    boolean isStart();
    
    IDatatype process(String temp, boolean all, String sep);
    
    IDatatype process(IDatatype dt, IDatatype[] args, 
            String temp, boolean all, String sep, Expr exp, Environment env);
    
}
