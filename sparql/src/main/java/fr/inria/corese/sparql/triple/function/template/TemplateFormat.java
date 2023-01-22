package fr.inria.corese.sparql.triple.function.template;

import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
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
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {     
        if (oper() == ExprType.STL_FORMAT && isFuture()) {
            return future(eval, b, env, p);
        }
        
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null || param.length == 0){
            return null;
        }
       
        IDatatype format = param[0];
        String path = format.getLabel();
        
//        if (format.isURI()) {
//            check(Feature.LINKED_TRANSFORMATION, b, path, LINKED_FORMAT_MESS);
//            if (isFile(path)) {
//                check(Feature.READ_FILE, b, path, LINKED_FORMAT_MESS);
//            }
//        }
        
        if (format.isURI()) {
            if (isFile(path)) {
                // do not accept file path when accept list is empty
                Access.check(Feature.LINKED_TRANSFORMATION, b.getAccessLevel(), path, LINKED_FORMAT_MESS, false);
            }
            else {
                // may accept url path when accept list is empty
                check(Feature.LINKED_TRANSFORMATION, b, path, LINKED_FORMAT_MESS);
            }
        }
        
        return eval.getGraphProcessor().format(param);
    }   
       
}

