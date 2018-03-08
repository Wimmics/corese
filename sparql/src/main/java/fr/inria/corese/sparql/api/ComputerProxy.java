package fr.inria.corese.sparql.api;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author corby
 */
public interface ComputerProxy {
    
    ComputerProxy getComputerPlugin();
    ComputerProxy getComputerTransform();
    
    IDatatype eval(Expr exp, Environment env, Producer p, IDatatype[] param);
    IDatatype function(Expr exp, Environment env, Producer p);
    IDatatype function(Expr exp, Environment env, Producer p, IDatatype dt);
    IDatatype function(Expr exp, Environment env, Producer p, IDatatype dt1, IDatatype dt2);
    IDatatype hash(Expr exp, IDatatype dt);
}
