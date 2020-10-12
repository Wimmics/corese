package fr.inria.corese.core.api;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.exceptions.EngineException;

/**
 * 
 * @author Olivier Corby, Wimmics INRIA 2012
 */
public interface Engine {
	
	static final int UNDEF = -1;
	static final int RDFS_ENGINE = 0;
	static final int RULE_ENGINE = 1;
	static final int QUERY_ENGINE = 2;
	static final int WORKFLOW_ENGINE = 3;

	
	// temporarily desactivate 
	void setActivate(boolean b);
	
	boolean isActivate();
	
	void init();

	// return true if some new entailment have been performed 
	boolean process() throws EngineException ;
	
	// remove entailments
	void remove();
	
	// some edges have been deleted
	void onDelete();
	
	// edge inserted
	void onInsert(Node gNode, Edge edge);
	
	// graph have been cleared
	void onClear();
	
	int type();


}
