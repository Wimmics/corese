package fr.inria.corese.core.edge;

import fr.inria.corese.sparql.cg.datatype.DatatypeMap;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Graph;

/**
 * Edge entailed by a Rule
 * index and provenance
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public abstract class EdgeRuleTop extends EdgeBinary {
    static int pcount = 0;
    int index = -1;
    Object prov;
     
    EdgeRuleTop (Node subject, Node object){
      super(subject, object);
  }
    
    @Override
    public int getIndex(){
        return index;
    }
    
    @Override
    public void setIndex(int i){
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
