package fr.inria.corese.sparql.triple.function.extension;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
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
public class Display extends TermEval {

    public Display() {}
    
    public Display(String name) {
        super(name);
    }
        
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null) {
            return null;
        }
        for (IDatatype dt : param) {
            IDatatype res = dt.display();
            switch (oper()) {
                case ExprType.XT_PRETTY: return res; 
                case ExprType.XT_PRINT: System.out.print(res.stringValue()); 
                break;
                case ExprType.XT_DISPLAY:    
                default:                System.out.print(res);              
            }
            System.out.print(" ");
        }       
        System.out.println();
        return TRUE;
    }

  
}
