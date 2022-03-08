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
    
    // TripleNode as Triple reference node
    public IDatatype createTripleReference() {
        return getTriple().createTripleReference();
    }
    
    public IDatatype createTripleReference(Graph g) {
        return getTriple().createTripleReference(g);
    }

    @Override
    public Node getGraph() {
        return graph;
    }
        
    @Override
    public void setGraph(Node graph) {
        this.graph = graph;
    }
    

    @Override
    public Node getNode(int n) {
        switch (n) {
            case Graph.IGRAPH:
                return getGraph();
            case 0:
                return getTriple().getSubject();
            case 1:
                return getTriple().getObject();
        }
        return null;
    }
    
    @Override
    public Node getEdgeNode() {
        return getTriple().getPredicate();
    }

    @Override
    public void setEdgeNode(Node pred) {
        getTriple().setPredicate(pred);
    }
    
    
    
    
    

    public TripleNode getTriple() {
        return triple;
    }

    public void setTriple(TripleNode triple) {
        this.triple = triple;
    }
    
}
