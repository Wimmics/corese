package fr.inria.corese.sparql.triple.function.core;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.Processor;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Extern extends TermEval {

    public Extern() {}

    public Extern(String name) {
        super(name);
    }
    
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null || ! getProcessor().isCorrect()){
            return null;
        } 
        Processor proc = getProcessor();
        proc.compile();
        if (proc.getProcessor() instanceof FunctionEvaluator){
            FunctionEvaluator fe = (FunctionEvaluator) proc.getProcessor();
            fe.setProducer(p);
            fe.setEnvironment(env);
        }
        String name = proc.getMethod().getName();
        try {
            return (IDatatype) proc.getMethod().invoke(proc.getProcessor(), param);
        } catch (IllegalArgumentException e) {
           trace(e, "eval", name, param);
        } catch (IllegalAccessException e) {
            trace(e, "eval", name, param);
        } catch (InvocationTargetException e) {
           trace(e, "eval", name, param);
        } catch (NullPointerException e) {
           trace(e, "eval", name, param); 
        }
        return null;
    }
    
    void trace(Exception e, String title, String name, IDatatype[] ldt){
        String str = "";
        for (IDatatype dt : ldt) {
            str += dt + " ";
        }
        TermEval.logger.error(title + " "+ name + " " + str, e);  
    }
    
}
