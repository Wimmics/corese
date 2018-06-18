
package fr.inria.corese.sparql.api;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Eval;

/**
 *
 * @author corby
 */
public interface Computer extends ComputerProxy {
    
    Computer getComputer(Environment env, Producer p, Expr function); 
    Environment getEnvironment();  
    Eval getEval();     
    
    IDatatype function(Expr exp, Environment env, Producer p);
    IDatatype exist(Expr exp, Environment env, Producer p);
        
    Expr getDefine(Expr exp, Environment env);  
    Expr getDefineGenerate(Expr exp, Environment env, String name, int n); 
    Expr getDefineMethod(Environment env, String name, IDatatype type, IDatatype[] param);   
    boolean isCompliant();
}
