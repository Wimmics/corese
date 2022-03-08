package fr.inria.corese.core.edge;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.NodeImpl;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.api.IDatatype;

/**
 * Node that is a Triple 
 * Can be used as Node in the graph
 * Can be subject/object of an Edge
 */
public class TripleNode extends NodeImpl {
    private Node subject;
    private Node predicate;
    private Node object;
    
    public TripleNode(Node s, Node p, Node o) {
        setSubject(s);
        setPredicate(p);
        setObject(o);
    }
    
    @Override
    public String toString() {
        return String.format("%s %s %s", getSubject(), getPredicate(), getObject());
    }
    
    public IDatatype createTripleReference() {
        if (getTripleStore() == null) {
            return null;
        }
        return createTripleReference(getTripleStore());    
    }
    
    public IDatatype createTripleReference(Graph g) {
        setDatatypeValue(g.createTripleReference(
                   getSubject(), getPredicate(), getObject()));
        return getDatatypeValue();
    }
    
    @Override
    public Graph getTripleStore() {
        return (Graph) getSubject().getTripleStore();
    }
    
    public Node getSubject() {
        return subject;
    }

    public void setSubject(Node subject) {
        this.subject = subject;
    }

    public Node getPredicate() {
        return predicate;
    }

    public void setPredicate(Node predicate) {
        this.predicate = predicate;
    }

    public Node getObject() {
        return object;
    }

    public void setObject(Node object) {
        this.object = object;
    }

}
