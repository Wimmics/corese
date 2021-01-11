/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.sparql.triple.function.extension;

import static fr.inria.corese.kgram.api.core.PointerType.EXPRESSION;
import fr.inria.corese.kgram.api.core.Pointerable;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import java.util.ArrayList;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class Rest extends TermEval {
      
    public Rest(){}
    
    public Rest(String name){
        super(name);
        setArity(2);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype dt    = getBasicArg(0).eval(eval, b, env, p);
        IDatatype index = getBasicArg(1).eval(eval, b, env, p);
        if (dt == null || index == null) {
            return null;
        }
        IDatatype last   = null;
        if (arity()>2) {
           last   = getBasicArg(2).eval(eval, b, env, p); 
        }
        return rest(dt, index, last);
        
    }
    
    public static IDatatype rest(IDatatype dt, IDatatype index, IDatatype last){        
      
        if (dt.isMap() || dt.isJSON() || dt.isXML() || dt.pointerType() == EXPRESSION) {
            dt = dt.toList();
        }
        if (dt.isList()) {
            if (last == null) {
                return DatatypeMap.rest(dt, index);
            }
            else {
                return DatatypeMap.rest(dt, index, last);
            }           
        }
        else if (dt.isPointer() && dt.isLoop()) {
            if (last == null) {
                return rest(dt, index);
            }
            else {
                return DatatypeMap.rest(dt.toList(), index, last);
            }
        }
        return null;
    }
    
    static IDatatype rest(IDatatype dt, IDatatype index) {       
        Pointerable res = dt.getPointerObject(); 
        ArrayList<IDatatype> list = new ArrayList<>();
        int i = 0;
        for (Object obj : res.getLoop()) {          
            if (i++ >= index.intValue() && obj != null) {
                list.add(DatatypeMap.getValue(obj));
            }
        }
        return DatatypeMap.newInstance(list);
    }

}
