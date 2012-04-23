package fr.inria.edelweiss.kgenv.parser;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Atom;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.Or;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgenv.api.QueryVisitor;
import fr.inria.edelweiss.kgram.core.Query;

/**
 * Expand exp+ ?y as exp [exp ?y]
 * 
 * @author Olivier Corby, Wimmics, INRIA 2012
 *
 */
public class ExpandPath implements QueryVisitor {
	
	static final String ROOT = "?_VAR_";
	int max = 20;
	
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
		cast(ast);
	}

	public void visit(Query query) {
		
	}

	
	
	/**
	 * cast path to sparql 1.0
	 */
	void cast(ASTQuery ast){
		Exp body = ast.getBody();
		cast(body);
	}
	
	/**
	 * Transform exp to SPARQL 1.0
	 * foaf:knows+ -> union foaf:knows[foaf:knows ?y]
	 */
	Exp cast(Exp exp){
		if (exp.isTriple()){
			Triple t = exp.getTriple();
			if (t.isPath()){
				return cast(t);
			}
		}
		else for (int i=0; i<exp.size(); i++){
			exp.set(i, cast(exp.get(i)));
		}
		return exp;
	}

	
	Exp cast(Triple t){
		Expression regex = t.getRegex();
		
		if (regex.isPlus() && regex.getArg(0).isConstant()){
			Constant cst = regex.getArg(0).getConstant();
			Exp exp = loop(t, cst);
			return exp;
		}
		
		return t;
	}
	
	Exp loop(Triple t, Constant p){
		Exp res = Triple.create(t.getArg(0), p, t.getArg(1));
		
		for (int i = 1; i<max; i++){
			Exp exp = loop(t, p, i);
			res = Or.create(res, exp);
		}
		
		return res;
		
	}

	Exp loop(Triple t, Constant p, int n){
		Atom at = t.getArg(0);
		BasicGraphPattern bgp = BasicGraphPattern.create();
		
		for (int i = 0; i<n; i++){
			Variable var = Variable.create(ROOT + i);
			var.setBlankNode(true);
			Triple nt = Triple.create(at, p, var);
			bgp.add(nt);
			at = var;
		}
		
		Triple nt = Triple.create(at, p, t.getArg(1));
		bgp.add(nt);

		return bgp;
	}



}
