package fr.inria.corese.sparql.triple.function.extension;

import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.GraphProcessor;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;

/**
 * dt:list(term) -> cast term to list
 * dt:graph(dt:mappings amap) -> create the Mappings graph with W3C Results schema.
 */
public class Cast extends TermEval {
    
    static final String MAPPINGS_DATATYPE = IDatatype.MAPPINGS_DATATYPE;
    
    String datatype;

    public Cast() {}
    
    public Cast(String name, String lname) {
        super(name);
        setArity(1);
        datatype = lname;       
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype dt = getBasicArg(0).eval(eval, b, env, p);
        
        if (dt == null) {
            return null;
        }
        switch (datatype) {
            case IDatatype.LIST_DATATYPE : return list(dt);
            case IDatatype.GRAPH_DATATYPE: return graph(eval, dt);       
        }
        
        return null;
    }
    
    IDatatype graph(Computer eval, IDatatype dt) {
        if (dt.isExtension()) {
            switch (dt.getDatatypeURI()) {
                case IDatatype.MAPPINGS_DATATYPE:
                    // dt:graph(dt:mappings amap)
                    GraphProcessor proc = eval.getGraphProcessor();
                    return proc.graph(dt);
            }
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
