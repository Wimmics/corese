package fr.inria.corese.sparql.triple.function.core;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class UnaryFunction extends TermEval {
    
    public UnaryFunction(){}

    public UnaryFunction(String name){
        super(name);
        setArity(1);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype dt = getBasicArg(0).eval(eval, b, env, p);
        if (dt == null){
            return null;
        }
        switch (oper()){
            case ExprType.ISBLANK:      return dt.isBlankNode();
            case ExprType.ISURI:        return dt.isURINode();
            case ExprType.ISLITERAL:    return dt.isLiteralNode();
            case ExprType.ISWELLFORMED: return dt.isWellFormed();
            case ExprType.ISLIST:       return value(dt.isList());
            case ExprType.ISNUMERIC:    return value(dt.isNumber());
            case ExprType.ISUNDEFINED:  return value(dt.isUndefined());
            case ExprType.ISSKOLEM:     return value(dt.isSkolem());
            case ExprType.ISEXTENSION:  return value(dt.isExtension());
                
            case ExprType.CEILING:      return ceil(dt);
            case ExprType.ABS:          return abs(dt);
            case ExprType.ROUND:        return round(dt);
            case ExprType.FLOOR:        return floor(dt);
                
            case ExprType.DATATYPE:     return dt.getDatatype();
            case ExprType.LANG:         return dt.getDataLang();
            case ExprType.STR:          return DatatypeMap.newLiteral(dt.getPrettyLabel());
            case ExprType.XSDSTRING:    return DatatypeMap.newInstance(dt.getPrettyLabel());
            case ExprType.CAST:         return dt.cast(getLabel());
            case ExprType.STRLEN:       return DatatypeMap.strlen(dt);
            case ExprType.UCASE:        return result(dt, dt.getLabel().toUpperCase());
            case ExprType.LCASE:        return result(dt, dt.getLabel().toLowerCase());
            case ExprType.ENCODE:       return DatatypeMap.encode_for_uri(dt);
            case ExprType.URI:          return uri(dt);
        }
        return null;
    }
    
    
    IDatatype uri(IDatatype dt) {
        if (dt.isURI()) {
            return dt;
        }
        String label = dt.getLabel();
        if (getModality() != null && !isURI(label)) {
            // with base
            return DatatypeMap.newResource(getModality() + label);
        } else {
            return DatatypeMap.newResource(label);
        }
    }
    
     boolean isURI(String str) {
        return str.matches("[a-zA-Z0-9]+://.*");
    }
    
    IDatatype abs(IDatatype dt) {
        switch (dt.getCode()){
            case IDatatype.INTEGER: return DatatypeMap.newInteger(Math.abs(dt.longValue()));
            default:                return DatatypeMap.newInstance(Math.abs(dt.doubleValue()));
        }
    }
     
    IDatatype floor(IDatatype dt) {
        return DatatypeMap.newInstance(Math.floor(dt.doubleValue()), dt.getDatatypeURI());
    }

    IDatatype round(IDatatype dt) {
        return DatatypeMap.newInstance(Math.round(dt.doubleValue()), dt.getDatatypeURI());
    }

    IDatatype ceil(IDatatype dt) {
        return DatatypeMap.newInstance(Math.ceil(dt.doubleValue()), dt.getDatatypeURI());
    } 
    
}
