package fr.inria.corese.kgraph.query;



import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.Source;
import fr.inria.corese.sparql.triple.update.ASTUpdate;
import fr.inria.corese.sparql.triple.update.Basic;
import fr.inria.corese.sparql.triple.update.Composite;
import fr.inria.corese.sparql.triple.update.Update;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;


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
	private static Logger logger = LogManager.getLogger(UpdateProcess.class);	

	Manager manager;
        QueryProcess exec;
	Query query;
        Dataset ds;
	
	boolean isDebug = false; 
	
	UpdateProcess(QueryProcess e, Dataset ds){
		manager = e.getManager();
                exec = e;
                this.ds = ds;
	}
	
	public static UpdateProcess create(QueryProcess e,  Dataset ds){
		UpdateProcess u = new UpdateProcess(e,  ds);
		return u;
	}
		
	
	/**
	 * Process an update sparql query
	 * There may be a list of queries
	 * return the Mappings of the last update ...
	 */
	
	public Mappings update(Query q){
		query = q;
		ASTQuery ast = (ASTQuery) q.getAST();
		ASTUpdate astu = ast.getUpdate();
		Mappings map = Mappings.create(q);
		NSManager nsm = null;
		
		for (Update u : astu.getUpdates()){
			if (isDebug){
				logger.debug("** Update: " + u);
			}
			boolean suc = true;
			
			
			switch (u.type()){
			
				case Update.PROLOG: 
					// local prolog, get the local NSManager
					// for next operations
					nsm = u.getLocalNSM();
					break;
					
				default:
					// assign local NSManager to current operation
					if (nsm != null){
						u.setLocalNSM(nsm);
					}					
			}
			
			
			if (u.isBasic()){
				// load copy ...
				Basic b = u.getBasic();
				suc = manager.process(q, b, ds);
			}
			else {
				// delete insert data where
				Composite c = u.getComposite();
				map = process(q, c);
			}
			
			if (! suc){
				q.setCorrect(false);
				if (isDebug){
					logger.debug("** Failure: " + u);
				}
				break;
			}
		}
		return map;
	}
	
	public void setDebug(boolean b){
		isDebug = b;
	}
	
	
	Mappings process(Query q, Composite ope){
		
		switch (ope.type()){
		
		case Update.INSERT: 	return insert(q, ope); 
			
		case Update.DELETE: 	return delete(q, ope); 
		
		case Update.COMPOSITE: 	return composite(q, ope); 
			
		}
		
		return Mappings.create(q);
		
	}
        
        
        /**
	 * query is the global Query
	 * ast is the current update action
         * use case: 
         * delete insert data
         * delete insert where
         * In case of data, fake an empty where and process as a where update.
	 */
             Mappings update(Query query, ASTQuery ast) {
		
		//System.out.println("** QP:\n" + ast);
		exec.logStart(query);
                
		Mappings lMap = exec.basicQuery(ast, ds);
		Query q = lMap.getQuery();
		
		// PRAGMA: update can be both delete & insert
		if (q.isDelete()){
			manager.delete(q, lMap, ds);
		}
		
		if (q.isConstruct()){ 
			// insert
			manager.insert(q, lMap, ds);
		}
                
		exec.logFinish(query, lMap);

		return lMap;
	}
             
         
	   
	
	
	/**
	 * insert data {<a> ex:p <b>}
	 * Ground pattern (no variable)
	 * Processed as a construct query in the target graph
	 */
	Mappings insert(Query q, Composite ope){
		
		ASTQuery ast = createAST(q, ope);
		ast.setInsert(true);
		
		Exp exp = ope.getData();
		if (! exp.validateData(ast)){
			if (isDebug) logger.debug("** Update: insert not valid: " + exp);
			q.setCorrect(false);
			return Mappings.create(q);
		}
		
		if (exp != null){
			ast.setBody(BasicGraphPattern.create());
			ast.setInsert(exp);
			ast.setInsertData(true);
		}
		
		// Processed as a construct (add) on target graph
		//return manager.query(q, ast);
		return update(q, ast);

	}
        
        
	
	/**
	 * delete data {<a> ex:p <b>}
	 * Ground pattern (no variable)
	 * Processed by Construct as a delete query in the target graph
	 * 
	 */	
	Mappings delete(Query q, Composite ope){
		
		ASTQuery ast = createAST(q, ope);
		ast.setDelete(true);
		
		Exp exp = ope.getData();
		if (! exp.validateData(ast) || ! exp.validateDelete()){
			q.setCorrect(false);
			q.addError("** Update: delete not valid: " , exp);
			return Mappings.create(q);
		}
		
		if (exp != null){
			ast.setBody(BasicGraphPattern.create());
			ast.setDelete(exp);
			ast.setDeleteData(true);
		}
		
		return update(q, ast);

	}
	
	
	/**
	 * with
	 * delete {pat}
	 * insert {pat}
	 * using
	 * where {pat}
	 */
	Mappings composite(Query q, Composite ope){
		
		// the graph where insert/delete occurs
		Constant src = ope.getWith();

		ASTQuery ast = createAST(q, ope);

		for (Composite cc : ope.getUpdates()){
				
				Exp exp = cc.getPattern();
				
				if (src != null){
					// insert in src
					exp = Source.create(src, exp);
                                        exp = BasicGraphPattern.create(exp);
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
						q.setCorrect(false);
						q.addError("Error: Blank Node in Delete", "");
						return Mappings.create(q);
					}
				}
		}

		Mappings map = update(q, ast);
		//if (isDebug) logger.debug(map);	
		return map;
	}
	
	
	/**
	 * Create an AST with the where part (empty for data update)
	 * 
	 */
	ASTQuery createAST(Query q, Composite ope){
		ASTQuery ast = ASTQuery.create();
		ASTQuery ga  = (ASTQuery) q.getAST();
		ast.setNSM(ope.getNSM());	
		ast.setPragma(ga.getPragma());
		ast.setPrefixExp(ga.getPrefixExp());
                ast.setDefine(ga.getDefine());
		ast.setSelectAll(true);
		// where {pat}
		ast.setBody(ope.getBody());		
                ast.setValues(ope.getValues());
               if (ope.getDataset().isEmpty()){
                   if (ope.getWith() != null){
                        // SPARQL requires that if there is WITH and if the endpoint Dataset has named graph
                        // the named graph are still in the Dataset
                        // so in this case, do not complete() the Dataset
                        ast.getDataset().setWith(ope.getWith());
                   }
               }
               else {
                    ast.setDataset(ope.getDataset());                   
               }
		
 		
		return ast;
	}
	

	
}
