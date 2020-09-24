package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Expression;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 *
 */
public class LDScript extends TermEval {
    
    public LDScript() {}
    
    public LDScript(String name) {
        super(name);
    }
    
    LDScript(String name, Expression e1, Expression e2) {
        super(name, e1, e2);
    }
    
    @Override
    public boolean isLDScript() {
        return true;
    }
    
    @Override
    public Expression prepare(ASTQuery ast) {
        ast.setLDScript(true);
        ast.getGlobalAST().setLDScript(true);
        return super.prepare(ast);
    }
    
}
