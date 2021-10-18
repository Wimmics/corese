package fr.inria.corese.sparql.triple.function.proxy;

import fr.inria.corese.kgram.api.core.Expr;
import static fr.inria.corese.kgram.api.core.ExprType.XT_CONTEXT;
import static fr.inria.corese.kgram.api.core.ExprType.XT_FROM;
import static fr.inria.corese.kgram.api.core.ExprType.XT_MAPPINGS;
import static fr.inria.corese.kgram.api.core.ExprType.XT_METADATA;
import static fr.inria.corese.kgram.api.core.ExprType.XT_NAME;
import static fr.inria.corese.kgram.api.core.ExprType.XT_NAMED;
import static fr.inria.corese.kgram.api.core.ExprType.XT_QUERY;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.script.LDScript;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Dataset;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Introspection extends LDScript {  
    
    public Introspection(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null){
            return null;
        }  
        
        switch (oper()) {
                         
            case XT_QUERY:
                return DatatypeMap.createObject(env.getQuery());
                
//            case XT_MAPPINGS:
//                // use case: transformer work on Mappings of a query
//                // st:start template manage Mappings 
//                if (b.getMappings() != null) {
//                    return DatatypeMap.createObject(b.getMappings());
//                }
//                return DatatypeMap.createObject(env.getQuery().getMappings());    
                
            case XT_METADATA:
                ASTQuery ast =  env.getQuery().getAST();
                if (ast.getMetadata() == null){
                    return null;
                }
                return DatatypeMap.createObject(ast.getMetadata());
                
            case XT_CONTEXT:
                return eval.getContext(b, env, p).getDatatypeValue();
                
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
        Node gNode = env.getGraphNode();
        if (gNode == null) {
            return null;
        }  
        if (gNode.isConstant()) {
            return  gNode.getDatatypeValue();
        }
        Node n = env.getNode(gNode);
        if (n == null) {
            return null;
        }
        return  n.getDatatypeValue();
    }
    
    IDatatype dataset(Expr exp, Environment env, Producer p){
        ASTQuery ast = env.getQuery().getAST();
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

