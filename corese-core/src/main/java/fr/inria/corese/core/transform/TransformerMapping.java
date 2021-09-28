package fr.inria.corese.core.transform;

import fr.inria.corese.compiler.parser.NodeImpl;
import fr.inria.corese.core.Graph;
import static fr.inria.corese.core.transform.Transformer.IN;
import static fr.inria.corese.core.transform.Transformer.IN2;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class TransformerMapping {
    
    Mapping mapping;
    Graph graph, fake;
    
    TransformerMapping(Graph g) {
        graph = g;
        fake = Graph.create();
    }

     /**
     * @return the mapping
     */
    public Mapping getMapping() {
        return mapping;
    }

    /**
     * @param mapping the mapping to set
     */
    public void setMapping(Mapping mapping) {
        this.mapping = mapping;
    }
    
     Mapping getMapping(Query q, IDatatype[] ldt, IDatatype dt) {         
        if (ldt != null && q != null && !q.getArgList().isEmpty()) {
            return getMapping(q, ldt);
        }
        else {
            return getMappingCache(q, dt);
        } 
    }
    
    Mapping getMappingCache(Query q, IDatatype dt){
        if (q == null){
            return getSimpleMapping(dt);
        }
        else {
            Mapping map = q.getMapping();
            if (map == null || map.getNodes().length != 1){
                map = getMapping(q, dt);
                q.setMapping(map);
            }
            else {                
                map.setNode(getNode(dt), 0);
            }

            return map;
        }
    }
    
    Mapping getSimpleMapping(IDatatype dt){
        Mapping map = getMapping();
        if (map == null){
            map = Mapping.create(NodeImpl.createVariable(IN), getNode(dt));
            setMapping(map);
        }
        else {
            //map.getNodes()[0] = getNode(dt);
            map.setNode(getNode(dt), 0);
        }
        return map;
    }
    
    
    Mapping getMapping(Query q, IDatatype dt){
        Node qn = NodeImpl.createVariable(getArg(q, 0));
        Node n = getNode(dt);
        return Mapping.create(qn, n);
    }
    

    Mapping getMapping(Query q, IDatatype[] values) {
        Mapping map = q.getMapping();
        if (map == null || map.getNodes().length != values.length){
            map = getMapping(q.getArgList(), values);
            q.setMapping(map);
        }
        else {
            // reuse template Mapping
            for (int i = 0; i<values.length; i++){
                map.setNode(getNode(values[i]), i);
            }
        }
        
        return map;
    }
    
    
    /**
     * args:   template arguments
     * values: call-template arguments
     */  
    Mapping getMapping(List<Node> args, IDatatype[] values){
        int size = Math.min(values.length, args.size());
        Node[] qn = new Node[size];
        Node[] tn = new Node[size];
        for (int i = 0; i<size; i++){
           qn[i] = args.get(i); 
           tn[i] = getNode(values[i]);
        }
        return Mapping.create(qn, tn);
    }
    
    String getArg(Query q, int n){
        if (q == null || q.getArgList().isEmpty()){
            return getArg(n);
        }
        List<Node> list = q.getArgList();
        if (n < list.size()){
           return list.get(n).getLabel();
        }
        else {
           return getArg(n);
        }
 }
    
    String getArg(int n){
        switch (n){
            case 0:  return IN;
            default: return IN2;
        }
    }

    Node getNode(IDatatype dt) {
        Node n = graph.getNode(dt, false, false);
        if (n == null) {
            //n = fake.getNode(dt, true, true);
            return dt;
        }
        return n;
    }
    

}
