/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.sparql.triple.function.core;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class HashFunction extends TermEval {

    public HashFunction() {}
    
    public HashFunction(String name) {
        super(name);
        setArity(1);
    }
    
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype dt = getBasicArg(0).eval(eval, b, env, p);
        if (dt == null) {
            return null;
        }
        String res = new Hash(getModality()).hash(dt.getLabel());
        if (res == null) {
            return null;
        }
        return DatatypeMap.newLiteral(res);
    }
    
}
