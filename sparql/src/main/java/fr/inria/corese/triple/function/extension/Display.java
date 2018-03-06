package fr.inria.corese.triple.function.extension;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.corese.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
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
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
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
