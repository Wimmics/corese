package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.exceptions.LDScriptException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Processor;

/**
 *
 */
public class TryCatch extends Statement {
    
    public TryCatch() {
        super();
    }
    
    public TryCatch(Expression e1, Expression e2) {
        super(Processor.TRY_CATCH, e1, e2);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        try {
          IDatatype dt = getArg(0).evalWE(eval, b, env, p);
          return dt;
        }
        catch (LDScriptException e){
            // try {exp} catch (var) { exp } implemented as:
            // TryCatch(exp, let(var = xt:getDatatytpeValue()) { exp })
            // arg(0) = exp
            // arg(1) = let(var = xt:getDatatytpeValue()) { exp }
            b.setDatatypeValue(e.getDatatypeValue());
            IDatatype res = getArg(1).eval(eval, b, env, p);
            return res;
        }
    }

}
    