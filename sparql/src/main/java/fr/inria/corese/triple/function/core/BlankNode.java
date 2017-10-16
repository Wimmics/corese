package fr.inria.corese.triple.function.core;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.corese.triple.function.term.TermEval;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class BlankNode extends TermEval {  
    static final String name = "_:_bnode_";
    long count = 0;
    
    public BlankNode(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        switch (oper()){
            case ExprType.PATHNODE: return DatatypeMap.createBlank(name + count());
            default: return DatatypeMap.createBlank();                   
        }
    }
    
    synchronized long count(){
        return count++;
    }
   
}
