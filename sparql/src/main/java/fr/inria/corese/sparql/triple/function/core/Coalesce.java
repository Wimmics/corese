package fr.inria.corese.sparql.triple.function.core;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Coalesce extends TermEval {

    public Coalesce(){}

    public Coalesce(String name) {
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        for (Expression arg : getArgs()) {
            boolean save = b.isCoalesce();
            b.setCoalesce(true);
            IDatatype dt = arg.eval(eval, b, env, p);
            b.setCoalesce(save);
            if (dt != null) {
                return dt;
            }
        }
        return null;
    }
}
