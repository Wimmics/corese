package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.SafetyException;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Call Corese Java function with parameters as IDatatype values
 * and this object possibly as getObject()
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class JavaDScall extends JavaFunction {
    
    String javaName;

    public JavaDScall() {}
    
    public JavaDScall(String name) {
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
        
        Class<IDatatype>[] types = new Class[param.length];
        Arrays.fill(types, IDatatype.class);
        
        try {
            Method meth = object.getClass().getMethod(javaName, types);
            Object obj = meth.invoke(object, param); 
            IDatatype res = DatatypeMap.getValue(obj);
            return res;
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(Javacall.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }
         
}
