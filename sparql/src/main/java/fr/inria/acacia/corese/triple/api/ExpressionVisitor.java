/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.acacia.corese.triple.api;

import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.acacia.corese.triple.parser.Variable;

/**
 *
 * @author corby
 */
public interface ExpressionVisitor {
    
    void visit(Term t);
    
    void visit(Variable v);
    
    void visit(Constant c);
    
}
