package fr.inria.corese.core.edge.rule;

import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.kgram.api.core.Node;

/**
 * Edge entailed by a Rule
 * index and provenance
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class EdgeRuleGraph extends EdgeRule {
    private Node graph;
     
    public EdgeRuleGraph(Node source, Node pred, Node subject, Node object){
        super(source,  pred, subject, object);
        setGraph(source);
    }
                
    @Override
    public Node getGraph(){
        return graph;
    }

    @Override
    public void setGraph(Node graph) {
        this.graph = graph;
    }

}
