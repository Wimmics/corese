package fr.inria.corese.core.api;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.kgram.api.core.Entity;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Graph;

/**
 * 
 * Visitor Design Pattern to rewrite an RDF Graph into a SPARQL BGP Query Graph 
 * 
 */
public interface QueryGraphVisitor {
	
	Graph visit(Graph g);
	
	ASTQuery visit(ASTQuery ast);
	
	Entity visit(Entity ent);

	Query visit(Query q);

}
