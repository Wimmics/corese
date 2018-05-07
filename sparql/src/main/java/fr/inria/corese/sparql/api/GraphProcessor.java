package fr.inria.corese.sparql.api;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby - INRIA - 2018
 */
public interface GraphProcessor {
    
    IDatatype load(IDatatype dtfile, IDatatype format);
    
    IDatatype write(IDatatype dtfile, IDatatype dt); 
        
    IDatatype similarity(Environment env, Producer p);
    
    IDatatype similarity(Environment env, Producer p, IDatatype dt1, IDatatype dt2);
    
    IDatatype depth(Environment env, Producer p, IDatatype dt);

    IDatatype tune(Expr exp, Environment env, Producer p, IDatatype dt1, IDatatype dt2);
    
    IDatatype edge(Expr exp, Environment env, Producer p, IDatatype subj, IDatatype pred, IDatatype obj);
   
}
