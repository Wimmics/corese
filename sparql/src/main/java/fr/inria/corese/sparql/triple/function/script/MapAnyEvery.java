package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import static fr.inria.corese.kgram.api.core.ExprType.MAPANY;
import static fr.inria.corese.kgram.api.core.ExprType.MAPEVERY;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.exceptions.EngineException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class MapAnyEvery extends Funcall {

    public MapAnyEvery() {}
    
    public MapAnyEvery(String name) {
        super(name);
        setArity(2);
    }
    
//    @Override
//    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
//        return evalnew(eval, b, env, p);
//    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype name = getBasicArg(0).eval(eval, b, env, p);
        IDatatype[] param = evalArguments(eval, b, env, p, 1);
        if (name == null || param == null) {
            return null;
        }

        Function function = null;
        try {
            function = getDefineGenerate(this, env, name.stringValue(), param.length);
        } catch (EngineException ex) {
            log(ex.getMessage());
        }
        if (function == null) {
            return null;
        }

        /**
         * every (xt:fun, ?list) every (xt:fun, ?x, ?list) every (xt:fun, ?l1,
         * ?l2) TODO: when getLoop() it works with only one loop error follow
         * SPARQ semantics of OR (any) AND (every)
         *
         * @return
         */
        boolean every = oper() == MAPEVERY;
        boolean any = oper() == MAPANY;
        IDatatype iter = null;
        Iterator<IDatatype> loop = null;
        boolean isList = false, isLoop = false;


        int k = 0;
        for (IDatatype dt : param) {
            if (dt.isList() || dt.isLoop()) {
                iter = dt;
                break;
            }
            else {
                k++;
            }
        }
        if (iter == null) {
            return null;
        }
        IDatatype[] value = param; 
        boolean error = false, ok = true;
        
        for (IDatatype elem : iter) {

            value[k] = elem;

            if (elem != null) {
                // iterator may return null when it ends
                IDatatype res = call(eval, b, env, p, function, value);

                if (res == null) {
                    error = true;
                } else {
                    if (every) {
                        if (!res.booleanValue()) {
                            return FALSE;
                        }
                    } else if (any) {
                        // any
                        if (res.booleanValue()) {
                            return TRUE;
                        }
                    }
                }
            }
        }
        
        if (error) {
            return null;
        }
        return value(every);


    }
    
     public IDatatype evalold(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype name = getBasicArg(0).eval(eval, b, env, p);
        IDatatype[] param = evalArguments(eval, b, env, p, 1);
        if (name == null || param == null) {
            return null;
        }

        Function function = null;
        try {
            function = getDefineGenerate(this, env, name.stringValue(), param.length);
        } catch (EngineException ex) {
            log(ex.getMessage());
        }
        if (function == null) {
            return null;
        }

        /**
         * every (xt:fun, ?list) every (xt:fun, ?x, ?list) every (xt:fun, ?l1,
         * ?l2) TODO: when getLoop() it works with only one loop error follow
         * SPARQ semantics of OR (any) AND (every)
         *
         * @return
         */
        boolean every = oper() == MAPEVERY;
        boolean any = oper() == MAPANY;
        IDatatype list = null;
        IDatatype ldt = null;
        Iterator<IDatatype> loop = null;
        boolean isList = false, isLoop = false;


        int k = 0;
        for (IDatatype dt : param) {
            if (dt.isList() && !isList && !isLoop) {
                isList = true;
                list = dt;
            } else if (dt.isLoop()) {
                if (!isList && !isLoop) {
                    isLoop = true;
                    ldt = dt;
                    loop = ldt.iterator();
                } else {
                    // list + loop || loop + loop
                    // snd_loop.toList()
                    param[k] = dt.toList();
                }
            }

            k++;
        }
        if (list == null && ldt == null) {
            return null;
        }
        IDatatype[] value = new IDatatype[param.length];
        boolean error = false, ok = true;
        
        for (int i = 0; (isList) ? i < list.size() : loop.hasNext(); i++) {

            for (int j = 0; j < value.length; j++) {
                IDatatype dt = param[j];
                if (dt.isList()) {
                    value[j] = (i < dt.size()) ? dt.get(i) : dt.get(dt.size() - 1);
                } else if (isLoop && dt.isLoop()) {
                    if (loop.hasNext()) {
                        // TODO:  track the case with several dt loop
                        value[j] = loop.next(); 
                        if (value[j] == null){
                            // some iterators (DataProducer) return null when there is no more element
                            ok = false;
                        }
                    } else {
                        return null;
                    }
                } else {
                    value[j] = dt;
                }
            }

            if (ok) {
                IDatatype res = call(eval, b, env, p, function, value);

                if (res == null) {
                    error = true;
                } else {
                    if (every) {
                        if (!res.booleanValue()) {
                            return FALSE;
                        }
                    } else if (any) {
                        // any
                        if (res.booleanValue()) {
                            return TRUE;
                        }
                    }
                }
            }
        }
        
        if (error) {
            return null;
        }
        return value(every);


    }
    
}
