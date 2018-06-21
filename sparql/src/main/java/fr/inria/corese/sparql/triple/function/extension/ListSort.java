package fr.inria.corese.sparql.triple.function.extension;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.triple.function.script.Funcall;
import fr.inria.corese.sparql.triple.function.script.Function;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class ListSort extends Funcall {

    public ListSort(){}
    
    public ListSort(String name){
        super(name);
        setArity(1);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype list = getBasicArg(0).eval(eval, b, env, p);
        if (list == null) {
            return null;
        }
        if (arity() == 2) {            
             return sort(eval, b, env, p, list);            
        }
        return DatatypeMap.sort(list);
    }
    
    IDatatype sort(Computer eval, Binding b, Environment env, Producer p, IDatatype list) {
        IDatatype f = getBasicArg(1).eval(eval, b, env, p);
        Function function = (f == null) ? null : (Function) eval.getDefineGenerate(this, env, f.stringValue(), 2);
        if (function == null) {
            return DatatypeMap.sort(list);
        } 
        List<IDatatype> l = list.getValueList();
        l.sort((IDatatype dt1, IDatatype dt2) -> {
            return call(eval, b, env, p, function, dt1, dt2).intValue();
        });
        if (list.isList()) {
            return list;
        }
        return DatatypeMap.newList(l);
    }
    
}
