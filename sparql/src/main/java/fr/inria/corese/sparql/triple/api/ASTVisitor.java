/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.sparql.triple.api;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.And;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exist;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Minus;
import fr.inria.corese.sparql.triple.parser.Optional;
import fr.inria.corese.sparql.triple.parser.Union;
import fr.inria.corese.sparql.triple.parser.Query;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Source;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Values;
import fr.inria.corese.sparql.triple.parser.Variable;
import fr.inria.corese.sparql.triple.update.Basic;
import fr.inria.corese.sparql.triple.update.Composite;
import fr.inria.corese.sparql.triple.update.Update;



public interface ASTVisitor {

	void visit(ASTQuery ast);

	/* Exp part */

	void visit(Exp exp);

	void visit(Triple triple);

	void visit(Values values);

	void visit(Query query);

	void visit(Union or);

	void visit(Optional option);

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
