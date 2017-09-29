package fr.inria.acacia.corese.triple.api;

import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.Function;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.acacia.corese.triple.parser.Variable;

/**
 *
 * @author corby
 */
public interface ExpressionVisitor {
    
    void start(Expression exp);
    
    void visit(Exp exp);
    
    void visit(Term t);
    
    void visit(Function f);

    void visit(Variable v);
    
    void visit(Constant c);
        
}
