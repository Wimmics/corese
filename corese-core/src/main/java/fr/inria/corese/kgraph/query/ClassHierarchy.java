package fr.inria.corese.kgraph.query;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeHierarchy;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgenv.parser.NodeImpl;
import fr.inria.edelweiss.kgram.api.core.DatatypeValue;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
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

     static final String queryObject = 
                "select (aggregate(?c) as ?list) where { "
                + "select distinct ?c ?x where {"
                + "?x rdf:type/rdfs:subClassOf* ?c "
                + "} order by desc(kg:depth(?c))"
                + "}";
     
     static final String queryClass = 
                "select (aggregate(?c) as ?list) where { "
                + "select distinct ?c ?x where {"
                + "?x rdfs:subClassOf* ?c "
                + "} order by desc(kg:depth(?c))"
                + "}";
     
    static final String X = "?x";
    static final String LIST = "?list";
     
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
    * object is a node in graph, type may be its type or null
    * return the list of class and superclass of object/type
    * if value have no type or is a literal, delegate to DatatypeHierarchy
    * TODO: store the list in HashMap
    */
    @Override
    public List<String> getSuperTypes(DatatypeValue object, DatatypeValue type) {
        List<String> list;
        if (type == null){
            list = getSuperTypes((IDatatype) object, queryObject);
        }
        else {
            list = getSuperTypes((IDatatype) type, queryClass);
        }
        if (list.isEmpty()){
            return super.getSuperTypes(object, type);
        }
        if (isDebug()){
            System.out.println("CH: " + object + " " + list);
        }       
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
    List<String> getSuperTypes(IDatatype val, String query) {       
        try {
            Mapping m = getMapping(X, val);
            Mappings map = exec.query(query, m);
            IDatatype dt = (IDatatype) map.getValue(LIST);
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
