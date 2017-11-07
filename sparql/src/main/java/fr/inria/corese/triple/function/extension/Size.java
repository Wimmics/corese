package fr.inria.corese.triple.function.extension;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.corese.triple.function.term.TermEval;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Size extends TermEval {

    public Size(String name){
        super(name);
        setArity(1);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype dt = getBasicArg(0).eval(eval, b, env, p);       
        if (dt == null) {
            return null;
        }
        if (dt.isList()){
            return DatatypeMap.size(dt);
        }
        if (dt.isPointer()){
            return DatatypeMap.newInstance(dt.getPointerObject().size());                    
        }
        return null;
    }
    
}
