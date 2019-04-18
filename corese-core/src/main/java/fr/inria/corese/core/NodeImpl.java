package fr.inria.corese.core;


import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.TripleStore;
import java.util.Objects;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.path.Path;

/**
 * Node
 *
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public class NodeImpl extends GraphObject implements Node,  Comparable<NodeImpl> {

//    String key = INITKEY;
    public static boolean byIDatatype = false;
    Graph graph;
    int index = -1;
    IDatatype dt;

    NodeImpl(IDatatype val) {
        dt = val;
    }

    public static Node create(IDatatype val) {
        if (byIDatatype) {
            return val;
        }
        return new NodeImpl(val);
    }

    NodeImpl(IDatatype val, Graph g) {
        graph = g;
        dt = val;
    }

    public static NodeImpl create(IDatatype val, Graph g) {
        return new NodeImpl(val, g);
    }

    @Override
    public String toString() {
        return getValue().toSparql();
    }

    @Override
    public int compare(Node node) {
        // TODO Auto-generated method stub
        return getValue().compareTo((IDatatype) node.getValue());
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String getLabel() {
        // TODO Auto-generated method stub
        return getValue().getLabel();
    }

    @Override
    public IDatatype getValue() {
        return dt;
    }
    
    @Override
    public IDatatype getDatatypeValue() {
        return dt;
    }
    
    @Override
    public void setDatatypeValue(DatatypeValue dt) {
        if (dt instanceof IDatatype) {
            setDatatypeValue((IDatatype) dt);
        }
    }
    
    public void setDatatypeValue(IDatatype dt) {
        this.dt = dt;
    }

    @Override
    public boolean isBlank() {
        // TODO Auto-generated method stub
        return getValue().isBlank();
    }

    @Override
    public boolean isConstant() {
        // TODO Auto-generated method stub
        return getValue().isConstant();
    }

    @Override
    public boolean isVariable() {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public boolean isFuture(){
        return dt.isFuture();
    }

    @Override
    public boolean same(Node node) {
        return getValue().sameTerm((IDatatype) node.getValue());
    }
    
     @Override
    public boolean match(Node node) {
        return getValue().match((IDatatype) node.getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Node) {
            //return same((Node) obj);
            return getValue().equals(((Node) obj).getValue());
        }
        return false;
    }

    @Override
    public int hashCode() {
		return Objects.hashCode(this.dt);
//		int hash = 7;
//		// hash = 67 * hash + (this.key != null ? this.key.hashCode() : 0);
//		hash = 67 * hash + (this.dt != null ? this.dt.hashCode() : 0);
//		return hash;
    }

    @Override
    public void setIndex(int n) {
        index = n;
    }

    @Override
    public Edge getEdge() {
        return null;
    }

    @Override
    public Node getGraph() {
        return null;
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public Object getObject() {
         return dt.getObject();
    }

    @Override
    public void setObject(Object o) {
        dt.setObject(o);
    }
    
    @Override
    public Path getPath() {       
        return dt.getPath();
    }


    @Override
    public Object getProperty(int p) {
        return null;
    }

    @Override
    public void setProperty(int p, Object o) {
    }

 
    
    @Override
    public String getKey() {
        return null;
    }

    @Override
    public void setKey(String str) {
        
    }


    @Override
    public Iterable getLoop() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TripleStore getTripleStore() {
        return graph;
    }

	@Override
	public int compareTo(NodeImpl o) {
		return o.dt.compareTo(dt);
	}
}
