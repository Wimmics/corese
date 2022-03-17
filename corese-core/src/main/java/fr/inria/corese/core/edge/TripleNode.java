package fr.inria.corese.core.edge;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.NodeImpl;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.api.IDatatype;

/**
 * Node that is a Triple 
 * Can be used as Node in the graph
 * Can be subject/object of an Edge
 * @todo: function getIndex() is the same here for Node and Edge ->
 * rename to getEdgeIndex() or getNodeIndex()
 * It is not a pb because it is mostly used as Node
 */
public class TripleNode extends NodeImpl implements Edge 
{
    private Node subject;
    private Node predicate;
    private Node object;
    
    public TripleNode(Node s, Node p, Node o) {
        setSubjectNode(s);
        setPropertyNode(p);
        setObjectNode(o);
    }
    
    @Override
    public String toString() {
        return String.format("%s %s %s", pretty(getSubjectNode()), getPropertyNode(), pretty(getObjectNode()));
    }
    
    @Override
    public Node getNode(){
        return this;
    }
    
    @Override
    public Edge getEdge() {
        return this;
    }
    
    @Override
    public boolean isTripleNode() {
        return true;
    }
        
    String pretty(Node n) {
        if (n.isTriple()) {
            return String.format("<<%s>>", n);
        }
        return n.toString();
    }
    
    @Override
    public String getEdgeLabel() {
        return getPropertyNode().getLabel();
    }
    
    public IDatatype createTripleReference() {
        if (getTripleStore() == null) {
            return null;
        }
        return createTripleReference(getTripleStore());    
    }
    
    public IDatatype createTripleReference(Graph g) {
        setDatatypeValue(g.createTripleReference(
                   getSubjectNode(), getPropertyNode(), getObjectNode()));
        return getDatatypeValue();
    }
    
    @Override
    public Graph getTripleStore() {
        return (Graph) getSubjectNode().getTripleStore();
    }
    
     //@Override
    public Node getNode(int n) {
        switch (n) {            
            case 0:
                return getSubjectNode();
            case 1:
                return getObjectNode();            
        }
        return null;
    }
    
    @Override
    public Node getGraph() {
        return null;
    }
    
    //@Override
    public Node getSubjectNode() {
        return subject;
    }

    public void setSubjectNode(Node subject) {
        this.subject = subject;
    }
    
    //@Override
    public Node getProperty() {
        return predicate;
    }

    //@Override
    public Node getPropertyNode() {
        return predicate;
    }

    public void setPropertyNode(Node predicate) {
        this.predicate = predicate;
    }

    //@Override
    public Node getObjectNode() {
        return object;
    }

    public void setObjectNode(Node object) {
        this.object = object;
    }

}
