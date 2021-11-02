package fr.inria.corese.sparql.triple.function.template;

import fr.inria.corese.kgram.api.core.ExprType;
import static fr.inria.corese.kgram.api.core.ExprType.XT_MAPPINGS;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.TransformProcessor;
import fr.inria.corese.sparql.datatype.DatatypeMap;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class TemplateAccess extends TemplateFunction {  
    static final IDatatype EMPTY = DatatypeMap.newStringBuilder("");
       
    public TemplateAccess(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null){
            return null;
        }
        
        switch (oper()) {
            case XT_MAPPINGS:
                return getMappings(eval, b, env, p, param);               
        }
        
        TransformProcessor trans = eval.getTransformer(b, env, p);
        
        switch (oper()) {
            case ExprType.STL_NL:
                switch (param.length) {
                    case 0:
                        return trans.tabulate();
                    case 1:
                        trans.setLevel(trans.getLevel() + param[0].intValue());
                        return trans.tabulate();
                }

            case ExprType.INDENT:
                switch (param.length) {
                    case 1: trans.setLevel(trans.getLevel() + param[0].intValue());
                    return EMPTY;
                }
                
            case ExprType.STL_ISSTART:
                return (trans.isStart()) ? TRUE : FALSE;
                
            case ExprType.STL_DEFINED:
                return trans.isDefined(param[0].getLabel()) ? TRUE : FALSE;
                                                                               
            default:
                return null;
        }
    }
      
    
    IDatatype getMappings(Computer eval, Binding b, Environment env, Producer p, IDatatype[] param)
            throws EngineException {
        
        if (param.length == 0) {
            Mappings map = null;
            
            if (b.getMappings() != null) {
                map = b.getMappings();
            } else {
                map = eval.getTransformer(b, env, p).getMappings();
            }
            if (map == null) {
                return null;
            }
            return DatatypeMap.createObject(map);
        } else {
            // xt:mappings(path) with path of Query Results Mappings to parse
            IDatatype dt = eval.getGraphProcessor().readSPARQLResult(param[0]);
            return dt;
        }
    }
    
   
}

