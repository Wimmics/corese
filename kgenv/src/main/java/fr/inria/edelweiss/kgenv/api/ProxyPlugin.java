package fr.inria.edelweiss.kgenv.api;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.filter.Proxy;

/**
 *
 * @author corby
 */
public interface ProxyPlugin extends Proxy {
    
     @Override
     IDatatype function(Expr exp, Environment env, Producer p);
     
     IDatatype function(Expr exp, Environment env, Producer p, IDatatype dt);
     
     IDatatype function(Expr exp, Environment env, Producer p, IDatatype dt1, IDatatype dt2);
     
     IDatatype eval(Expr exp, Environment env, Producer p, IDatatype[] param);

     @Override
     IDatatype getBufferedValue(StringBuilder sb, Environment env);

}
