
package fr.inria.acacia.corese.api;

import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author corby
 */
public interface Computer {
    
    IDatatype eval(Expr exp, Environment env, Producer p);
    
    IDatatype funcall(IDatatype name, IDatatype[] args, Expr exp, Environment env, Producer p);
    
    IDatatype in(Expr exp, Environment env, Producer p);

}
