package fr.inria.corese.sparql.api;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Mappings;

/**
 *
 * @author Olivier Corby - INRIA - 2018
 */
public interface GraphProcessor {
    
    IDatatype load(IDatatype dtfile, IDatatype graph, IDatatype expectedFormat, IDatatype requiredFormat);
    
    IDatatype write(IDatatype dtfile, IDatatype dt); 
    IDatatype syntax(IDatatype dtsyntax, IDatatype dtgraph, IDatatype node); 
    
    IDatatype read(IDatatype dt); 
    IDatatype httpget(IDatatype dt); 
    
    IDatatype format(IDatatype[] ldt);
    
    IDatatype format(Mappings map, int format);
    
    IDatatype spin(IDatatype dt);
    
    IDatatype graph(IDatatype dt);    
        
    IDatatype similarity(Environment env, Producer p);
    
    IDatatype similarity(Environment env, Producer p, IDatatype dt1, IDatatype dt2);
    
    IDatatype approximate (Expr exp, Environment env, Producer p);
    IDatatype approximate (Expr exp, Environment env, Producer p, IDatatype[] param);
    
    IDatatype depth(Environment env, Producer p, IDatatype dt);
    
    IDatatype index(Environment env, Producer p);

    IDatatype tune(Expr exp, Environment env, Producer p, IDatatype... dt);
    
    IDatatype value(Environment env,  Producer p, IDatatype graph, IDatatype node, IDatatype predicate, int n);
    IDatatype edge(Environment env,   Producer p, IDatatype subj, IDatatype pred, IDatatype obj);   
    IDatatype edge(Environment env,   Producer p, IDatatype subj, IDatatype pred, IDatatype obj, IDatatype graph);
    IDatatype subjects(Environment env,  Producer p, IDatatype subj, IDatatype pred, IDatatype obj, IDatatype graph);
    IDatatype objects(Environment env,   Producer p, IDatatype subj, IDatatype pred, IDatatype obj, IDatatype graph);
    IDatatype exists(Environment env, Producer p, IDatatype subj, IDatatype pred, IDatatype obj);
    IDatatype insert(Environment env, Producer p, IDatatype[] param);
    IDatatype delete(Environment env, Producer p, IDatatype[] param);
    
    IDatatype degree(Environment env, Producer p, IDatatype node, IDatatype pred, IDatatype index);
    IDatatype mindegree(Environment env, Producer p, IDatatype node, IDatatype pred, IDatatype index, IDatatype min);

    IDatatype entailment(Environment env, Producer p, IDatatype graph);
    
    IDatatype shape(Expr exp, Environment env, Producer p, IDatatype[] param);

    IDatatype sparql(Environment env, Producer p, IDatatype[] param);
    
    IDatatype union(Expr exp, Environment env, Producer p, IDatatype dt1, IDatatype dt2);
    
    IDatatype algebra(Expr exp, Environment env, Producer p, IDatatype dt1, IDatatype dt2);
    
}
