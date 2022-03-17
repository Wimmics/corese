package fr.inria.corese.core.edge.rule;

import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.edge.EdgeTriple;

/**
 * Edge entailed by a Rule
 * index and provenance
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class EdgeRule extends EdgeTriple {
    static int pcount = 0;
    int index = -1;
    Object prov;
     
     EdgeRule(Node source, Node pred, Node subject, Node object){
        super(pred, subject, object);
    }
    
    public static EdgeRule create(Node source, Node subject, Node pred, Node object) {
        return new EdgeRule(source, pred, subject, object);
    }
      
    @Override
    public int getEdgeIndex(){
        return index;
    }
    
    @Override
    public void setEdgeIndex(int i){
        index = i;
    }
    
     @Override
    public Object getProvenance() {
        if (prov != null && ! (prov instanceof Node)) {
            prov = DatatypeMap.createObject("p" + pcount++, prov);
        }
        return prov;
    }
    
    @Override    
    public void setProvenance(Object obj) {         
        prov = obj;
    }
    
    @Override
    public Node getGraph(){
        return subject.getTripleStore().getNode(Graph.RULE_INDEX);
    }

}
