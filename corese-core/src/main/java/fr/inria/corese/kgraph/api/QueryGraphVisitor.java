package fr.inria.corese.kgraph.api;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.corese.kgraph.core.Graph;

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
