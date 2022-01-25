 package fr.inria.corese.test.research;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.load.SPARQLJSONResult;
import fr.inria.corese.core.load.SPARQLResult;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.util.Property;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 */
public class RDFStar {
    
    static final String data  = "/user/corby/home/AADemoNew/rdf-star-main/tests/";
    static final String rdfs  = "http://www.w3.org/2000/01/rdf-schema#";
    static final String mf    = "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#";
    static final String qt    = "http://www.w3.org/2001/sw/DataAccess/tests/test-query#";
    static final String ut    = "http://www.w3.org/2009/sparql/tests/test-update#";
    boolean trace = false;
    
    public static void main(String[] args) throws LoadException {
        new RDFStar().process();
    }
    
    void process() throws LoadException {
        Property.set(Property.Value.RDF_STAR, true);
        Property.set(Property.Value.RDF_STAR_VALIDATION, true);
        Property.set(Property.Value.LOAD_IN_DEFAULT_GRAPH, true);
        
        for (IDatatype dt : RDFStar.this.manifest()) {
            RDFStar.this.manifest(dt.getLabel());
        }
    }
    
    // main manifest
    void manifest(String name) throws LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(name);
        g.init();
        for (Edge ee : g.getEdges(mf + "entries")) {
            manifest(g, name, g.getDatatypeList(ee.getObjectNode()));
        }
    }
    
    // manifest name
    void manifest(Graph g, String name, List<IDatatype> testList) {
        System.out.println(name);
        if (trace) System.out.println("process: " + testList);
        
        for (IDatatype dt : testList) {
            test(g, dt);
        }
    }
    
    String getValue(Graph g, String predicate, String subject) {
        Edge edge = g.getEdge(predicate, subject, 0);
        if (edge == null) {
            return null;
        }
        return edge.getObjectNode().getLabel();
    }
    
    /**
     * test: subject uri of a test 
     */
    void test(Graph gg, IDatatype test) {
        System.out.println("test: " + test);
        Edge eaction= gg.getEdge(mf + "action", test.getLabel(), 0);
        Edge eresult= gg.getEdge(mf + "result", test.getLabel(), 0);
        Edge etype  = gg.getEdge(NSManager.RDF+"type", test.getLabel(), 0);        
        String type = etype ==null ? "undefined" : etype.getObjectNode().getLabel();
        String result = null; 
        String comment = getValue(gg, rdfs+"comment", test.getLabel());
        if (comment!=null) {
            System.out.println("comment: " + comment);
        }
        
        if (eresult !=null) {
            Node nresult = eresult.getObjectNode();
            IDatatype dt = eresult.getObjectValue();
            if (nresult.isBlank()) {
                eresult= gg.getEdge(ut + "data", nresult, 0);
                if (eresult!=null) {
                    result = eresult.getObjectNode().getLabel();
                }
            }
            else if (dt.isBoolean()) {
            
            }
            else {
                result = nresult.getLabel();
            }
        }
                
        if (eaction != null) {
            Node node = eaction.getObjectNode();
            if (trace) System.out.println("action: " + node.getLabel());
            if (trace) if (result!=null)System.out.println("result: " + result);
            
            if (node.isBlank()) {
                // [qt:query sparql ; qt:data rdf]
                Edge equery   = gg.getEdge(qt + "query", node, 0);
                Edge erequest = gg.getEdge(qt + "request", node, 0);
                if (erequest == null) {
                    erequest = gg.getEdge(ut + "request", node, 0);
                }
                Edge edata    = gg.getEdge(qt + "data",  node, 0);
                if (edata == null) {
                    edata = gg.getEdge(ut + "data", node, 0);
                }
                
                boolean isQuery = equery!=null;
                boolean isRequest = erequest!=null;
                
                if ((isQuery || isRequest )&& edata!=null) {
                    if (trace) if (isQuery)   System.out.println("query: " + equery.getObjectNode().getLabel());
                    if (trace) if (isRequest) System.out.println("request: " + erequest.getObjectNode().getLabel());
                    if (trace) System.out.println("data: " + edata.getObjectNode().getLabel());
 
                    query(edata.getObjectNode().getLabel(), 
                            isQuery?
                             equery.getObjectNode().getLabel():
                             erequest.getObjectNode().getLabel()       ,
                            result, type, isQuery);
                }
                else {
                    if (trace) System.out.println("query: " + equery);
                    if (trace) System.out.println("data: " + edata);
                }
            }
            else {
                action(node.getLabel(), result, type);
            }
        }
        else {
            if (trace) System.out.println("action: " + eaction);
        }
    }
    
    void load(Load ld, String name) throws LoadException, EngineException {
        if (name.endsWith(".rq") || name.endsWith(".ru")) {
            String q = QueryLoad.create().readWE(name);
            QueryProcess exec = QueryProcess.create(Graph.create());
            exec.compile(q);
        }
        else if (name.endsWith(".trig")) {
            ld.parse(name, Load.TURTLE_FORMAT,Load.TURTLE_FORMAT);
        }
        else {
            ld.parse(name);
        }
    }
    
    void query(String rdf, String query, String result, String type, boolean isQuery) {       
        Graph g = Graph.create();
        Load ld = Load.create(g);
        try {
            load(ld, rdf);
//            if (type.contains("*** Negative test not detected")) {
//                System.out.println(g.display());
//            }
        } catch (LoadException ex) {
            System.out.println("syntax error: " + rdf);
            System.out.println(type);
            System.out.println(ex.getMessage());
            return;
        } catch (EngineException ex) {
            System.out.println("syntax error: " + rdf);
            System.out.println(type);
            System.out.println(ex.getMessage());
            return;
        }
        
        QueryLoad ql = QueryLoad.create();
        try {
            String q = ql.readWE(query);
            System.out.println("query:\n"+q);
            if (type.contains("Negative")) System.out.println(type);
            QueryProcess exec = QueryProcess.create(g);
            Mappings map = exec.query(q);
            Mappings map2 = result(result);            
            genericompare(g, map, map2);            
        } catch (LoadException ex) {
            Logger.getLogger(RDFStar.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EngineException ex) {
            Logger.getLogger(RDFStar.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RDFStar.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(RDFStar.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(RDFStar.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    void genericompare(Graph g, Mappings m1, Mappings m2) {
        if (m2.getGraph() != null) {
            if (m1.getGraph() == null) {
                compare(g, (Graph) m2.getGraph());
            }
            else {
                compare((Graph) m1.getGraph(), (Graph) m2.getGraph());
            }
        } else {
           compare(m1, m2);
        }
    }
    
    void compare(Mappings m1, Mappings m2) {
        display(m1, m2);
    }
    
    void compare(Graph g, Graph r) {
        System.out.println("corese:");
        display(g);
        System.out.println("rdf star:");
        display(r);
    }

    void display(Mappings m1, Mappings m2) {
        System.out.println("corese:");
        display(m1);

        System.out.println("rdf star:");
        display(m2);
    }
    
    
    void display(Mappings map) {
        if (map.getGraph()==null) {
            System.out.println(map);
        }
        else {
            display((Graph)map.getGraph());
        }
    }
    
    void display(Graph g) {
        System.out.println("size: " + g.size());
        System.out.println(g.display());
    }

    Mappings result(String name) throws IOException, ParserConfigurationException, SAXException, LoadException, EngineException {
        Mappings map = null;
        // DRAFT for nquad
//        if (name.endsWith(".nq")) {
//            name.replace(".nq", ".trig");
//        }
        
        if (name.endsWith(".srj")) {
            SPARQLJSONResult json = SPARQLJSONResult.create();
            map = json.parse(name);

        } else if (name.endsWith(".srx")) {
            SPARQLResult xml = SPARQLResult.create();
            map = xml.parse(name);
        } else if (name.endsWith(".ttl") || name.endsWith(".trig")) {
            Graph g = Graph.create();
            Load ld = Load.create(g);
            load(ld, name);
            map = new Mappings();
            map.setGraph(g);
        }
        return map;
    }
    
    void action(String name, String result, String type) {        
        Graph g = Graph.create();
        Load ld = Load.create(g);
        try {
            load(ld, name);
            if (type.contains("Negative")) { 
                System.out.println(type);
                if (result == null) {
                    System.out.println(g.display());
                }
            }
        } catch (LoadException | EngineException ex) {
            System.out.println("syntax error detected: "+ name);
            System.out.println(type);
            if (!type.contains("Negative")){
                System.out.println(ex.getMessage());
            }
            return;
        } 
        
        if (result != null) {
            Graph gres = Graph.create();
            Load ldr = Load.create(gres);
            try {
                ldr.parse(result);
                compare(g, gres);
            } catch (LoadException ex) {
                Logger.getLogger(RDFStar.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    

    
    List<IDatatype> manifest() throws LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(data + "manifest.ttl");
        for (Edge ee : g.getEdges(mf + "include")) {
            return g.getDatatypeList(ee.getObjectNode().getDatatypeValue());
        }
        return new ArrayList<>();
    }
    
}
