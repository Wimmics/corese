package fr.inria.corese.sparql.api;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.NSManager;

/**
 *
 * @author corby
 */
public interface ComputerProxy {
        
    TransformProcessor getTransformer(Environment env, Producer p) 
            throws EngineException ;
    
    TransformProcessor getTransformer(Environment env, Producer p, Expr exp, IDatatype uri, IDatatype gname) 
            throws EngineException;
    
    GraphProcessor getGraphProcessor();

    TransformVisitor getVisitor(Environment env, Producer p);
    
    Context getContext(Environment env, Producer p);
    
    NSManager getNSM(Environment env, Producer p);

}
