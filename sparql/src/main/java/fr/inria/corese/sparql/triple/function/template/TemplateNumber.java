package fr.inria.corese.sparql.triple.function.template;

import static fr.inria.corese.kgram.api.core.ExprType.STL_INDEX;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.datatype.DatatypeMap;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class TemplateNumber extends TemplateFunction {  
    int index = 0;
        
    public TemplateNumber(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        switch (oper()) {
            case STL_INDEX:
                return DatatypeMap.newInstance(index++);
                
            default: return DatatypeMap.newInstance(1 + env.count());
        }
    }
   
}

