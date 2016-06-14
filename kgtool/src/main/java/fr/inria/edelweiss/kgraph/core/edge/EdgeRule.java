package fr.inria.edelweiss.kgraph.core.edge;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.edelweiss.kgram.api.core.Node;

/**
 * Edge entailed by a Rule
 * index and provenance
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class EdgeRule extends EdgeQuad {
    static int pcount = 0;
    int index = -1;
    Object prov;
    
//    EdgeRule(Node pred, Node subject, Node object){
//        super(pred, subject, object);
//    }
    
     EdgeRule(Node source, Node pred, Node subject, Node object){
        super(source, pred, subject, object);
    }
    
//    public static EdgeRule create(Node subject, Node pred, Node object) {
//        return new EdgeRule(pred, subject, object);
//    }
    
    public static EdgeRule create(Node source, Node subject, Node pred, Node object) {
        return new EdgeRule(source, pred, subject, object);
    }
    
    @Override
    public EdgeRule copy(){
        return create(graph, subject, predicate, object);
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
    
//    @Override
//    public Node getGraph(){
//        return ((Graph) subject.getGraphStore()).getRuleGraphNode();
//    }

}
