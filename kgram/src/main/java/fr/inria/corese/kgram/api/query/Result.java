package fr.inria.corese.kgram.api.query;

import fr.inria.corese.kgram.api.core.Node;

/**
 * Interface to KGRAM elementary result (a mapping)
 * 
 * @author corby
 *
 */
public interface Result {

	Node getNode(String name);
	
	Node getNode(Node node);

}
