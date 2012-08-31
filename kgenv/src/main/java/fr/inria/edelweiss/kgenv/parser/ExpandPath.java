package fr.inria.edelweiss.kgenv.parser;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Atom;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.Or;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgenv.api.QueryVisitor;
import fr.inria.edelweiss.kgram.api.core.Regex;
import fr.inria.edelweiss.kgram.core.Query;

/**
 * Rewrite Property Path into BGP
 * Expand path and loop (+ and *) with a chain of length at most n
 * Implemented as a Visitor that is applied at compilation time, just after parsing.
 * Visitor rewrites the AST, Paths are transformed into BGP
 * 
 * Usage:
 * 
 * (1) pragma {kg:path kg:expand n}
 * 
 * (2) exec.setVisitor(ExpandPath.create(n));
 * 
 * @author Olivier Corby, Wimmics, INRIA 2012
 *
 */
public class ExpandPath implements QueryVisitor {
	
	private static Logger log = Logger.getLogger(ExpandPath.class);
	
	static final String ROOT = "?_VAR_";
	int max = 5;
	int varCount = 0;
	boolean isDebug = false;
	ASTQuery ast;
	
	ExpandPath(int n){
		max = n;
	}
	
	ExpandPath(){

	}
	
	public static ExpandPath create(){
		return new ExpandPath();
	}
	
	public static ExpandPath create(int n){
		return new ExpandPath(n);
	}
	
	public void visit(ASTQuery ast) {
		rewrite(ast);
	}

	public void visit(Query query) {
		
	}

	
	
	/**
	 * rewrite path to sparql 1.0 BGP
	 */
	public void rewrite(ASTQuery ast){
		this.ast = ast;
		isDebug = ast.isDebug();
		Exp body = ast.getBody();
		rewrite(body);
	}
	

	/**
	 * Rewrite path as BGP
	 * Modify exp (no copy is done)
	 */
	public Exp rewrite(Exp exp){
		if (ast == null){
			ast = ASTQuery.create();
		}
		if (exp.isTriple()){
			Triple t = exp.getTriple();
			if (t.isPath()){
				return rewrite(t, t.getRegex());
			}
		}
		else for (int i=0; i<exp.size(); i++){
			exp.set(i, rewrite(exp.get(i)));
		}
		return exp;
	}
			
	
	/*************************************************************/
	
	
	
	
	private Exp rewrite(Triple path, Expression exp){
		Exp res = null;
		
		switch (exp.getretype()){
		
		case Regex.UNDEF:
			if (isDebug) log.debug("** Expand: not rewrite UNDEF Path: " + exp);
			
		case Regex.LABEL:
			res = triple(path, exp);
			break;	
			
		case Regex.STAR:
			if (isDebug) log.debug("** Expand: rewrite exp* " + exp + " as exp+");

		case Regex.PLUS:
			res = loop(path, exp);
			break;
			
		case Regex.SEQ:
			res = sequence(path, exp);
			break;	

		case Regex.ALT:
			res = alt(path, exp);
			break;
			
		case Regex.NOT:
			res = not(path, exp);
			break;	
			
		case Regex.OPTION:
			res = option(path, exp);
			break;	

		}
		
		res = rewrite(res);
		return res;
	}
	
	
	/**
	 * Create a fresh new variable
	 */
	private Variable variable(){
		Variable var = Variable.create(ROOT + varCount++);
		//var.setBlankNode(true);
		return var;
	}
	
	/**
	 * exp is a property label, generate a simple triple
	 */
	private Exp triple(Triple t, Expression exp){
		Triple tt = Triple.create(t.getSubject(), exp.getConstant(), t.getObject());
		return tt;
	}

	
	/**
	 * e1 | e2
	 */
	private Exp alt(Triple t, Expression exp){
		Exp e1 = ast.createPath(t.getArg(0), exp.getArg(0), t.getArg(1));
		Exp e2 = ast.createPath(t.getArg(0), exp.getArg(1), t.getArg(1));
		
		BasicGraphPattern bgp1 = BasicGraphPattern.create(e1);
		BasicGraphPattern bgp2 = BasicGraphPattern.create(e2);
		
		Or or = Or.create(bgp1, bgp2);
		return or;
	}
	
