package fr.inria.edelweiss.kgraph.query;

import java.util.HashMap;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Provider;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;

/**
 * Implements service expression using local QueryExec
 * 
 * @author Olivier Corby, Edelweiss INRIA 2011
 *
 */
public class ProviderImpl implements Provider {
	
	HashMap<String, QueryProcess> table;
	
	ProviderImpl(){
		table = new HashMap<String, QueryProcess>();
	}
	
	public static ProviderImpl create(){
		return new ProviderImpl();
	}
	
	public void add(String uri, Graph g){
		QueryProcess exec = QueryProcess.create(g);
		exec.set(this);
		table.put(uri, exec);
	}

	public Mappings service(Node serv, Exp exp) {
		QueryProcess exec = table.get(serv.getLabel());
		Query q = exp.getQuery();
		Mappings map;
		if (exec == null){
			map = Mappings.create(q);
		}
		else {
			ASTQuery ast = exec.getAST(q);
			map = exec.query(ast);
		}
		return map;
	}

}
