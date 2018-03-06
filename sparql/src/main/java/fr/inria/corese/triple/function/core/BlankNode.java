package fr.inria.corese.triple.function.core;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.corese.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import java.util.Map;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class BlankNode extends TermEval {  
    static final String name = "_:_bnode_";
    long count = 0;
    
    public BlankNode(){}
    
    public BlankNode(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        if (arity() == 1){
            return bnode(eval, b, env, p);
        }
        switch (oper()){
            case ExprType.PATHNODE: return DatatypeMap.createBlank(name + count());
            default: return DatatypeMap.createBlank();                   
        }
    }
    
    synchronized long count(){
        return count++;
    }
    
    IDatatype bnode(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype dt = getBasicArg(0).eval(eval, b, env, p);
        if (dt == null) return null;
        Map map = env.getMap();
        IDatatype bn = (IDatatype) map.get(dt.getLabel());
        if (bn == null) {
            bn = DatatypeMap.createBlank();
            map.put(dt.getLabel(), bn);
        }
        return bn;
    }
   
}
