package fr.inria.corese.sparql.triple.function.extension;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class SystemFunction extends TermEval {

     public SystemFunction(){}
     
     public SystemFunction(String name){
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null) return null;
        
        switch (oper()) {
            case ExprType.DEBUG: debug(param[0], eval, b, env, p);
        }
        
        return TRUE;
    }
    
    void debug(IDatatype dt, Computer eval, Binding b, Environment env, Producer p) {
        b.setDebug(dt.booleanValue());
    }
    
}
