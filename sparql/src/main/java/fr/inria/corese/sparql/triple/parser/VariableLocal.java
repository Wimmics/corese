package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class VariableLocal extends Variable {
    
    public VariableLocal(String name){
        super(name);
        setSubtype(ExprType.LOCAL);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p){
        return b.get(this);
    }
    
    @Override
    public IDatatype eval(Computer eval, Environment env, Producer p, IDatatype[] param){
        return param[getIndex()];
    }
    

}
