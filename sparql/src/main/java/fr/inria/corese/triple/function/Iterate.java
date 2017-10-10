package fr.inria.corese.triple.function;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.corese.triple.term.Binding;
import fr.inria.corese.triple.term.TermEval;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Iterate extends TermEval {  
    
    public Iterate(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype[] args = eval(eval, b, env, p, 0);
        
        int start = 0;
        int end = 1;
        
        if (args.length > 1){
            start = args[0].intValue();
            end =   args[1].intValue();
        }
        else {
            end =   args[0].intValue();
        }
        
        int step = 1;
        
        if (end < start){
            step = -1;
        }
        
        if (args.length == 3){
            step = args[2].intValue();
        }
                      
        IDatatype dt = DatatypeMap.newIterate(start, end, step);
        return dt;
    }
        
    
    
   
}
