package fr.inria.corese.sparql.triple.function.template;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.TransformProcessor;
import fr.inria.corese.sparql.triple.parser.Access;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class CallTemplateWith extends TemplateFunction {  
        
    public CallTemplateWith(String name){
        super(name);
        setArity(2);
    }
    
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null) {
            return null;
        }
        
        String uri = param[0].getLabel();
        
        check(Access.Feature.LINKED_TRANSFORMATION, b, uri, LINKED_TRANSFORMATION_MESS);

        TransformProcessor trans = eval.getTransformer(b, env, p, this, param[0], null);

        switch (param.length) {           
            case 2:
                return trans.process(param[1].getLabel(), isAll(), getModality(), this, env);
            default:
                return trans.process(param[1].getLabel(), isAll(), getModality(), this, env, param[2], getParam(param, 2));
        }
    }
      
}

