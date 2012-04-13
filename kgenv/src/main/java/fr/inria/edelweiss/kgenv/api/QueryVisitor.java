package fr.inria.edelweiss.kgenv.api;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.kgram.core.Query;

public interface QueryVisitor {
	
	void visit(ASTQuery ast);
	
	void visit(Query query);


}
