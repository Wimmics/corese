package fr.inria.edelweiss.kgraph.query;



import org.apache.log4j.Logger;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Source;
import fr.inria.acacia.corese.triple.update.ASTUpdate;
import fr.inria.acacia.corese.triple.update.Basic;
import fr.inria.acacia.corese.triple.update.Composite;
import fr.inria.acacia.corese.triple.update.Update;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;


/**
 * SPARQL 1.1 Update
 * 
 * Called by QueryProcess.query() to handle update query
 * 
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class UpdateProcess {
	private static Logger logger = Logger.getLogger(UpdateProcess.class);	

	Manager manager;
	Query query;
	
	boolean isDebug = false; 
	
	UpdateProcess(Manager m){
		manager = m;
	}
	
	public static UpdateProcess create(Manager m){
		UpdateProcess u = new UpdateProcess(m);
		return u;
	}
		
	
	/**
	 * Process an update sparql query
	 * There may be a list of queries
	 */
	
	public Mappings update(Query q){
		query = q;
		ASTQuery ast = (ASTQuery) q.getAST();
		ASTUpdate astu = ast.getUpdate();
		
		for (Update u : astu.getUpdates()){
			if (isDebug){
				logger.debug("** Update: " + u);
			}
			boolean suc = true;
			
			if (u.isBasic()){
				// load copy ...
				Basic b = u.getBasic();
				suc = manager.process(q, b);
				
			}
			else {
				// delete insert data where
				Composite c = u.getComposite();
				suc = process(q, c);
			}
			
			if (! suc){
				q.setCorrect(false);
				if (isDebug){
					logger.debug("** Failure: " + u);
				}
				break;
			}
		}
		
		Mappings lMap = Mappings.create(q);
		return lMap;
	}
	
	public void setDebug(boolean b){
		isDebug = b;
	}
	
	
	boolean process(Query q, Composite ope){
		
		switch (ope.type()){
		
		case Update.INSERT: 	insert(q, ope); break;
			
		case Update.DELETE: 	delete(q, ope); break;
		
		case Update.COMPOSITE: 	composite(q, ope); break;
			
		}
		return true;
		
	}
	
	
	/**
	 * insert data {<a> ex:p <b>}
	 * Ground pattern (no variable)
	 * Processed as a construct query in the target graph
	 */
	void insert(Query q, Composite ope){
		
		ASTQuery ast = createAST(q, ope);
		ast.setInsert(true);
		
		Exp exp = ope.getData();
		if (! exp.validateData()){
			if (isDebug) logger.debug("** Update: insert not valid: " + exp);
			query.setCorrect(false);
			return;
		}
		
		if (exp != null){
			ast.setBody(BasicGraphPattern.create());
			ast.setInsert(exp);
		}
		
		// Processed as a construct (add) on target graph
		manager.query(q, ast);

	}
	
	/**
	 * delete data {<a> ex:p <b>}
	 * Ground pattern (no variable)
	 * Processed by Construct as a delete query in the target graph
	 * 
	 */	
	void delete(Query q, Composite ope){
		
		ASTQuery ast = createAST(q, ope);
		ast.setDelete(true);
		
		Exp exp = ope.getData();
		if (! exp.validateData() || ! exp.validateDelete()){
			if (isDebug){
				logger.debug("** Update: delete not valid: " + exp);
			}
			query.setCorrect(false);
			return;
		}
		
		if (exp != null){
			ast.setBody(BasicGraphPattern.create());
			ast.setDelete(exp);
		}
		
		manager.query(q, ast);

	}
	
	
	/**
	 * with
	 * delete {pat}
	 * insert {pat}
	 * using
	 * where {pat}
	 */
	void composite(Query q, Composite ope){
		
		// the graph where insert/delete occurs
		Constant src = ope.getWith();

		ASTQuery ast = createAST(q, ope);

		for (Composite cc : ope.getUpdates()){
				
				Exp exp = cc.getPattern();
				
				if (src != null){
					// insert in src
					exp = Source.create(src, exp);
				}
				
				if (cc.type() == Update.INSERT){
					// insert {exp}
					ast.setInsert(true);
					ast.setInsert(exp);
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

		Mappings map = manager.query(q, ast);
		if (isDebug) logger.debug(map);	
	}
	
	
	/**
	 * Create an AST with the where part (empty for data update)
	 * 
	 */
	ASTQuery createAST(Query q, Composite ope){
		ASTQuery ast = ASTQuery.create();
		ASTQuery ga  = (ASTQuery) q.getAST();
		ast.setNSM(ope.getNSM());
		ast.setPrefixExp(ga.getPrefixExp());
		ast.setSelectAll(true);
		// where {pat}
		ast.setBody(ope.getBody());		
		
		for (Constant uri : ope.getUsing()){
			// using -> from
			ast.setFrom(uri);
		}
		for (Constant uri : ope.getNamed()){
			// using named -> from named
			ast.setNamed(uri);
		}

		Constant src = ope.getWith();
		if (src!=null && ope.getUsing().size()==0){
			ast.setFrom(src);
		}
		
		return ast;
	}
	

	
}
