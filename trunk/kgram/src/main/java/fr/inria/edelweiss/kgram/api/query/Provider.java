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
	
	Mappings service(Node serv, Exp exp, Environment env);
	
	Mappings service(Node serv, Exp exp, Mappings map, Environment env);

	void set(String uri, double version);

}
