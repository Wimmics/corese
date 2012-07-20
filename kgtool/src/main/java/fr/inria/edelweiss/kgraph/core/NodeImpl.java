package fr.inria.edelweiss.kgraph.core;

import java.util.Arrays;

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
	static int cindex = 0;
	int index = -1;
	IDatatype dt;
	Object[] properties;
	
	
	NodeImpl(IDatatype val){
		dt = val;
	}	
	
	public static Node create(IDatatype val){
		//return new NodeImpl(val);
		return val;
	}
	
	public String toString(){
		return dt.toSparql();
	}
	
	
	public int compare(Node node) {
		// TODO Auto-generated method stub
		if (node.getValue() instanceof IDatatype){
			return dt.compareTo((IDatatype) node.getValue());
		}
		else 
			return getLabel().compareTo(node.getLabel());
		
	}
	
	
	@Override
	public int getIndex() {
		if (index == -1){
			index = cindex++;
		}
		return index;
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
	
	public boolean equals(Object obj) {
		if (obj instanceof Node){
			return same((Node) obj);
		}
		return false;
	}

	@Override
	public void setIndex(int n) {
		index = n;
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
		return getProperty(OBJECT);
	}

	public void setObject(Object o) {
		setProperty(OBJECT, o);
	}

	@Override
	public Node getNode(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getProperty(int p) {
		if (properties == null || p >= properties.length ){
			return null;
		}
		return properties[p];
	}

	@Override
	public void setProperty(int p, Object o) {
		if (properties == null){
			properties = new Object[PSIZE];
		}
		if (p >= properties.length){
			properties = Arrays.copyOf(properties, p+1);
		}
		properties[p] = o;
	}

	@Override
	public int nbNode() {
		return 0;
	}

}
