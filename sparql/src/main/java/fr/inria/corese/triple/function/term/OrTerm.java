package fr.inria.corese.triple.function.term;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class OrTerm extends TermEval {
    
    public OrTerm(String name) {
        super(name);
    }
    
    public OrTerm(String name, Expression e1, Expression e2){
        super(name, e1, e2);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        boolean error = false;
        for (Expression arg : getArgs()) {
            IDatatype o = arg.eval(eval, b, env, p);
            if (o == null || !o.isTrueAble()) {
                error = true;
            } else if (isTrue(o)) {
                return TRUE;
            }
        }
        if (error) {
            return null;
        }
        return FALSE;
    }

   
}
