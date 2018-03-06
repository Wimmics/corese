package fr.inria.corese.kgraph.core;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.PointerObject;

public class EntityImpl extends PointerObject implements Entity {
	Node graph, node;

	EntityImpl(Node g, Node n){
		graph = g;
		node = n;
	}
	
	public static EntityImpl create(Node g, Node n){
		return new EntityImpl(g, n);
	}
	
	public Edge getEdge() {

		return null;
	}

	
	public Node getNode() {

		return node;
	}

	
	public Node getGraph() {

		return graph;
	}

	@Override
	public Node getNode(int i) {
		return null;
	}

	@Override
	public int nbNode() {
		return 0;
	}

    @Override
    public Object getProvenance() {
        return null;    
    }
    
    public void setProvenance(Object obj){
        
    }

    @Override
    public Iterable getLoop() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
