package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import static fr.inria.corese.sparql.triple.function.term.TermEval.JAVA_FUNCTION_MESS;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Call Corese Java function with parameters as getObject() or
 * Java values of IDatatype parameters
 * and this object possibly as getObject()
 * 
 * java:setNode(?m, cast:node(?var), cast:node(?val))
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Javacall extends JavaFunction {
    
    private static final String NODE     = NSManager.CAST+"node";
    private static final String DATATYPE = NSManager.CAST+"datatype";
    
    String javaName;

    public Javacall() {}
    
    public Javacall(String name) {
        super(name); 
        javaName = name.substring(name.indexOf(":")+1);
        setArity(1);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        check(Feature.JAVA_FUNCTION, b, JAVA_FUNCTION_MESS);
        IDatatype dt   = getBasicArg(0).eval(eval, b, env, p);
        IDatatype[] param = evalArguments(eval, b, env, p, 1);  
        if (dt == null || param == null) {
            return null;
        }
        Object object = dt;
        if (dt.getNodeObject() != null) {
            object = dt.getNodeObject();           
        }
        
        Object[] values       = new Object[param.length];
        Class<Object>[] types = new Class[param.length];
        cast(param, values, types, p);
        
        try {
            Method meth = object.getClass().getMethod(javaName, types);
            Object obj = meth.invoke(object, values); 
            IDatatype res = DatatypeMap.getValue(obj);
            return res;
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(Javacall.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }
       
    /**
     * Cast IDatatype parameters as Java values and return appropriate classes
     */
    void cast(IDatatype[] param, Object[] values, Class[] types, Producer p) {
        int i = 0;
        for (IDatatype val : param) {
            // param[i] = getArg(i+1)
            if (getArg(i+1).oper() == ExprType.JAVACAST) { 
                cast(val, i, values, types, p);
            }
            else {
                cast(val, i, values, types);
            }
           i++;
        }
    }
    
    // default cast
    void cast(IDatatype val, int i, Object[] values, Class[] types) {
        switch (val.getCode()) {
            case IDatatype.INTEGER:
                values[i] = val.intValue();
                break;
            case IDatatype.DOUBLE:
            case IDatatype.FLOAT:
            case IDatatype.DECIMAL:
                values[i] = val.doubleValue();
                break;
            case IDatatype.STRING:
            case IDatatype.LITERAL:
            case IDatatype.URI:
                values[i] = val.stringValue();
                break;
            case IDatatype.BOOLEAN:
                values[i] = val.booleanValue();
                break;
            default:
                if (val.getNodeObject() != null) {
                    values[i] = val.getNodeObject();
                } else {
                    values[i] = val;
                }
        }
        types[i] = val.getJavaClass();
        if (types[i] == null) {
            types[i] = IDatatype.class;
        }
    }
    
    /**
     * param[i] = getArg(i+1) = cast:node(exp)
     */
    void cast(IDatatype dt, int i, Object[] values, Class[] types, Producer p) {
        String target = getArg(i+1).getLabel();
        switch (target) {
            case NODE:
                values[i] = p.getNode(dt);
                types[i] = Node.class;
                break;
            case DATATYPE:    
                values[i] = dt;
                types[i] = IDatatype.class;
                break;
            default:
                cast(dt, i, values, types);
        }
    }
    
    
}
