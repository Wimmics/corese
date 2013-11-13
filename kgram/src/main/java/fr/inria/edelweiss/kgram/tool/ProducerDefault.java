package fr.inria.edelweiss.kgram.tool;

import java.util.ArrayList;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Regex;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mappings;


/**
 * 
 * @author corby
 *
 */
public class ProducerDefault implements Producer {
	
	public void setMode(int n){
		
	}

	@Override
	public Iterable<Entity> getNodes(Node node, List<Node> from, Node node2,
			Environment env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<Entity> getEdges(Node node, List<Node> from, Edge edge,
			Environment env) {
		// TODO Auto-generated method stub
		ArrayList<Entity> list = new ArrayList<Entity> ();
		list.add( EntityImpl.create(null, edge));
		return list;
	}

	@Override
	public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge edge,  Environment env, Regex exp, 
			Node src, Node start,
			int index){
		// TODO Auto-generated method stub
		return new ArrayList<Entity> ();
	}

	@Override
	public Iterable<Node> getGraphNodes(Node node, List<Node> from,
			Environment env) {
		// TODO Auto-generated method stub
		return new ArrayList<Node> ();
	}

	
	@Override
	public void init(int nbNodes, int nbEdges) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initPath(Edge edge, int index) {
		// TODO Auto-generated method stub
		
	}

//	public Iterable<Entity> getNodes(Node gNode, List<Node> from, Edge edge,  Environment env, Regex exp, 
//			int index){
//		// TODO Auto-generated method stub
//		return null;
//	}


	@Override
	public Node getNode(Object value) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<Node> toNodeList(Object obj) {
		// TODO Auto-generated method stub
		return new ArrayList<Node> ();
	}

	@Override
	public Mappings map(List<Node> nodes, Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isGraphNode(Node node, List<Node> from, Environment env) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBindable(Node node) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterable<Entity> getNodes(Node node, List<Node> from, Edge edge,
			Environment env, List<Regex> exp, int index) {
		// TODO Auto-generated method stub
		return new ArrayList<Entity> ();
	}

 @Override
    public boolean isProducer(Node node) {
        return false;
    }

    @Override
    public Producer getProducer(Node node) {
        return null;
    }

    @Override
    public Object getGraph() {
        return null;
    }
}
