/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.edelweiss.kgraph.query;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeHierarchy;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgenv.parser.NodeImpl;
import fr.inria.edelweiss.kgram.api.core.DatatypeValue;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Given a graph with a class hierarchy, emulate method inheritance
 * return the class list of an rdf term
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class ClassHierarchy extends DatatypeHierarchy {

      
    Graph graph, fake;
    QueryProcess exec;
    
    ClassHierarchy(Graph g){
        graph = g;
        exec = QueryProcess.create(g);
        init(g);
    }
    
    void init(Graph g){
        g.setClassDistance();
    }

    /**
    * value is a node in graph
    * return the list of class and superclass of value
    * if value have no type or is a literal, delegate to DatatypeHierarchy
    * TODO: store the list in HashMap
    */
    @Override
    public List<String> getSuperTypes(DatatypeValue value) {
//        List<String> list = superTypes(value.stringValue());
//        if (list != null){
//            // already in cache
//            return list;
//        }
        List<String> list = getSuperTypes((IDatatype) value);
        if (list.isEmpty()){
            return super.getSuperTypes(value);
        }
        if (isDebug()){
            System.out.println("CH: " + value + " " + list);
        }
        // store in cache
        //defSuperTypes(value.stringValue(), list);
        return list;
    }
      
    void defSuperTypes(String name, List<String> list){
        for (String sup : list){
            defSuperType(name, sup);
        }
    }
    
    /**
     * Compute class list with a query
     * more precise class first
     */
    List<String> getSuperTypes(IDatatype val) {
        String query = 
                "select (aggregate(?c) as ?list) where { "
                + "select distinct ?c ?x where {"
                + "?x rdf:type/rdfs:subClassOf* ?c "
                + "} order by desc(kg:depth(?c))"
                + "}";
        try {
            Mapping m = getMapping("?x", val);
            Mappings map = exec.query(query, m);
            IDatatype dt = (IDatatype) map.getValue("?list");
            return getList(dt);
        } catch (EngineException ex) {
            Logger.getLogger(ClassHierarchy.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<String>(0);
    }
    
    List<String> getList(IDatatype dt){
        ArrayList<String> list = new ArrayList<String>();
        for (IDatatype e : dt.getValueList()){
            list.add(e.stringValue());
        }
        return list;
    }
    
    Mapping getMapping(String var, IDatatype dt){
        Mapping map = Mapping.create(NodeImpl.createVariable(var), getNode(dt));
        return map;
    }
    
    Node getNode(IDatatype dt) {
        Node n = graph.getNode(dt, false, false);
        if (n == null) {
            if (fake == null){
                fake = Graph.create();
            }
            n = fake.getNode(dt, true, true);
        }
        return n;
    }
    
    

}
