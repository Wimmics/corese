package fr.inria.corese.compiler.eval;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.slf4j.LoggerFactory;


/**
 * SPARQL Java extension functions  
 * cs:test()
 * prefix cs: <http://ns.inria.fr/sparql-custom/>
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Custom {
    
    public IDatatype eval(Expr exp, Environment env, Producer p, Object[] param) {
        try {
            return evalWE(exp, env, p, param);
        } catch (NoSuchMethodException ex) {
            LoggerFactory.getLogger(Custom.class.getName()).error(ex.getMessage());
        } catch (IllegalAccessException ex) {
            LoggerFactory.getLogger(Custom.class.getName()).error(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            LoggerFactory.getLogger(Custom.class.getName()).error(ex.getMessage());
        } catch (InvocationTargetException ex) {
            LoggerFactory.getLogger(Custom.class.getName()).error(ex.getMessage());
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
    
    
    public IDatatype sort(IDatatype list){
        if (list.isList()){
            try {
                sort(list.getValues());
            } catch (CoreseDatatypeException ex) {
                LoggerFactory.getLogger(Custom.class.getName()).error("", ex);
            }
        }
        return list;
    }
    
    void sort(List<IDatatype> l) throws CoreseDatatypeException {
        for (int i = l.size() - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                if (l.get(j + 1).less(l.get(j))) {
                    IDatatype tmp = l.get(j + 1);
                    l.set(j + 1, l.get(j));
                    l.set(j, tmp);
                }
            }
        }
    }

}
