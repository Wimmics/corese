package fr.inria.corese.core.api;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;

/**
 * 
 * Visitor Design Pattern to rewrite an RDF Graph into a SPARQL BGP Query Graph 
 * 
 */
public interface QueryGraphVisitor {
	
	Graph visit(Graph g);
	
	ASTQuery visit(ASTQuery ast);
	
	Edge visit(Edge ent);

	Query visit(Query q);

}
