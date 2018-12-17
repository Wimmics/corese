package fr.inria.corese.sparql.api;

import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.kgram.core.Query;

public interface QueryVisitor {
	
	void visit(ASTQuery ast);
	
	void visit(Query query);
        
        default void before(Query q) {
        }
        
        default void after(Mappings map) {
        }

}
