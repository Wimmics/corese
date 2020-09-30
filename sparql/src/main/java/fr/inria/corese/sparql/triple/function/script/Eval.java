package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.kgram.api.core.PointerType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.Expression;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class Eval extends LDScript {

    public Eval() {}
    
    public Eval(String name) {
        super(name);
        setArity(1);
    }
    
    /**
     * eval(?e) where ?e is a IDatatype Pointer which contains an expression  
     * use case:
     * @error function us:error(?e) { eval(?e) }
     */
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype dt = getBasicArg(0).eval(eval, b, env, p); 
        if (dt == null || dt.pointerType() != PointerType.EXPRESSION) {
            return dt;
        }
        Expression exp = (Expression) dt.getPointerObject();
        return exp.eval(eval, b, env, p);
    }
    
}
