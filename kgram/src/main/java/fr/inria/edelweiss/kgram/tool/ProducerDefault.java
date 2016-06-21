package fr.inria.edelweiss.kgram.tool;

import fr.inria.edelweiss.kgram.api.core.DatatypeValue;
import java.util.ArrayList;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Regex;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;


/**
 * 
 * @author corby
 *
 */
public class ProducerDefault implements Producer {
    int mode = Producer.DEFAULT;
    Node graphNode;
	
	public void setMode(int n){
            mode = n;
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
    public Producer getProducer(Node node, Environment env) {
        return null;
    }
    
    @Override
        public Query getQuery(){
            return null;
        }

    @Override
    public Object getGraph() {
        return null;
    }

    @Override
    public int getMode() {
        return mode;
    }

    @Override
    public void setGraphNode(Node n) {
        graphNode = n;
    }

    @Override
    public Node getGraphNode() {
        return graphNode;
    }

    @Override
    public Mappings getMappings(Node gNode, List<Node> from, Exp exp, Environment env) {
        //create a new Mappings: empty
        Mappings maps = new Mappings();
        return maps;
    }

    @Override
    public Object getValue(Object value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DatatypeValue getDatatypeValue(Object value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Entity copy(Entity ent) {
        return ent;
    }

}
