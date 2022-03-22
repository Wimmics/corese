package fr.inria.corese.core.query;

import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Query;

/**
 * Draft check if query may succeed on graph PRAGMA: 
 * no RDFS entailments, simple RDF match
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class QueryCheck {
    
    Graph graph;
    
    
    public QueryCheck(Graph g) {
        graph = g;
    }
    
    public boolean check(Query q) {
        return check(q, q.getBody());
    }
        
    boolean check(Query q, Exp exp) {
        switch (exp.type()) {

            case ExpType.EDGE:
                Edge edge = exp.getEdge();
                Node pred = edge.getEdgeNode();
                Node var = edge.getEdgeVariable();

                if (var == null) {

                    if (graph.getPropertyNode(pred) == null) {
                        // graph does not contain this property: fail now
                        return false;
                    } else if (graph.isType(pred)) {
                        Node value = edge.getNode(1);
                        // ?c a owl:TransitiveProperty
                        if (value.isConstant()) {
                            if (graph.getNode(value) == null) {
                                return false;
                            }
                        } else if (q.getBindingNodes().contains(value) && q.getValues().getMappings() != null) {
                            // ?c a ?t with bindings
                            for (Mapping map : q.getValues().getMappings()) {

                                Node node = map.getNode(value);
                                if (node != null && graph.getNode(node) != null) {
                                    // graph  contain node
                                    return true;
                                }
                            }
                            return false;
                        }
                    }
                } else if (q.getBindingNodes().contains(var) && q.getValues().getMappings() != null) {
                    // property variable with bindings: check the bindings
                    for (Mapping map : q.getValues().getMappings()) {

                        Node node = map.getNode(var);
                        if (node != null && graph.getPropertyNode(node) != null) {
                            // graph  contain a property
                            return true;
                        }
                    }

                    return false;
                }

                break;

            case ExpType.UNION:

                for (Exp ee : exp.getExpList()) {
                    if (check(q, ee)) {
                        return true;
                    }
                }
                return false;

            case ExpType.AND:
            case ExpType.GRAPH:

                for (Exp ee : exp.getExpList()) {
                    boolean b = check(q, ee);
                    if (!b) {
                        return false;
                    }
                }
        }

        return true;
    }

    

}
