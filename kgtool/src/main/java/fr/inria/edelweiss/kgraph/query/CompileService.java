package fr.inria.edelweiss.kgraph.query;

import java.util.ArrayList;
import java.util.Hashtable;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Atom;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.Or;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Query;

public class CompileService {
	
	static final String ROOT = "?_VAR_";
	static final int MAX = 20;
	
	Hashtable<String, Double> table;
	
	CompileService(){
		table = new Hashtable<String, Double>();
	}

	void compile(Node serv, Query q, Environment env){
		ASTQuery ast = (ASTQuery) q.getAST();
		Query g = q.getOuterQuery();
		ASTQuery ag  = (ASTQuery) g.getAST();
		ast.setPrefixExp(ag.getPrefixExp());
		
		if (isSparql0(serv)){
			cast(ast);
			filter(q, env);
		}
		else {
			bindings(q, env);
		}		
	}
	
	void set(String uri, double version){
		table.put(uri, version);
	}
	
	
	// everybody is 1.0 except localhost
	boolean isSparql0(Node serv){
		Double f = table.get(serv.getLabel());
		return (f == null || f == 1.0);
	}
	
	/**
	 * Search select variable of query that is bound in env
	 * Generate binding for such variable
	 * Set bindings in ASTQuery
	 */
	void bindings(Query q, Environment env){
		ASTQuery ast = (ASTQuery) q.getAST();
		ast.clearBindings();
		ArrayList<Variable> lvar = new ArrayList<Variable>();
		ArrayList<Constant> lval = new ArrayList<Constant>();

		for (Node qv : q.getSelect()){
			String var = qv.getLabel();
			Node val   = env.getNode(var);
			
			if (val != null){
				lvar.add(Variable.create(var));
				IDatatype dt = (IDatatype) val.getValue();
				Constant cst = Constant.create(dt);
				lval.add(cst);
			}
		}
		
		if (lvar.size()>0){
			ast.setVariableBindings(lvar);
			ast.setValueBindings(lval);
		}
	}
	
	
	void filter(Query q, Environment env){
		ASTQuery ast = (ASTQuery) q.getAST();
		ArrayList<Term> lt = new ArrayList<Term>();

		for (Node qv : q.getSelect()){
			String var = qv.getLabel();
			Node val   = env.getNode(var);
			
			if (val != null){
				Variable v = Variable.create(var);
				IDatatype dt = (IDatatype) val.getValue();
				Constant cst = Constant.create(dt);
				Term t = Term.create(Term.SEQ, v, cst);
				lt.add(t);
			}
		}
				
		if (lt.size()>0){
			Term f = lt.get(0);
			for (int i = 1; i<lt.size(); i++){
				f = Term.create(Term.SEAND, f, lt.get(i));
			}
			
			if (ast.getSaveBody() == null){
				ast.setSaveBody(ast.getBody());
			}
			BasicGraphPattern body = BasicGraphPattern.create();
			body.add(ast.getSaveBody());
			body.add(Triple.create(f));
			ast.setBody(body);
		}
		
	}
	
	
	/**
	 * cast path to sparql 1.0
	 */
	void cast(ASTQuery ast){
		Exp body = ast.getBody();
		for (int i=0; i<body.size(); i++){
			body.set(i, cast(body.get(i)));
		}
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
		
		for (int i = 1; i<MAX; i++){
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
