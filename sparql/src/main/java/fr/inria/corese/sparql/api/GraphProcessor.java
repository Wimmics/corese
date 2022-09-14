package fr.inria.corese.sparql.api;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Access.Level;

/**
 * interface of fr.inria.corese.core.query.PluginImpl
 * see fr.inria.corese.sparql.triple.function.proxy.GraphSpecificFunction
 * 
 * @author Olivier Corby - INRIA - 2018
 */
public interface GraphProcessor {
    
    IDatatype load(Producer p, IDatatype dtfile, IDatatype graph, IDatatype expectedFormat, IDatatype requiredFormat, Level level)
            throws EngineException;
    
    IDatatype write(IDatatype dtfile, IDatatype dt); 
    IDatatype superWrite(IDatatype dtfile, IDatatype dt); 
    IDatatype syntax(IDatatype dtsyntax, IDatatype dtgraph, IDatatype node); 
    
    IDatatype read(IDatatype dt); 
    IDatatype readSPARQLResult(IDatatype dtpath, IDatatype... dtformat);
    IDatatype readSPARQLResultString(IDatatype dtstring, IDatatype... dtformat);
    IDatatype httpget(IDatatype dt); 
    IDatatype httpget(IDatatype dt, IDatatype format); 
    
    IDatatype format(IDatatype[] ldt);
    
    IDatatype format(Mappings map, int format);
    
    IDatatype spin(IDatatype dt);
    
    IDatatype graph(IDatatype dt);  
    // xt:create(dt:graph) -> GraphStore.create()
    IDatatype create(IDatatype dt);    
        
    IDatatype similarity(Environment env, Producer p);
    
    IDatatype similarity(Environment env, Producer p, IDatatype dt1, IDatatype dt2);
    
    IDatatype approximate (Expr exp, Environment env, Producer p);
    IDatatype approximate (Expr exp, Environment env, Producer p, IDatatype[] param);
    
    IDatatype depth(Environment env, Producer p, IDatatype dt);
    
    IDatatype index(Environment env, Producer p);

    IDatatype tune(Expr exp, Environment env, Producer p, IDatatype... dt);
    
    IDatatype triple(Environment env,  Producer p, IDatatype subj, IDatatype pred, IDatatype obj);
    IDatatype value(Environment env,  Producer p, IDatatype graph, IDatatype node, IDatatype predicate, int n);
    IDatatype insert(Environment env, Producer p, IDatatype[] param);
    IDatatype delete(Environment env, Producer p, IDatatype[] param);
    
    IDatatype entailment(Environment env, Producer p, IDatatype graph) throws EngineException;
    
    IDatatype shape(Expr exp, Environment env, Producer p, IDatatype[] param);

    IDatatype sparql(Environment env, Producer p, IDatatype[] param) throws EngineException;
    
    IDatatype union(Expr exp, Environment env, Producer p, IDatatype dt1, IDatatype dt2);
    IDatatype merge(Expr exp, Environment env, Producer p, IDatatype dt1, IDatatype dt2);
    
    IDatatype algebra(Expr exp, Environment env, Producer p, IDatatype dt1, IDatatype dt2);
    
}
