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
	
    default Graph visit(Graph g) {
        return g;
    }
	
    default ASTQuery visit(ASTQuery ast) {
        return ast;
    }
	
    default Edge visit(Edge ent) {
        return ent;
    }

    default Query visit(Query q) {
        return q;
    }

}
