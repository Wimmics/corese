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

    String key = INITKEY;
    Graph graph;
    // these fields can be removed:
    // index is used when an RDF graph is used as a query graph
    int index = -1;
    // dt is used when the graph does not manage values
    IDatatype dt;

    NodeImpl(IDatatype val) {
        dt = val;
    }

    public static Node create(IDatatype val) {
        return new NodeImpl(val);
    }

    NodeImpl(Graph g, IDatatype val) {
        graph = g;
        dt = val;
    }

    public static Node create(Graph g, IDatatype val) {
        return new NodeImpl(g, val);
    }

    public String toString() {
        return getValue().toSparql();
    }

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
        if (graph == null) {
            return dt;
        } else {
            return graph.getValue(this);
        }
    }

    @Override
    public boolean isBlank() {
        // TODO Auto-generated method stub
        return getValue().isBlank();
    }

    @Override
    public boolean isConstant() {
        // TODO Auto-generated method stub
        return !getValue().isBlank();
    }

    @Override
    public boolean isVariable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean same(Node node) {
        // TODO Auto-generated method stub
        return getValue().sameTerm((IDatatype) node.getValue());
    }

    public boolean equals(Object obj) {
        if (obj instanceof Node) {
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
        if (dt != null){
            return dt.getObject();
        }
        return null;
    }

    public void setObject(Object o) {
        if (dt != null){
             dt.setObject(o);
        }    
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
        return key;
    }

    @Override
    public void setKey(String str) {
        key = str;
    }

    @Override
    public Object getProvenance() {
        return null;    
    }
    
    public void setProvenance(Object obj){
        
    }
}