	/**
	 * e1 / e2
	 * Special cases: 
	 * (1) ?x rdf:type/rdfs:subClassOf* ?c
	 * ->
	 * {?x rdf:type ?c} union { ?x rdf:type/rdfs:subClassOf+ ?c} 
	 * (2) rdf:rest* / rdf:first -> rdf:first union rdf:rest+ / rdf:first
	 */
	private Exp sequence(Triple t, Expression exp){
		Variable var = variable();
		Exp e1 = ast.createPath(t.getArg(0), exp.getArg(0), var);
		Exp e2 = ast.createPath(var, exp.getArg(1), t.getArg(1));
		Exp res = BasicGraphPattern.create(e1, e2);
		
		if (exp.getArg(0).isStar() || exp.getArg(0).isOpt()){
			// use case: rdf:rest*/rdf:first
			// add ?x rdf:first ?y
			Exp e = ast.createPath(t.getArg(0), exp.getArg(1), t.getArg(1));
			BasicGraphPattern b = BasicGraphPattern.create(e);
			res = Or.create(b, res);
		}
		else if (exp.getArg(1).isStar() || exp.getArg(1).isOpt()){
			// use case: rdf:type/rdfs:subClassOf* 
			// add ?x rdf:type ?c
			Exp e = ast.createPath(t.getArg(0), exp.getArg(0), t.getArg(1));
			BasicGraphPattern b = BasicGraphPattern.create(e);
			res = Or.create(b, res);
		}
		
		return res;
	}
	

	
	/**
	 * Expression loop is exp+ or exp*
	 * exp* rewritten as exp+ except in the case e1/e2* where it is correctly rewritten
	 */
	private Exp loop(Triple t, Expression loop){
		return loop(t.getSubject(), loop.getArg(0), t.getObject(), max);
	}
	
	
	/**
	 * rec create a path bgp of length n between s and o
	 *  s exp(n) o =
	 *  {s exp o} union {s exp vi . vi exp(n-1) o} 
	 */
	private Exp loop(Atom subject, Expression exp, Atom object, int n){

		Exp res = ast.createPath(subject, exp, object);
		
		if (n <= 1){
			return res;
		}

		Variable var = variable();
		Exp e1 = ast.createPath(subject, exp, var);
		Exp e2 = loop(var, exp, object, n-1);
		BasicGraphPattern bgp = BasicGraphPattern.create(e1, e2);

		Or or = Or.create(res, bgp);
		return or;
	}
	
	
	/**
	 * exp? -> exp
	 * See also: special treatment in sequence
	 */
	private Exp option(Triple t, Expression option){
		Exp res = ast.createPath(t.getSubject(), option.getArg(0), t.getObject());
		return res;
	}
	
	

	/**
	 * ! ex:p1
	 * ! (ex:p1 | ex:p2)
	 * ->
	 * ?x ?p ?y . filter(?p != ex:p1)
	 */
	private Exp not(Triple t, Expression not){
		Expression exp = not.getArg(0);
		Variable p = variable();
		Triple tt = Triple.create(t.getSubject(), p, t.getObject());
		BasicGraphPattern bgp = BasicGraphPattern.create(tt);
		Expression f = null;

		if (exp.isConstant()){
			// ! p
			f = Term.create(Term.SNEQ, p, exp); 
		}
		else {
			// ! (p1 | p2)
			
			for (Expression ee : exp.getArgs()){
				Term g = Term.create(Term.SNEQ, p, ee); 
				f = and(f, g);
			}
		}
		
		bgp.add(Triple.create(f));
		
		return bgp;
	}
	
	
	
	private Expression and(Expression t1, Expression t2){
		if (t1 == null) return t2;
		return Term.create(Term.SEAND, t1, t2);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	


}
