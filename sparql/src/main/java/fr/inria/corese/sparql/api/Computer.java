
package fr.inria.corese.sparql.api;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.exceptions.EngineException;

/**
 * Interface for Interpreter
 * @author corby
 */
public interface Computer extends ComputerProxy {
    
    ComputerEval getComputerEval(Environment env, Producer p, Expr function); 
        
    IDatatype function(Expr exp, Environment env, Producer p) throws EngineException;
    IDatatype exist(Expr exp, Environment env, Producer p) throws EngineException ;
        
    boolean isCompliant();
}
