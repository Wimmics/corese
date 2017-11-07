package fr.inria.corese.triple.function.proxy;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.corese.triple.function.script.Extension;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

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
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        switch (oper()){
            case ExprType.STL_PROCESS:
                if (isDefined || eval.getDefine(this, env) != null){
                    isDefined = true;
                    // extension function call:  st:process(?x)
                    return super.eval(eval, b, env, p);
                } else {
                    proxy = new TemplateFunction(getName());
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
