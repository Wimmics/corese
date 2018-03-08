package fr.inria.corese.sparql.triple.function.core;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.cg.datatype.CoreseDouble;
import fr.inria.corese.sparql.cg.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class ZeroaryFunction extends TermEval {
     
    public ZeroaryFunction(){}
    
    public ZeroaryFunction(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        switch (oper()){
            case ExprType.RANDOM:   return CoreseDouble.create(Math.random());
            case ExprType.NOW:      return DatatypeMap.newDate();
        } 
        return null;
    }
}
