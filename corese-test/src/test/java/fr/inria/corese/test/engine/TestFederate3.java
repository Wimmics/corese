package fr.inria.corese.test.engine;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.URLParam;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.xml.parsers.ParserConfigurationException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Service Report with @report or mode=report 
 */
public class TestFederate3 implements URLParam {
    
            @Test
    public void test6() throws EngineException, MalformedURLException, LoadException, ParserConfigurationException, SAXException, IOException {        
                String q = "prefix h: <http://www.inria.fr/2015/humans#>"
                        //+ "@report  "
                        + "select * "
                        + "where {"
                        + "service <http://corese.inria.fr/sparql?mode=report> {"
                        + "    ?s h:name ?n"
                        + "}"
                        + "bind (strlang(?n, \"en\") as ?name)"
                        + "service <http://fr.dbpedia.org/sparql?mode=report> {"
                        + "    ?uri rdfs:label ?n"
                        + "}"
                        + "}";
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);       
        test(map.getValue(Binding.SERVICE_REPORT_ZERO));
        test(map.getValue(Binding.SERVICE_REPORT_ONE));

    }
    
    void test(IDatatype dt) {
        assertEquals(true, dt!=null);
        assertEquals(true, dt.get(SERVER_NAME)!=null);
        assertEquals(true, dt.get(URL)!=null);
        assertEquals(true, dt.get(STATUS)!=null);
        //System.out.println(dt.pretty());
    }
    
        @Test
    public void test5() throws EngineException, MalformedURLException, LoadException {        
        String q = "@report  "
                + "select *  "
                + "where {"
                + "service <http://dbpedia.org/sparql> {"
                + "select * where {"
                + "?s rdfs:label ?o"
                + "} limit 1"
                + "}"
                + "}";
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        //System.out.println("res: "+ map.toString()); 
        IDatatype dt = map.getValue(Binding.SERVICE_REPORT_ZERO);
        assertEquals(true, dt!=null);
        assertEquals(true, dt.get(SERVER_NAME)!=null);
        assertEquals(true, dt.get(URL)!=null);
        //assertEquals(true, dt.get(MES)!=null);
        assertEquals(true, dt.get(STATUS)!=null);
        assertEquals(true, dt.get(LOCATION)!=null);
        //System.out.println(dt.pretty());
    }
    
       @Test
    public void test4() throws EngineException, MalformedURLException, LoadException {        
        String q = "@report  "
                + "select *  "
                + "where {"
                + "service <http://fr.dbpedia.org/sparql> {"
                + "select * where {"
                + "?s rdfs:label ?o"
                + "} limit 1"
                + "}"
                + "}";
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        //System.out.println("res: "+ map.toString()); 
        IDatatype dt = map.getValue(Binding.SERVICE_REPORT_ZERO);
        assertEquals(true, dt!=null);
        assertEquals(true, dt.get(SERVER_NAME)!=null);
        assertEquals(true, dt.get(URL)!=null);
        //assertEquals(true, dt.get(MES)!=null);
        assertEquals(true, dt.get(STATUS)!=null);
        //System.out.println(dt.pretty());
    }
    
       @Test
    public void test3() throws EngineException, MalformedURLException, LoadException {        
        String q = "@report  "
                + "select *  "
                + "where {"
                + "service <http://fr.dbpedia.org/sparql> {"
                + "select * where {"
                + "?s rdfs:seeAlso ?o"
                + "} limit 1"
                + "}"
                + "}";
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
       // System.out.println("res: "+ map.toString()); 
        IDatatype dt = map.getValue(Binding.SERVICE_REPORT_ZERO);
        assertEquals(true, dt==null);
//        assertEquals(true, dt.get(SERVER_NAME)!=null);
//        assertEquals(true, dt.get(URL)!=null);
//        //assertEquals(true, dt.get(MES)!=null);
//        assertEquals(true, dt.get(STATUS)!=null);
//        System.out.println(dt.pretty());
    }
    
      @Test
    public void test2() throws EngineException, MalformedURLException, LoadException {        
        String q = "@report empty "
                + "select *  "
                + "where {"
                + "service <http://fr.dbpedia.org/sparql> {"
                + "select * where {"
                + "?s rdfs:seeAlso ?o"
                + "} limit 1"
                + "}"
                + "}";
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        //System.out.println("res: "+ map.toString()); 
        IDatatype dt = map.getValue(Binding.SERVICE_REPORT_ZERO);
        assertEquals(true, dt!=null);
        assertEquals(true, dt.get(SERVER_NAME)!=null);
        assertEquals(true, dt.get(URL)!=null);
        //assertEquals(true, dt.get(MES)!=null);
        assertEquals(true, dt.get(STATUS)!=null);
       // System.out.println(dt.pretty());
    }
    
     @Test
    public void test1() throws EngineException, MalformedURLException, LoadException {        
        String q = "@report "
                + "select *  "
                + "where {"
                + "service <http://fr.dbpedia.org/sparql> {"
                + "select * where {"
                + "?s rdfs:seeAlso+ ?o"
                + "} limit 1"
                + "}"
                + "}";
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        //System.out.println(map.toString()); 
        IDatatype dt = map.getValue(Binding.SERVICE_REPORT_ZERO);
        assertEquals(true, dt!=null);
        assertEquals(true, dt.get(SERVER_NAME)!=null);
        assertEquals(true, dt.get(URL)!=null);
        assertEquals(true, dt.get(MES)!=null);
        assertEquals(true, dt.get(STATUS)!=null);
        //System.out.println(dt.pretty());
    }
    
    
}
