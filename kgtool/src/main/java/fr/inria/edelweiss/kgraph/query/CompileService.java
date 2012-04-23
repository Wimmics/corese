package fr.inria.edelweiss.kgraph.query;

import java.util.ArrayList;
import java.util.Hashtable;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Query;

public class CompileService {
	

	
	Hashtable<String, Double> table;
	
	CompileService(){
		table = new Hashtable<String, Double>();
	}

	void compile(Node serv, Query q, Environment env){
		Query g 	 = q.getOuterQuery();
		ASTQuery ast = (ASTQuery) q.getAST();
		ASTQuery ag  = (ASTQuery) g.getAST();
		ast.setPrefixExp(ag.getPrefixExp());
		
		if (isSparql0(serv)){
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
	
	


}
