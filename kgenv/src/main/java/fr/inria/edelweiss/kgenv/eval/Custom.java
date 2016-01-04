package fr.inria.edelweiss.kgenv.eval;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.log4j.Logger;


/**
 * SPARQL Java extension functions  
 * cs:test()
 * prefix cs: <http://ns.inria.fr/sparql-custom/>
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Custom {
    
    IDatatype eval(Expr exp, Environment env, Producer p, Object[] param) {
        try {
            return evalWE(exp, env, p, param);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(Custom.class.getName()).error(ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Custom.class.getName()).error(ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(Custom.class.getName()).error(ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(Custom.class.getName()).error(ex);
        }
        return null;
    }
      
    IDatatype evalWE(Expr exp, Environment env, Producer p, Object[] param) 
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        Class<IDatatype>[] signature = getSignature(param.length);
        Method m = getClass().getMethod(exp.getShortName(), signature);
        Object res = m.invoke(this, param);
        return (IDatatype) res;
    }
    
    Class<IDatatype>[] getSignature(int n){
        Class<IDatatype>[] signature = new Class[n];
        for (int i = 0; i < signature.length; i++) {
            signature[i] = IDatatype.class;
        }
        return signature;
    }
    
    public IDatatype test(IDatatype dt){
        return dt;
    }
    
    public IDatatype fib(IDatatype dt){
        int n = dt.intValue();
        if (n <= 2){
            return DatatypeMap.newInstance(1);
        }
        else {
            return fib(DatatypeMap.newInstance(n - 1)).plus(fib(DatatypeMap.newInstance(n - 2)));
        }
    }
    
        public IDatatype fibon(IDatatype dt){
            return DatatypeMap.newInstance(fibo(dt.intValue()));
        }

    int fibo(int n){
        if (n <= 2){
            return 1;
        }
        return fibo(n - 1) + fibo (n -2);
    }

}
