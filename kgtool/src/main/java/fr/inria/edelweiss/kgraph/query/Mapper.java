package fr.inria.edelweiss.kgraph.query;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.edelweiss.kgenv.eval.SQLResult;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Pointerable;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implements unnest() statement 
 * bind (unnest(?exp) as ?var)
 * bind (unnest(?exp) as (?x, ?y))
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class Mapper {
    Producer p;
    MapperSQL mapper;
    
    Mapper(Producer p){
        this.p = p;
        mapper = new MapperSQL(p);

    }
    
    
    public Mappings map(List<Node> nodes, Object object) {
        if (object instanceof IDatatype) {
            return map(nodes, (IDatatype) object);
        }  
        else if (object instanceof Mappings) {
            return map(nodes, (Mappings) object);
        } 
        else if (object instanceof Collection){
            return map(nodes, (Collection<IDatatype>)object);
        }
        else if (object instanceof SQLResult) {
            return mapper.sql(nodes, (SQLResult) object);
        }
        return new Mappings();
    }
    
    
    Mappings map(List<Node> nodes, IDatatype dt) {   
        if (dt.isList()) { 
            return map(nodes, dt.getValues());
        } 
        else if (dt.isPointer()){
            return map(nodes, dt.getPointerObject());
        }
        else if (dt.getObject() != null) {
            return map(nodes, dt.getObject());
        } else  {
            return mapDT(nodes, dt);
        }
    }
    
    Mappings map(List<Node> nodes, Pointerable obj) { 
        switch (obj.pointerType()){
            case Pointerable.GRAPH: 
                return map(nodes, (Graph) obj.getGraph());
                
            case Pointerable.MAPPINGS:
                return map(nodes, obj.getMappings());
                
            case Pointerable.ENTITY:
                 return map(nodes, obj.getEntity());               
        }
        
        return map(nodes, (Object) obj);
    }
    
    /**
     * bind (unnest(us:graph()) as (?s, ?p, ?o))
     * bind (unnest(us:graph()) as ?t)
     */
    Mappings map(List<Node> varList, Graph g){
        Node[] qNodes = new Node[varList.size()];
        varList.toArray(qNodes);
        Node[] nodes;
        Mappings map = new Mappings();
        int size = varList.size();
        if (size != 1 && size != 3){
            return map;
        }
        for (Entity ent : g.getEdges()){
            nodes = new Node[size];
            if (size == 3){
                nodes[0] = ent.getNode(0);
                nodes[1] = ent.getEdge().getEdgeNode();
                nodes[2] = ent.getNode(1);
            }
            else {
                nodes[0] = DatatypeMap.createObject(ent);
            }
            map.add(Mapping.create(qNodes, nodes));           
        }
        
        return map;

    }
    
    Mappings map(List<Node> varList, Entity e) {
        Mappings map = new Mappings();
        int size = varList.size();
        if (size != 3) {
            return map;
        }
        
        Node[] qNodes = new Node[varList.size()];
        Node[] nodes = new Node[qNodes.length];
        varList.toArray(qNodes);

        nodes[0] = e.getNode(0);
        nodes[1] = e.getEdge().getEdgeNode();
        nodes[2] = e.getNode(1);
        map.add(Mapping.create(qNodes, nodes));

        return map;
    }
    
    

    Mappings map(List<Node> lNodes, Collection<IDatatype> list) {
        Mappings map = new Mappings();
        for (IDatatype dt : list){
            Mapping m =  Mapping.create(lNodes.get(0), dt);
            map.add(m);
        }
        return map;
    }
    
    
    /**
     * bind (unnest(exp) as ?x)
     * bind (unnest(exp) as (?x, ?y))
     * eval(exp) = list
     * 
     * 
     */
    Mappings map(List<Node> varList, List<IDatatype> valueList) {
        Node[] qNodes = new Node[varList.size()];
        varList.toArray(qNodes);
        Mappings map = new Mappings();
        Mapping m;
        for (IDatatype dt : valueList){
            if (dt.isList() && varList.size() > 1){ 
                // bind (((1 2)(3 4)) as (?x, ?y))
                // ?x = 1 ; ?y = 2
                 Node[] val = new Node[dt.size()];
                 m = Mapping.create(qNodes, dt.getValues().toArray(val));
            }
            else {
                // bind (((1 2)(3 4)) as ?x)
                // HINT: bind (((1 2)(3 4)) as (?x))
                // ?x = (1 2)
                 m =  Mapping.create(varList.get(0), dt);
            }
           
            map.add(m);
        }
        return map;
    }
    
    Mappings map(List<Node> lNodes, IDatatype[] list) {
        Mappings map = new Mappings();
        for (IDatatype dt : list){
            Mapping m =  Mapping.create(lNodes.get(0), dt);
            map.add(m);
        }
        return map;
    }

    Mappings map(List<Node> lNodes, Mappings map) {
        map.setNodes(lNodes);
        return map;
    }

    Mappings mapDT(List<Node> varList, IDatatype dt) {
        Node[] qNodes = new Node[varList.size()];
        varList.toArray(qNodes);
        Mappings lMap = new Mappings();
        List<Node> lNode = toNodeList(dt);
        for (Node node : lNode) {
            Node[] tNodes = new Node[1];
            tNodes[0] = node;
            Mapping map = Mapping.create(qNodes, tNodes);
            lMap.add(map);
        }
        return lMap;
    }
    


    public List<Node> toNodeList(Object obj) {
        IDatatype dt = (IDatatype) obj;
        List<Node> list = new ArrayList<Node>();
        if (dt.isList()) {
            for (IDatatype dd : dt.getValues()) {
                if (dd.isXMLLiteral() && dd.getLabel().startsWith("http://")) {
                    // try an URI
                    dd = DatatypeMap.newResource(dd.getLabel());

                }
                list.add(p.getNode(dd));
            }
        } else {
            list.add(p.getNode(dt));
        }
        return list;
    }

}
