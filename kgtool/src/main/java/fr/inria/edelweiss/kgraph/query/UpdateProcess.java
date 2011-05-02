package fr.inria.edelweiss.kgraph.query;


import java.util.List;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Source;
import fr.inria.acacia.corese.triple.update.ASTUpdate;
import fr.inria.acacia.corese.triple.update.Basic;
import fr.inria.acacia.corese.triple.update.Composite;
import fr.inria.acacia.corese.triple.update.Update;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.logic.Entailment;


/**
 * SPARQL 1.1 Update
 * 
 * This is called by QueryProcess.query() to process update query
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class UpdateProcess {

	QueryProcess exec;
	Manager manager;
	Query query;
	List<String> from, named;
	
	boolean isDebug = false; 
	
	UpdateProcess(QueryProcess e){
		exec = e;
		isDebug = e.isDebug();
		manager = Manager.create(exec.getGraph(), exec.getLoader());
	}
	
	static UpdateProcess create(QueryProcess e){
		UpdateProcess u = new UpdateProcess(e);
		return u;
	}
		
	
	/**
	 * Process an update sparql query
	 * There may be a list of queries
	 */
	public Mappings update(Query q){
		return update(q, null, null);
	}
	
	public Mappings update(Query q, List<String> from, List<String> named){
		query = q;
		ASTQuery ast = (ASTQuery) q.getAST();
		ASTUpdate astu = ast.getUpdate();
		if (from != null){
			// add the default graphs where insert or entailment may have been done previously
			for (String src : Entailment.GRAPHS){
				if (! from.contains(src)){
					from.add(src);
				}
			}		
		}
		manager.setFrom(from);
		manager.setNamed(named);
		this.from = from;
		this.named = named;
		
		for (Update u : astu.getUpdates()){
			if (isDebug){
				System.out.println("** Update: " + u);
			}
			
			if (u.isBasic()){
				// load copy ...
				Basic b = u.getBasic();
				manager.process(b);
			}
			else {
				// delete insert data where
				Composite c = u.getComposite();
				process(c);
			}
		}
		
		Mappings lMap = Mappings.create(q);
		lMap.setObject(exec.getGraph());
		return lMap;
	}
	
	
	boolean process(Composite ope){
		
		switch (ope.type()){
		
		case Update.INSERT: 	insert(ope); break;
			
		case Update.DELETE: 	delete(ope); break;
		
		case Update.COMPOSITE: 	composite(ope); break;
			
		}
		return true;
		
	}
	
	
	/**
	 * insert data {<a> ex:p <b>}
	 * Ground pattern (no variable)
	 * Processed as a construct query in the target graph
	 */
	void insert(Composite ope){
		
		ASTQuery ast = createAST(ope);
		ast.setInsert(true);
		
		Exp exp = ope.getData();
		if (! exp.validateData()){
			if (isDebug) System.out.println("** Update: insert not valid: " + exp);
			query.setCorrect(false);
			return;
		}
		
		if (exp != null){
			ast.setBody(BasicGraphPattern.create());
			ast.setConst(exp);
		}
		
		// Processed as a construct (add) on target graph
		exec.query(ast);

	}
	
	/**
	 * delete data {<a> ex:p <b>}
	 * Ground pattern (no variable)
	 * Processed by Construct as a delete query in the target graph
	 * 
	 */	
	void delete(Composite ope){
		
		ASTQuery ast = createAST(ope);
		ast.setDelete(true);
		
		Exp exp = ope.getData();
		if (! exp.validateData() || ! exp.validateDelete()){
			if (isDebug){
				System.out.println("** Update: delete not valid: " + exp);
			}
			query.setCorrect(false);
			return;
		}
		
		if (exp != null){
			ast.setBody(BasicGraphPattern.create());
			ast.setDelete(exp);
		}
		
		exec.query(ast, from, named);

	}
	
	
	/**
	 * with
	 * delete {pat}
	 * insert {pat}
	 * using
	 * where {pat}
	 */
	void composite(Composite ope){
		
		// the graph where insert/delete occurs
		String src = ope.getWith();

		ASTQuery ast = createAST(ope);

		for (Composite cc : ope.getUpdates()){
				
				Exp exp = cc.getPattern();
				
				if (src != null){
					// insert in src
					exp = Source.create(ast.createConstant(src), exp);
				}
				
				if (cc.type() == Update.INSERT){
					// insert {exp}
					ast.setInsert(true);
					ast.setConst(exp);
				}
				else {
					// delete {exp}
					ast.setDelete(true);
					ast.setDelete(exp);
					
					if (! exp.validateDelete()){
						query.setCorrect(false);
						return;
					}
				}
		}

		Mappings map = exec.query(ast, from, named);
		if (isDebug) System.out.println(map);	
	}
	
	
	/**
	 * Create an AST with the where part (empty for data update)
	 * 
	 */
	ASTQuery createAST(Composite ope){
		ASTQuery ast = ASTQuery.create();
		ast.setNSM(ope.getNSM());
		ast.setSelectAll(true);
		// where {pat}
		ast.setBody(ope.getBody());		
		
		for (String uri : ope.getUsing()){
			// using -> from
			ast.setFrom(uri);
		}
		for (String uri : ope.getNamed()){
			// using named -> from named
			ast.setNamed(uri);
		}

		String src = ope.getWith();
		if (src!=null && ope.getUsing().size()==0){
			ast.setFrom(src);
		}
		
		return ast;
	}
	

	
}
