package fr.inria.corese.sparql.triple.function.core;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
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
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        if (isSystem()) {
            // LDScript subquery
            if (reject(Feature.SPARQL, eval, env, p)) {
                TermEval.logger.error("SPARQL query unauthorized");
                return null;
            }
        }
        return eval.exist(this, env, p);
    }
    
    @Override
    public boolean isTermExist() {
        return true;
    }
    
     @Override
    public Expression prepare(ASTQuery ast) {
        if (isSystem()) {
            ast.setLDScript(true);
            ast.getGlobalAST().setLDScript(true);
        }
        return super.prepare(ast);
    }
   
}
