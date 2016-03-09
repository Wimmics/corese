package fr.inria.edelweiss.kgram.api.query;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mappings;

/**
 * Service Provider
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public interface Provider {
	
	/** Execute the request in exp on the serv node, with the env environment,
	 *  and returns the mapping obtained.
	 *  @param serv Server on which to execute the query.
	 *  @param exp  SPARQL request.
	 *  @param env  Environment to use when applying the request.
	 *  @return The mapping of the variables to their values.
	 */
	Mappings service(Node serv, Exp exp, Environment env);
	
	/**
	 * @param map Pre-existing mappings.
	 * @return 
	 * @see #service(fr.inria.edelweiss.kgram.api.core.Node, fr.inria.edelweiss.kgram.core.Exp, fr.inria.edelweiss.kgram.api.query.Environment) 
	 */
	Mappings service(Node serv, Exp exp, Mappings map, Environment env);

	void set(String uri, double version);

        public boolean isSparql0(Node serv);

}   
