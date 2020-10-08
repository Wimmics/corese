package fr.inria.corese.sparql.triple.function.template;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.TransformProcessor;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class CallTemplate extends TemplateFunction {  
        
    public CallTemplate(String name){
        super(name);
        setArity(1);
    }
    
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null) {
            return null;
        }

        TransformProcessor trans = eval.getTransformer(b, env, p, this, null, null);

        switch (param.length) {
            case 1:
                return trans.process(param[0].getLabel(), isAll(), getModality(), this, env);
            case 2:
                return trans.process(param[0].getLabel(), isAll(), getModality(), this, env, param[1], null);
            default:
                return trans.process(param[0].getLabel(), isAll(), getModality(), this, env, param[1], getParam(param, 1));
        }
    }
    
      
}

