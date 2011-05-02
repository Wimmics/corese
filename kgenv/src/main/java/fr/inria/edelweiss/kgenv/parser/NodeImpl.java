package fr.inria.edelweiss.kgenv.parser;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.cst.RDFS;
import fr.inria.acacia.corese.triple.parser.Atom;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgram.api.core.Node;

public class NodeImpl implements Node {
	
	Atom atom;
	int index = -1;
	
	public NodeImpl(Atom at){
		atom = at;
	}
	
	public static NodeImpl createVariable(String name){
		return new NodeImpl(Variable.create(name));
	}
	
	public static NodeImpl createResource(String name){
		return new NodeImpl(Constant.create(name));
	}
	
	public static NodeImpl createConstant(String name){
		return new NodeImpl(Constant.create(name, RDFS.xsdstring));
	}
	
	public static NodeImpl createConstant(String name, String datatype){
		return new NodeImpl(Constant.create(name, datatype));
	}
	
	public static NodeImpl createConstant(String name, String datatype, String lang){
		return new NodeImpl(Constant.create(name, null, lang));
	}
	
	public Atom getAtom(){
		return atom;
	}
	
	public Object getValue(){
		return atom.getValue();
	}
	
	public String toString(){
		return atom.toSparql();
	}

	@Override
	public int compare(Node node) {
		if (getValue() instanceof IDatatype && node.getValue() instanceof IDatatype){
			IDatatype dt1 = (IDatatype) getValue();
			IDatatype dt2 = (IDatatype) node.getValue();
			return dt1.compareTo(dt2);
		}
		return getLabel().compareTo(node.getLabel());
	}

	@Override
	public int getIndex() {
		// TODO Auto-generated method stub
		return index;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		if (atom.isResource())
			return atom.getLongName();
		return atom.getName();
	}

	@Override
	public boolean isConstant() {
		// TODO Auto-generated method stub
		return atom.isConstant();
	}

	@Override
	public boolean isVariable() {
		// TODO Auto-generated method stub
		return atom.isVariable() ; //&& ! atom.getVariable().isBlankNode();
	}
	
	public boolean isBlank() {
		// TODO Auto-generated method stub
		return atom.isBlank() || (isVariable() && atom.getVariable().isBlankNode());
	}

//	@Override
//	public boolean match(Node qNode) {
//		// TODO Auto-generated method stub
//		return true;
//	}

	@Override
	public boolean same(Node n) {
		// TODO Auto-generated method stub
		return compare(n) == 0;
//		if (isVariable() && n.isVariable())
//			return getLabel().equals(n.getLabel());
//		return this == n;
	}

	@Override
	public void setIndex(int n) {
		// TODO Auto-generated method stub
		index = n;
	}

}
