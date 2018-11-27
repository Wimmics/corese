package fr.inria.corese.sparql.triple.function.extension;

import static fr.inria.corese.kgram.api.core.ExprType.XT_JSON;
import static fr.inria.corese.kgram.api.core.ExprType.XT_XML;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.api.ResultFormatDef;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;

public class ResultFormater extends TermEval {

    public ResultFormater() {}
    
    public ResultFormater(String name) {
        super(name);
        setArity(1);
    }
        
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype dt = getArg(0).eval(eval, b, env, p);
        if (dt == null || dt.pointerType() != MAPPINGS_POINTER || dt.getPointerObject() == null) {
            return null;
        }
        Mappings map = dt.getPointerObject().getMappings();
        switch (oper()){
            case XT_XML: return eval.getGraphProcessor().format(map, ResultFormatDef.XML_FORMAT);
            case XT_JSON:return eval.getGraphProcessor().format(map, ResultFormatDef.JSON_FORMAT);               
            default:   
                return dt;
        }
    }
    
}
