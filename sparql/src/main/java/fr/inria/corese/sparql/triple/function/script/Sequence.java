package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.triple.parser.ASTBuffer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Sequence extends LDScript {  
    
    public Sequence(){}
    
    public Sequence(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype res = TRUE;
        for (Expression exp : getArgs()) {
            res = exp.eval(eval, b, env, p);
            if (res == null) {
                return null;
            }
            if (b.isResult()) {
                return res;
            }
        }
        return res;
    }
    
    @Override
    public void tailRecursion(Function fun){
        if (getArity() > 0){
            getArg(getArity() - 1).tailRecursion(fun);
        }
    }
    
    @Override
    public ASTBuffer toString(ASTBuffer sb) {
        int i = 0;
        for (Expression exp : getArgs()) {
            if (i++ > 0) {
                sb.append(" ;");
                sb.nl();
            }
            exp.toString(sb);
        }
        return sb;
    }
   
}
