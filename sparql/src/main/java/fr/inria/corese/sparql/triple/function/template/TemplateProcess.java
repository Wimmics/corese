package fr.inria.corese.sparql.triple.function.template;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.script.Extension;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;

/**
 * st:process(?x) can be overloaded by
 * function st:process(?x) { body }
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class TemplateProcess extends Extension  {
    boolean isDefined = false;
    TemplateFunction proxy;
    
    public TemplateProcess(String name) {
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        switch (oper()){
            case ExprType.STL_PROCESS:
                if (isDefined || getDefine(this, env) != null){
                    isDefined = true;
                    // extension function call:  st:process(?x)
                    return super.eval(eval, b, env, p);
                } else {
                    proxy = new Turtle(getName());
                    fill(proxy);
                    proxy.setOper(ExprType.TURTLE);
                }
                // continue
                
            default:
                // proxy: st:turtle(?x)
                return proxy.eval(eval, b, env, p);
        }
    }
    
    
}
