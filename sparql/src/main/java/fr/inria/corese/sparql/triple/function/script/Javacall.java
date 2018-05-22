package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Call Corese Java function with parameters as getObject() or
 * Java values of IDatatype parameters
 * and this object possibly as getObject()
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Javacall extends TermEval {
    
    String javaName;

    public Javacall() {}
    
    public Javacall(String name) {
        super(name); 
        javaName = name.substring(name.indexOf(":")+1);
        setArity(1);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype dt   = getBasicArg(0).eval(eval, b, env, p);
        IDatatype[] param = evalArguments(eval, b, env, p, 1);  
        if (dt == null || param == null) {
            return null;
        }
        Object object = dt;
        if (dt.getObject() != null) {
            object = dt.getObject();
        }
        
        Object[] values = new Object[param.length];
        Class<Object>[] types = getClasses(param, values);
        
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
    Class[] getClasses(IDatatype[] param, Object[] values) {
        Class<? extends Object>[] types = new Class[param.length];
        int i = 0;
        for (IDatatype val : param) {
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
                    if (val.getObject() != null) {
                        values[i] = val.getObject();
                    } else {
                        values[i] = val;
                    }
            }                                   
            types[i] = val.getJavaClass();
            if (types[i] == null){
                types[i] = IDatatype.class;
            }
            i++;
        }
        return types;
    }
    
}
