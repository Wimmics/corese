package fr.inria.corese.sparql.triple.function.extension;

import fr.inria.corese.kgram.api.core.Pointerable;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.script.Funcall;
import fr.inria.corese.sparql.triple.function.script.Function;
import java.util.List;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class ListSort extends Funcall {
    private static Logger logger = LoggerFactory.getLogger(ListSort.class);

    public ListSort(){}
    
    public ListSort(String name){
        super(name);
        setArity(1);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype list = getBasicArg(0).eval(eval, b, env, p);
        if (list == null) {
            return null;
        }
        if (arity() == 2) {
            IDatatype funName = getBasicArg(1).eval(eval, b, env, p);
            Function function;
            try {
                function = (funName == null) ? null : 
                        getDefineGenerate(this, env, funName.stringValue(), 2);
            } catch (EngineException ex) {
                log(ex.getMessage());
                return null;
            }
            if (function == null) {
                return DatatypeMap.sort(list);
            }
            return sort(eval, b, env, p, function, list);
        }
        return DatatypeMap.sort(list);
    }
    
    public IDatatype sort(Computer eval, Binding b, Environment env, Producer p, Function function, IDatatype dt)  {             
        List<IDatatype> l = dt.getValueList();
        l.sort((IDatatype dt1, IDatatype dt2) -> {
            IDatatype res = null;
            try {
                res = call(eval, b, env, p, function, dt1, dt2);
            } catch (EngineException ex) {
                
            }
            if (res == null) {
                logger.error(String.format("sort error: %s %s %s" , dt1 , dt2, function));
                return dt1.compareTo(dt2);
            }
            return res.intValue();
        });
        if (dt.isList()) {
            return dt;
        }
        return toList(l, dt);
    }
    
    IDatatype toList(List<IDatatype> list, IDatatype dt) {
        switch (dt.pointerType()) {
            case MAPPINGS:
                return toListMappings(list, dt);
        }
        return DatatypeMap.newList(list);
    }
    
    IDatatype toListMappings(List<IDatatype> list, IDatatype dt) {
        Mappings map = dt.getPointerObject().getMappings();
        int i = 0;
        for (IDatatype m : list) {
            map.set(i++, m.getPointerObject().getMapping());
        }
        return dt;
    }
    
    

}
