package fr.inria.corese.sparql.triple.function.template;

import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.Processor;



/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class TemplateFormat extends TemplateFunction {

    public TemplateFormat(String name) {
        super(name);
        setProxy(Processor.FORMAT);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {     
        if (oper() == ExprType.STL_FORMAT && isFuture()) {
            return future(eval, b, env, p);
        }
        return super.eval(eval, b, env, p);
    }   
       
}

