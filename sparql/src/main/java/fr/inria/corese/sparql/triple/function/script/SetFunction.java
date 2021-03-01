package fr.inria.corese.sparql.triple.function.script;

import static fr.inria.corese.kgram.api.core.ExprType.SET;
import static fr.inria.corese.kgram.api.core.ExprType.STATIC;
import static fr.inria.corese.kgram.api.core.ExprType.STATIC_UNSET;
import static fr.inria.corese.kgram.api.core.ExprType.UNSET;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class SetFunction extends LDScript {  
    
    public SetFunction(){}
    
    public SetFunction(String name){
        super(name);
        //setArity(2);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException { 
        switch (oper()) {
            case UNSET:
                // without get static var 
                IDatatype dt = b.getBasic(getBasicArg(0).getVariable(), false);
                if (dt != null) {
                    b.unbind(this, getBasicArg(0).getVariable());
                }
                return dt;
                
            case STATIC_UNSET:
                IDatatype sdt = Binding.getStaticVariable(getBasicArg(0).getLabel());
                if (sdt != null) {
                    Binding.setStaticVariable(getBasicArg(0).getLabel(), null);
                }
                return sdt;
        }
        
        IDatatype val = getBasicArg(1).eval(eval, b, env, p);
        if (val == null) {
            return null;
        }
        
        switch (oper()) {            
            case SET: b.bind(this, getBasicArg(0).getVariable(), val);
            break;
            case STATIC:
                Binding.setStaticVariable(getBasicArg(0).getLabel(), val);
            
        }
        return val;
    }   
   
}
