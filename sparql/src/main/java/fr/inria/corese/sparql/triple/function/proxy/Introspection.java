package fr.inria.corese.sparql.triple.function.proxy;

import fr.inria.corese.kgram.api.core.Expr;
import static fr.inria.corese.kgram.api.core.ExprType.XT_CONTEXT;
import static fr.inria.corese.kgram.api.core.ExprType.XT_FROM;
import static fr.inria.corese.kgram.api.core.ExprType.XT_METADATA;
import static fr.inria.corese.kgram.api.core.ExprType.XT_NAME;
import static fr.inria.corese.kgram.api.core.ExprType.XT_NAMED;
import static fr.inria.corese.kgram.api.core.ExprType.XT_QUERY;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Dataset;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Introspection extends TermEval {  
    
    public Introspection(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null){
            return null;
        }  
        
        switch (oper()) {
                         
            case XT_QUERY:
                return DatatypeMap.createObject(env.getQuery());
                
            case XT_METADATA:
                ASTQuery ast = (ASTQuery) env.getQuery().getAST();
                if (ast.getMetadata() == null){
                    return null;
                }
                return DatatypeMap.createObject(ast.getMetadata());
                
            case XT_CONTEXT:
                return DatatypeMap.createObject(eval.getContext(env, p));
                
            case XT_FROM:
            case XT_NAMED:
                return dataset(this, env, p);
                
            case XT_NAME:
                return name(env);
                
            default: return null;
        }
    }
    
    // name of  current named graph 
    IDatatype name(Environment env) {
        if (env.getGraphNode() == null) {
            return null;
        }      
        Node n = env.getNode(env.getGraphNode());
        if (n == null) {
            return null;
        }
        return (IDatatype) n.getDatatypeValue();
    }
    
    IDatatype dataset(Expr exp, Environment env, Producer p){
        ASTQuery ast = (ASTQuery) env.getQuery().getAST();
        Dataset ds = ast.getDataset();
        
        switch (exp.oper()){
            case XT_FROM:
                return ds.getFromList();
            case XT_NAMED:
                return ds.getNamedList();
        }
        return null;
    }
         
}

