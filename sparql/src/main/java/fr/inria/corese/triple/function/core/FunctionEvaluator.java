package fr.inria.corese.triple.function.core;

import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author corby
 */
public interface FunctionEvaluator {
    
    void setProducer(Producer p);
    
    void setEnvironment(Environment e);
    
}
