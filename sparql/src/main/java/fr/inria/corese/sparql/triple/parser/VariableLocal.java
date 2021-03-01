package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class VariableLocal extends Variable {
    private static Logger logger = LoggerFactory.getLogger(VariableLocal.class);
    
    public VariableLocal(String name){
        super(name);
        setSubtype(ExprType.LOCAL);
    }
    
    @Override
    public VariableLocal duplicate() {
        return new VariableLocal(getLabel());
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p){
        IDatatype val = b.get(this);
        if (val == null && ! b.isCoalesce()) {
            logger.error("Undefined variable: " + this);
        }
        return val;
    }
    
    @Override
    public IDatatype eval(Computer eval, Environment env, Producer p, IDatatype[] param){
        return param[getIndex()];
    }
    

}
