package fr.inria.edelweiss.kgraph.core;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;

/**
 * Node
 * 
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public class NodeImpl implements Node, Entity {
	IDatatype dt;
	Object object;
	
	
	public NodeImpl(IDatatype val){
		dt = val;
	}	
	
	public static NodeImpl create(IDatatype val){
		return new NodeImpl(val);
	}
	
	public String toString(){
		return dt.toSparql();
	}
	
	
	@Override
	public int compare(Node node) {
		// TODO Auto-generated method stub
		if (node.getValue() instanceof IDatatype){
			return dt.compareTo((IDatatype) node.getValue());
		}
		else return getLabel().compareTo(node.getLabel());
		
	}

	@Override
	public int getIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return dt.getLabel();
	}

	@Override
	public IDatatype getValue() {
		// TODO Auto-generated method stub
		return dt;
	}
	

	@Override
	public boolean isBlank() {
		// TODO Auto-generated method stub
		return dt.isBlank();
	}

	@Override
	public boolean isConstant() {
		// TODO Auto-generated method stub
		return ! dt.isBlank();
	}

	@Override
	public boolean isVariable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean same(Node node) {
		// TODO Auto-generated method stub
		if (node.getValue() instanceof IDatatype){
			return dt.sameTerm((IDatatype)node.getValue());
		}	
		return getLabel().equals(node.getLabel());	
	}

	@Override
	public void setIndex(int n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Edge getEdge() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getGraph() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getNode() {
		// TODO Auto-generated method stub
		return this;
	}
	
	public Object getObject() {
		return object;
	}

	public void setObject(Object o) {
		object = o;
	}

}
