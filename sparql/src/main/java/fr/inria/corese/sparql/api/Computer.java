
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
public interface Computer {
    
    ComputerProxy getComputerProxy();
    ComputerProxy getComputerPlugin();
    ComputerProxy getComputerTransform();
    Computer getComputer(Environment env, Producer p, Expr function); 
    Environment getEnvironment();  
    
    IDatatype function(Expr exp, Environment env, Producer p);
    IDatatype exist(Expr exp, Environment env, Producer p);
        
    Expr getDefine(Expr exp, Environment env);  
    Expr getDefineGenerate(Expr exp, Environment env, String name, int n); 
    Expr getDefineMethod(Environment env, String name, IDatatype type, IDatatype[] param);   
    boolean isCompliant();
    
    GraphProcessor getGraphProcessor();
    
    TransformProcessor getTransformer(Environment env, Producer p);
    
    TransformProcessor getTransformer(Environment env, Producer p, Expr exp, IDatatype uri, IDatatype gname);
    
    TransformVisitor getVisitor(Environment env, Producer p);
    
    Context getContext(Environment env, Producer p);
    
    NSManager getNSM(Environment env, Producer p);
    
}
