/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.acacia.corese.triple.api;

import fr.inria.acacia.corese.triple.parser.*;
import fr.inria.acacia.corese.triple.update.*;



public interface ASTVisitor {

	void visit(ASTQuery ast);

	/* Exp part */

	void visit(Exp exp);

	void visit(Triple triple);

	void visit(Values values);

	void visit(Query query);

	void visit(Or or);

	void visit(Option option);

	void visit(And and);

	void visit(Minus minus);
	
	void visit(BasicGraphPattern bgp);

	void visit(Exist exist);

	void visit(Service service);
	
	void visit(Source source);

	/* Expression part */

	void visit(Expression expression);

	void visit(Term term);

	void visit(Atom atom);

	void visit(Variable variable);

	void visit(Constant constant);
	
	
	/*Update */
	void visit(Update update);
	void visit(Composite composite);
	void visit(Basic basic);

}
