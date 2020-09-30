package fr.inria.corese.sparql.triple.function.extension;

import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;

/**
 *
 * @author corby
 */
public class Cast extends TermEval {
    kind akind = kind.DEFAULT;
    
    enum kind { DEFAULT, LIST };

    public Cast() {}
    
    public Cast(String name, String lname) {
        super(name);
        setArity(1);
        switch(lname) {
            case IDatatype.LIST_DATATYPE: 
                akind =  kind.LIST; break;
        }
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype dt = getBasicArg(0).eval(eval, b, env, p);
        if (dt == null) {
            return null;
        }
        switch (akind) {
            case LIST: return list(dt);
        }
        return null;
    }
    
    IDatatype list(IDatatype dt) {
        if (dt.isList()) {
            return dt;
        }
        return DatatypeMap.newList(dt);
    }
    
}
