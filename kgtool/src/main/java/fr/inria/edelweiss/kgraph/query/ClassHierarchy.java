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
    }

    /**
    * value is a node in graph
    * return the list of class and superclass of value
    * if value have no type or is a literal, delegate to DatatypeHierarchy
    * TODO: store the list in HashMap
    */
    @Override
    public List<String> getSuperTypes(DatatypeValue value) {
        List<String> list = getSuperTypes((IDatatype) value);
        if (list.isEmpty()){
            return super.getSuperTypes(value);
        }
        return list;
    }
    
    /**
     * Compute type list with a query
     */
    List<String> getSuperTypes(IDatatype val) {
        String query = "select (aggregate(distinct ?c) as ?list) where { ?x rdf:type/rdfs:subClassOf* ?c }";
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
