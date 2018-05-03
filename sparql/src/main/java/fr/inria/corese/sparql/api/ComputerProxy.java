package fr.inria.corese.sparql.api;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.NSManager;

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
    
    IDatatype transform(IDatatype[] args, IDatatype focus, IDatatype trans, IDatatype temp, IDatatype name,
            Expr exp, Environment env, Producer prod);
    
    IDatatype transform(IDatatype trans, IDatatype temp, IDatatype name, Expr exp, Environment env, Producer prod);
    
    Context getContext(Environment env, Producer p);
    
    NSManager getNSM(Environment env, Producer p);

    IDatatype hash(Expr exp, IDatatype dt);
}
