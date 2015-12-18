package fr.inria.edelweiss.kgram.api.core;

/**
 * Interface for Producer iterator that encapsulate Edge or Node with its Graph Node
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public interface Entity extends Loopable, Pointerable {
	
	Edge getEdge();
	
	Node getNode();
	
	Node getNode(int i);
	
	int nbNode();
	
	Node getGraph();
        
        Object getProvenance();
        
        void setProvenance(Object obj);

}
