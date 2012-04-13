package junit;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Service;
import fr.inria.edelweiss.kgenv.api.QueryVisitor;
import fr.inria.edelweiss.kgram.core.Query;

public class VisitorImpl implements QueryVisitor {

	/**
	 * Called after parsing and before compiling into Query
	 */
	public void visit(ASTQuery ast) {
		Exp body = ast.getBody();
		Exp exp = body.get(0);
		
		Exp serv = Service.create(Constant.create("http://localhost:8080/corese/sparql"), BasicGraphPattern.create(exp));
		body.set(0, serv);
		System.out.println(ast);
	}

	/**
	 * Called before sorting edge and filter
	 */
	public void visit(Query query) {
		System.out.println(query);

	}
	
	

}
