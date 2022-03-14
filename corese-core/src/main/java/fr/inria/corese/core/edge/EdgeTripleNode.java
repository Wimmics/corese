package fr.inria.corese.core.edge;

import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.api.IDatatype;

/**
 * Edge(g, t) where t = s p o
 * and t : Node as triple reference
 * s p o {| q v |}
 * ::=
 * Edge(g, t = (s p o))  Edge(g, a = (t q v))
 */
public class EdgeTripleNode extends EdgeTop {

    private Node graph;
    private TripleNode triple;
    
    public EdgeTripleNode(Node g, TripleNode t) {
        setGraph(g);
        setTriple(t);
    }
    
    public EdgeTripleNode(Node g, Node s, Node p, Node o) {
        setGraph(g);
        setTriple(new TripleNode(s, p, o));
    }
    
    public EdgeTripleNode copy(Node graphNode) {
        return new EdgeTripleNode(graphNode, getTripleNode());
    }
    
    // TripleNode as Triple reference node
    public IDatatype createTripleReference() {
        return getTriple().createTripleReference();
    }
    
    public IDatatype createTripleReference(Graph g) {
        return getTriple().createTripleReference(g);
    }
    
    @Override
    public boolean isTripleNode() {
        return true;
    }
    
    @Override
    public String toString() {
        return String.format(isNested()?"%s <<%s>> [%s]":"%s %s [%s]", 
                getGraph(), getTriple(), getTriple().getDatatypeValue().getLabel());
    }    

    @Override
    public Node getGraph() {
        return graph;
    }
        
    @Override
    public void setGraph(Node graph) {
        this.graph = graph;
    }
    
    /**
     * for sparql query processing there are 3 nodes: s o t
     */
    @Override
    public int nbNode() {
        return 3;
    }
    
    /**
     * For graph index processing there are 2 nodes: s o
     */
    @Override
    public int nbNodeIndex() {
        return 2;
    }
    
    @Override
    public Node getNode(int n) {
        switch (n) {
            case Graph.IGRAPH:
                return getGraph();
            case 0:
                return getTriple().getSubjectNode();
            case 1:
                return getTriple().getObjectNode();
            case 2:
                return getTripleNode();
        }
        return null;
    }
    
    @Override
    public Node getEdgeNode() {
        return getTriple().getPropertyNode();
    }

    @Override
    public void setEdgeNode(Node pred) {
        getTriple().setPropertyNode(pred);
    }
        
    
    @Override
    public TripleNode getTripleNode() {
        return triple;
    }
    
    @Override
    public void setTripleNode(Node node) {
        if (node instanceof TripleNode) {
            setTriple((TripleNode) node);
        }
    }
    
    public TripleNode getTriple() {
        return triple;
    }

    public void setTriple(TripleNode triple) {
        this.triple = triple;
    }
    
}
