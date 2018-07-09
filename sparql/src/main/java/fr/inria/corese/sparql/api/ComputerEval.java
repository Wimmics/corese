package fr.inria.corese.sparql.api;

import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.core.Eval;

/**
 * Interface for Eval
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 */
public interface ComputerEval {
    
    Computer getComputer();
    Environment getEnvironment();
    Eval getEval();
    
}
