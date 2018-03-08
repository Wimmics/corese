package fr.inria.corese.kgenv.api;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.kgram.core.Query;

public interface QueryVisitor {
	
	void visit(ASTQuery ast);
	
	void visit(Query query);


}
