package fr.inria.corese.sparql.triple.function.core;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import fr.inria.corese.sparql.triple.parser.Expression;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class ExistFunction extends TermEval {  
    
    public ExistFunction() {}
    
    public ExistFunction(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        if (isSystem()) {
            // LDScript subquery
            check(Feature.SPARQL, b, SPARQL_MESS);
        }
        try {
            return eval.exist(this, env, p);
        } catch (EngineException ex) {
            log(ex.getMessage());
            return null;
        }
    }
    
    @Override
    public boolean isTermExist() {
        return true;
    }
    
     @Override
    public Expression prepare(ASTQuery ast) throws EngineException {
        if (isSystem()) {
            ast.setLDScript(true);
            ast.getGlobalAST().setLDScript(true);
        }
        return super.prepare(ast);
    }
   
}
