package fr.inria.corese.kgenv.api;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.corese.kgram.core.Query;

public interface QueryVisitor {
	
	void visit(ASTQuery ast);
	
	void visit(Query query);


}
