package fr.inria.corese.core.edge.rule;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Graph;

/**
 * Edge entailed by a Rule
 * index and provenance
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class EdgeRuleSubclass extends EdgeRuleTop {

    EdgeRuleSubclass(Node subject, Node object) {
        super(subject, object);
    }
    
    public static EdgeRuleSubclass create(Node source, Node subject, Node predicate, Node object){
        return new EdgeRuleSubclass(subject, object);
    }

    @Override
    public Node getEdgeNode() {
        return subject.getTripleStore().getNode(Graph.SUBCLASS_INDEX);
    }
}
