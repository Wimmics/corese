package fr.inria.edelweiss.kgraph.query;


import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.update.Basic;
import fr.inria.edelweiss.kgram.core.Mappings;


/**
 * SPARQL 1.1 Update
 *  
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public interface Manager {

	boolean process(Basic b);

	Mappings query(ASTQuery ast);
	

}
