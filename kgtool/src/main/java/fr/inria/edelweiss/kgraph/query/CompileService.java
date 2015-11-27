package fr.inria.edelweiss.kgraph.query;

import java.util.ArrayList;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.acacia.corese.triple.parser.Values;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Provider;
import fr.inria.edelweiss.kgram.core.Group;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgtool.transform.Transformer;

public class CompileService {
	
	Provider provider;
        Group group;
	
	public CompileService(Provider p){
            provider = p;
	}

	/**
	 * Generate bindings for the service, if any
	 */
	public void compile(Node serv, Query q, Mappings lmap, Environment env, int start, int limit){
            Query out = q.getOuterQuery();
		if (lmap == null || (lmap.size() == 1 && lmap.get(0).size() == 0)){
                    // lmap may contain one empty Mapping
                    // use env because it may have bindings
                    if (isValues(out)) {
                        bindings(q, env);
                    } else if (isFilter(out) || provider.isSparql0(serv)) {
                        filter(q, env);
                    } else {
                        bindings(q, env);
                    }
		}
		else if (isValues(out)){
                    bindings(q, lmap, start, limit);
                }
                else if (isFilter(out) || provider.isSparql0(serv)){
			filter(q, lmap, start, limit);
		}
		else {
			bindings(q, lmap, start, limit);
		}		
	}
        
      String getBind(Query q) {
        Transformer t = (Transformer) q.getTransformer();
        if (t != null) {
            Context c = t.getContext();
            IDatatype dt = c.get(Context.STL_BIND);
            if (dt != null){
                return dt.getLabel();
            }
        }
        return null;
        }
        
        
       boolean isValues(Query q) {
           String str = getBind(q);
           return str != null && str.equals(Context.STL_VALUES);
        }
       
       boolean isFilter(Query q) {
           String str = getBind(q);
           return str != null && str.equals(Context.STL_FILTER);
        }
	
	public void prepare(Query q){
		Query g 	 = q.getGlobalQuery();
		ASTQuery ast = (ASTQuery) q.getAST();
		ASTQuery ag  = (ASTQuery) g.getAST();
		ast.setPrefixExp(ag.getPrefixExp());
	}
	
	public int slice(Query q){
		Query g  = q.getOuterQuery();
		return g.getSlice();
	}
	
	boolean isMap(Query q){
		Query g  = q.getOuterQuery();
		return g.isMap();
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
                
               if (ast.getSaveBody() == null) {
                ast.setSaveBody(ast.getBody());
            }
            BasicGraphPattern body = BasicGraphPattern.create();
            if (lvar.size() > 0) {
                Values values = Values.create(lvar, lval);
                body.add(values);
            }
            for (Exp e : ast.getSaveBody()) {
                body.add(e);
            }
            ast.setBody(body);
		
//		if (lvar.size()>0){
//			Values values = Values.create(lvar, lval);
//			ast.setValues(values);
//		}
	}
	
	/**
	 * Generate bindings as bindings from Mappings
	 */
	void bindings(Query q, Mappings lmap, int start, int limit){
		ASTQuery ast = (ASTQuery) q.getAST();
		ast.clearBindings();
		ArrayList<Variable> lvar = new ArrayList<Variable>();
		ArrayList<Constant> lval; 
		Values values = Values.create();
		
		for (Node qv : q.getSelect()){
			String var = qv.getLabel();
			lvar.add(Variable.create(var));
		}
		
		values.setVariables(lvar);
                
                for (int j = start; j < lmap.size() && j < limit; j++){
			
			Mapping map = lmap.get(j);			
			boolean ok = false;
			lval = new ArrayList<Constant>();
			
			for (Node var : q.getSelect()){
				Node val = map.getNode(var);

				if (val != null){
					IDatatype dt = (IDatatype) val.getValue();
					Constant cst = Constant.create(dt);
					lval.add(cst);
					ok = true;
				}
				else {
					lval.add(null);
				}
			}
			
			if (ok){
				values.addValues(lval);
			}
		}
                
            if (ast.getSaveBody() == null) {
                ast.setSaveBody(ast.getBody());
            }
            BasicGraphPattern body = BasicGraphPattern.create();
            if (values.getValues().size() > 0) {
                body.add(values);
            }
            for (Exp e : ast.getSaveBody()) {
                body.add(e);
            }
            ast.setBody(body);

//                if (values.getValues().size()>0){
//			ast.setValues(values);
//		}

	}
	
	
	/**
	 * Generate bindings as a string
	 *  values () {()} syntax 
	 */
	StringBuffer strBindings(Query q, Mappings map){
//                if (group == null){
//                    group =  Group.instance(q.getSelectFun());
//                }
                
		String SPACE = " ";
		StringBuffer sb = new StringBuffer();
		
		sb.append("values (");
		
		for (Node qv : q.getSelect()){
			sb.append(qv.getLabel());
			sb.append(SPACE);
		}
		sb.append("){");
		
		for (Mapping m : map){
                    
//                        if (! group.isDistinct(m)){
//                                continue;
//                        }
                    
			sb.append("(");
			
			for (Node var : q.getSelect()){
				Node val = m.getNode(var);
				if (val == null){
					sb.append("UNDEF");
				}
				else {
					sb.append(val.getValue().toString());
				}
				sb.append(SPACE);
			}
			
			sb.append(")");
		}
		
		sb.append("}");
		return sb;

	}

	
	/**
	 * Search select variable of query that is bound in env
	 * Generate binding for such variable as filters
	 * Set filters in ASTQuery
	 */
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
		
                if (ast.getSaveBody() == null){
                    ast.setSaveBody(ast.getBody());						
                 }
                
                BasicGraphPattern body = BasicGraphPattern.create();
                for (Exp e : ast.getSaveBody()){                            
                     body.add(e);
                }
                        
		if (lt.size()>0){
			Term f = lt.get(0);
			for (int i = 1; i<lt.size(); i++){
				f = Term.create(Term.SEAND, f, lt.get(i));
			}						
			//body.add(ast.getSaveBody());
			body.add(Triple.create(f));
		}
                
		ast.setBody(body);

	}
	
	/**
	 * Generate bindings from Mappings as filter
	 */
	
        void filter(Query q, Mappings lmap, int start, int limit){
            
		ASTQuery ast = (ASTQuery) q.getAST();
		ArrayList<Term> lt;
		Term filter = null;
//		if (group == null){
//                    group =  Group.instance(q.getSelectFun());
//                }
		
		for (int j = start; j < lmap.size() && j < limit; j++){
			
			Mapping map = lmap.get(j);
                       
//			if (! group.isDistinct(map)){
//                            continue;
//			}
			
			lt = new ArrayList<Term>();
			
			for (Node qv : q.getSelect()){
				String var = qv.getLabel();
				Node val   = map.getNode(var);
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
				
				if (filter == null){
					filter = f;
				}
				else {
					filter = Term.create(Term.SEOR, filter, f);
				}
			}
		
		}		
		
		if (ast.getSaveBody() == null){
			ast.setSaveBody(ast.getBody());
		}
		
		BasicGraphPattern body = BasicGraphPattern.create();
		//body.add(ast.getSaveBody());
                for (Exp e : ast.getSaveBody()){                            
                            body.add(e);
                }
		if (filter != null) {
			body.add(Triple.create(filter));
		}
		ast.setBody(body);
		
	}


}
