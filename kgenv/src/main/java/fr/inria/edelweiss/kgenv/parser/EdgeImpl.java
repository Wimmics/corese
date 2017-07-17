package fr.inria.edelweiss.kgenv.parser;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import java.util.ArrayList;

import fr.inria.acacia.corese.triple.cst.RDFS;
import fr.inria.acacia.corese.triple.parser.Atom;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgram.api.core.DatatypeValue;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Pointerable;
import fr.inria.edelweiss.kgram.core.PointerObject;

public class EdgeImpl extends PointerObject implements Edge, Entity {

	public static String TOP = RDFS.RootPropertyURI;
	ArrayList<Node> nodes;
	Node edgeNode, edgeVariable, mySelf;
	String label;
	Triple triple;
	int index = -1;
    
	
	public EdgeImpl(){
		this(TOP);
	}
	
	public EdgeImpl(String label){
		this.label = label;
		nodes = new ArrayList<Node>();
	}
	
	public EdgeImpl(Triple t){
		label = t.getProperty().getLongName();
		triple = t;
		nodes = new ArrayList<Node>();
	}
	
	public static EdgeImpl create(String label, Node sub, Node obj){
		EdgeImpl edge = new EdgeImpl(label);
		edge.add(sub);
		edge.add(obj);
		return edge;
	}
	
	public static EdgeImpl create(Node prop, Node sub, Node obj){
		String name = TOP;
		if (prop.isConstant()){
			name = prop.getLabel();
		}
		EdgeImpl edge = new EdgeImpl(name);
		edge.add(sub);
		edge.add(obj);
		if (prop.isVariable()){
			//edge.setEdgeNode(prop);
			edge.setEdgeVariable(prop);
		}
		else {
			edge.setEdgeNode(prop);
		}
		return edge;
	}
	
	
        @Override
	public String toString(){
		String str = "";
		String name = label;
		if (getEdgeVariable()!=null) name = getEdgeVariable().toString();
		else if (triple!=null) name = triple.getProperty().getName();
		str += getNode(0) + " " + name ;
		for (int i=1; i<nodes.size(); i++){
			str += " " + getNode(i);
		}
		return str;
	}
        
        @Override
     public Iterable<DatatypeValue> getLoop() {
       ArrayList<DatatypeValue> list = new ArrayList();
       for (int i = 0; i<3; i++){
           list.add(getValue(null, i));
       }
       return list;
    }
        
     @Override
     public DatatypeValue getValue(String var, int n){        
        switch (n){
            case 0: return getNode(0).getDatatypeValue();
            case 1: return getPredicate().getDatatypeValue();                  
            case 2: return getNode(1).getDatatypeValue();
        }
        return null;
    }   
     
     public Node getPredicateNode(){
        Node var = getEdgeVariable();
        return (var == null) ? getEdgeNode() : var;
     }
	
	public Triple getTriple(){
		if (triple == null) triple = triple();
		return triple;
	}
	
	Triple triple(){
		Atom subject = ((NodeImpl)nodes.get(0)).getAtom();
		Atom object  = ((NodeImpl)nodes.get(1)).getAtom();
		Constant property = getName();
		Variable variable = getVariable();
		Triple triple = Triple.create(subject, property, variable, object);
		return triple;
	}
	
	Constant getName(){
		Atom name;
		if (edgeNode != null){
			name = ((NodeImpl)edgeNode).getAtom();
			return name.getConstant();
		}
		return Constant.create(label);
	}
	
	Variable getVariable(){
		if (edgeVariable != null){
			return((NodeImpl)edgeVariable).getAtom().getVariable();
		}
		return null;
	}
	
	public void add(Node node){
		nodes.add(node);
	}
	
	/**
	 * 
	 * Query edge node is stored only if it is a variable
	 * otherwise it is useless and may lead to a pb when match subproperty 
	 */
	public void setEdgeNode(Node n){
		edgeNode = n;
	}

	@Override
	public boolean contains(Node n) {
		// TODO Auto-generated method stub
		return nodes.contains(n);
	}

	@Override
	public Node getEdgeNode() {
		// TODO Auto-generated method stub
		return edgeNode;
	}
	
        @Override
	public Node getEdgeVariable() {
		// TODO Auto-generated method stub
		return edgeVariable;
	}
        
        @Override
        public Node getPredicate(){
            if (edgeVariable == null){
                return edgeNode;
            }
            return edgeVariable;
        }
	
	public void setEdgeVariable(Node n){
		edgeVariable = n;
	}

	@Override
	public int getIndex() {
		// TODO Auto-generated method stub
		return index;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return label;
	}

	@Override
	public Node getNode(int n) {
		// TODO Auto-generated method stub
		return nodes.get(n);
	}

//	@Override
//	public boolean match(Edge edge) {
//		// TODO Auto-generated method stub
//		return true;
//	}

	@Override
	public int nbNode() {
		// TODO Auto-generated method stub
		return nodes.size();
	}

	@Override
	public void setIndex(int n) {
		// TODO Auto-generated method stub
		index = n;
	}

	public Edge getEdge() {
		return this;
	}

	public Node getGraph() {
		return null;
	}

	public Node getNode() {
            if (mySelf == null){
                mySelf = DatatypeMap.createObject(this.toString(), this);
            }
            return mySelf;
	}

    @Override
    public Object getProvenance() {
        return null;        
    }
    
    public void setProvenance(Object obj){
        
    }



    @Override
    public int pointerType() {
        return Pointerable.ENTITY_POINTER;
    }

    @Override
    public Entity getEntity() {
        return this;
    }

}
