
package fr.inria.acacia.corese.api;

import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

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
    
    IDatatype method(String name, IDatatype type, IDatatype[] args, Environment env, Producer p);    
    IDatatype function(Expr exp, Environment env, Producer p);
    IDatatype funcall(IDatatype name, IDatatype[] args, Expr exp, Environment env, Producer p);
    IDatatype apply(IDatatype name, IDatatype[] args, Expr exp, Environment env, Producer p);
    IDatatype reduce(IDatatype name, IDatatype[] args, Expr exp, Environment env, Producer p);
    IDatatype map(IDatatype name, IDatatype[] args, Expr exp, Environment env, Producer p);
    IDatatype mapanyevery(IDatatype name, IDatatype[] args, Expr exp, Environment env, Producer p);
    
    IDatatype call(Expr exp, Environment env, Producer p, IDatatype[] values, Expr function);   
    IDatatype aggregate(Expr exp, Environment env, Producer p);
    IDatatype exist(Expr exp, Environment env, Producer p);
        
    Expr getDefine(Expr exp, Environment env);  
    Expr getDefineGenerate(Expr exp, Environment env, String name, int n); 
    
    boolean isCompliant();
}
