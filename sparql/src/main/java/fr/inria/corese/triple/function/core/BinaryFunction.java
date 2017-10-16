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
public class BinaryFunction extends TermEval {
       
    public BinaryFunction(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype dt1 = getArg(0).eval(eval, b, env, p);
        IDatatype dt2 = getArg(1).eval(eval, b, env, p);
        if (dt1 == null || dt2 == null) return null;
        
        switch (oper()){
            case ExprType.LANGMATCH: 
                return langMatches(dt1, dt2);
                
            case ExprType.STRLANG:
                if (eval.isCompliant() && !isSimpleLiteral(dt1)) return null;                
                return DatatypeMap.newInstance(dt1.getLabel(), null, dt2.getLabel());
                
            case ExprType.STRDT:    
                if (eval.isCompliant() && !isSimpleLiteral(dt1)) return null;                
                return DatatypeMap.newInstance(dt1.getLabel(), dt2.getLabel());
                
            case ExprType.SAMETERM:  return value(dt1.sameTerm(dt2));        
        }
        
        return null;
    }
    
    
    IDatatype langMatches(IDatatype ln1, IDatatype ln2) {
        String l1 = ln1.getLabel();
        String l2 = ln2.getLabel();

        if (l2.equals("*")) {
            return value(l1.length() > 0);
        }
        if (l2.indexOf("-") != -1) {
            // en-us need exact match
            return value(l1.toLowerCase().equals(l2.toLowerCase()));
        }
        return value(l1.regionMatches(true, 0, l2, 0, 2));
    }
 
}
