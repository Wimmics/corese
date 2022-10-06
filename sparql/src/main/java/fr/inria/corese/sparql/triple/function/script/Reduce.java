package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.core.Expr;
import static fr.inria.corese.kgram.api.core.ExprType.AND;
import static fr.inria.corese.kgram.api.core.ExprType.CONCAT;
import static fr.inria.corese.kgram.api.core.ExprType.DIV;
import static fr.inria.corese.kgram.api.core.ExprType.MINUS;
import static fr.inria.corese.kgram.api.core.ExprType.MULT;
import static fr.inria.corese.kgram.api.core.ExprType.OR;
import static fr.inria.corese.kgram.api.core.ExprType.PLUS;
import static fr.inria.corese.kgram.api.core.ExprType.XT_APPEND;
import static fr.inria.corese.kgram.api.core.ExprType.XT_MERGE;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Processor;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * reduce(rq:plus, list)
 * get a binary function, apply it on elements two by two
 * a kind of aggregate
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Reduce extends Funcall {  
    
    public Reduce(){}
    
    public Reduce(String name){
        super(name);
        setArity(1);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype name    = getBasicArg(0).eval(eval, b, env, p);
        IDatatype[] param = evalArguments(eval, b, env, p, 1);
        if (name == null || param == null || param.length == 0) {
            return null;
        }
                
        Function function = null;
        try {
            function = getDefineGenerate(this, env, name.stringValue(), 2);
        } catch (EngineException ex) {
            log(ex.getMessage());
        }
        if (function == null) {
            return null;
        }
        IDatatype dt = param[0];
        if (! dt.isList()) {
            return null;
        }
        List<IDatatype> list = dt.getValues();
        if (list.isEmpty()){
            IDatatype res = neutral(function, name, dt);
            if (res == dt) {
                return neutral(eval, b, env, p, name, dt);
            }
            return res;
        }
        IDatatype[] value = new IDatatype[2];
        IDatatype res = list.get(0);
        value[0] = res;
        // Iterate the value list in order to perform binary function call
        // reduce (rq:plus, list) -> for all (x, y) in list : rq:plus(x, y)
        for (int i = 1; i < list.size(); i++) {            
            value[1] = list.get(i);            
            // binary function call
            res = call(eval, b, env, p, function, value);            
            if (res == null) {
               return null;
            }
            value[0] = res;
        }
        return res;
    }
    
    int functionOper(Function exp) {
        Expression body = exp.getBody();
        if (body.isTerm()) {
            return body.oper();
        }
        return -1;
    }
    
    IDatatype neutral(Function exp, IDatatype name, IDatatype dt){
        switch (functionOper(exp)){
            case OR:
                return FALSE;
                
            case AND:
                return TRUE;
                
            case CONCAT:
                return DatatypeMap.EMPTY_STRING;
                
            case PLUS:
            case MINUS:
                return DatatypeMap.ZERO;
                
            case MULT:
            case DIV:
                return DatatypeMap.ZERO; 
                
            case XT_APPEND:
            case XT_MERGE:
                return DatatypeMap.createList();
                
            default: return dt;
        }
    }
    
    // fun with no arg returns neutral element
    IDatatype neutral(Computer eval, Binding b, Environment env, Producer p, IDatatype name, IDatatype dt) throws EngineException {
        Function function = null;
        try {
            function = getDefineGenerate(this, env, name.stringValue(), 0);
        } catch (EngineException ex) {
            log(ex.getMessage());
        }
        if (function == null) {
            return dt;
        }
        return call(eval, b, env, p, function);
    }
    
    
}
