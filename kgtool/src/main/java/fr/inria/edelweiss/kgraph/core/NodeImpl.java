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
public class NodeImpl extends GraphObject implements Node, Entity {

//    String key = INITKEY;
    public static boolean byIDatatype = false;
    Graph graph;
    int index = -1;
    IDatatype dt;

    NodeImpl(IDatatype val) {
        dt = val;
    }

    public static Node create(IDatatype val) {
        if (byIDatatype){
            return val;
        }
        return new NodeImpl(val);
    }

    NodeImpl(IDatatype val, Graph g) {
        graph = g;
        dt = val;
    }

    public static Node create(IDatatype val, Graph g) {
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
//        if (graph == null) {
//            return dt;
//        } else {
//            return graph.getValue(this);
//        }
    }
    
    @Override
    public IDatatype getDatatypeValue() {
        return dt;
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
    
    public boolean isFuture(){
        return dt.isFuture();
    }

    @Override
    public boolean same(Node node) {
        // TODO Auto-generated method stub
        return getValue().sameTerm((IDatatype) node.getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Node) {
            return same((Node) obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
       // hash = 67 * hash + (this.key != null ? this.key.hashCode() : 0);
        hash = 67 * hash + (this.dt != null ? this.dt.hashCode() : 0);
        return hash;
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
         return dt.getObject();
    }

    public void setObject(Object o) {
        dt.setObject(o);
    }

    @Override
    public Node getNode(int i) {
        return null;
    }

    @Override
    public Object getProperty(int p) {
        return null;
    }

    @Override
    public void setProperty(int p, Object o) {
    }

    @Override
    public int nbNode() {
        return 0;
    }

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public void setKey(String str) {
        
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

    @Override
    public Object getGraphStore() {
        return graph;
    }
}
