//package fr.inria.corese.kgram.tool;
//
//
//import fr.inria.corese.kgram.api.core.Node;
//
///**
// * To implement from/named URI as Node
// * To be uniform (kgram manages only nodes)
// * @author corby
// *
// */
//public class NodeImpl implements Node {
//	
//	String label;
//	int index;
//	Object object;
//	
//	public NodeImpl(String l){
//		label = l;
//	}
//	
//	public static NodeImpl create(String label){
//		return new NodeImpl(label);
//	}
//	
//	public String toString(){
//		return label;
//	}
//
//	@Override
//	public int getIndex() {
//		// TODO Auto-generated method stub
//		return index;
//	}
//
//	@Override
//	public String getLabel() {
//		// TODO Auto-generated method stub
//		return label;
//	}
//	
//	public Object getValue(){
//		return null;
//	}
//
//	@Override
//	public boolean same(Node n) {
//		// TODO Auto-generated method stub
//		return label.equals(n.getLabel());
//	}
//
//	@Override
//	public void setIndex(int n) {
//		// TODO Auto-generated method stub
//		index = n;
//	}
//
//	@Override
//	public int compare(Node node) {
//		// TODO Auto-generated method stub
//		return label.compareTo(node.getLabel());
//	}
//
//	@Override
//	public boolean isVariable() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//	
//	public boolean isConstant() {
//		// TODO Auto-generated method stub
//		return true;
//	}
//
//	@Override
//	public boolean isBlank() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	public Object getObject() {
//		return object;
//	}
//
//	public void setObject(Object o) {
//		object = o;
//	}
//
//	@Override
//	public Object getProperty(int p) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void setProperty(int p, Object o) {
//		// TODO Auto-generated method stub
//		
//	}
//
//}
