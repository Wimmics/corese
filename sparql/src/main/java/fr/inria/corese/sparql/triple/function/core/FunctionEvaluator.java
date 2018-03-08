package fr.inria.corese.sparql.triple.function.core;

import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author corby
 */
public interface FunctionEvaluator {
    
    void setProducer(Producer p);
    
    void setEnvironment(Environment e);
    
}
