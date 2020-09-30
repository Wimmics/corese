package fr.inria.corese.sparql.triple.function.extension;

import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;

public class SPINFormater extends TermEval {

    public SPINFormater() {}
    
    public SPINFormater(String name) {
        super(name);
        setArity(1);
    }
        
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype dt = getArg(0).eval(eval, b, env, p);
        if (dt == null) {
            return null;
        }
        return eval.getGraphProcessor().spin(dt);
    }
}
