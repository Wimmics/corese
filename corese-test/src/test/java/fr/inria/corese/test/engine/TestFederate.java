package fr.inria.corese.test.engine;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.shacl.Shacl;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.VariableScope;
import fr.inria.corese.sparql.api.IDatatype;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class TestFederate {
    
    @BeforeClass
    public static void before() {
        try {
            // import SHACL Interpreter
            new Shacl(Graph.create()).eval();
        } catch (EngineException ex) {
            Logger.getLogger(TestFederate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    

    Mappings process(QueryProcess exec, String q) throws EngineException {
        //System.out.println(q);
        return exec.query(q);
    }
    
   
       
       @Test
    public void testshape5() throws EngineException {
        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

           String i = "insert data { "
                   + "us:John us:location <http://fr.dbpedia.org/resource/France>, 'France' . "
                   + "us:John foaf:name 'Jean'@fr ; foaf:knows us:Jack . "
                   + "us:Jack us:location <http://dbpedia.org/resource/Nice> ; foaf:name 'Jack' . "
                   
                   + "us:shape a sh:NodeShape ;"
                   + "sh:targetSubjectsOf us:location ;"
                   + "sh:property ["
                   + "sh:path (foaf:name [<http://fr.dbpedia.org/sparql> "
                   + "( [sh:inversePath rdfs:label] <http://purl.org/dc/terms/subject> '*' )] "
                   + "[xsh:filter([sh:nodeKind sh:Literal])]"
                   + ");"
                   + "sh:datatype rdf:langString"
                   + "]"
                   + "}";
        
         String q =                   
                   "select ?r where { "
                + "bind (us:test() as ?t) "
                + "bind (sh:shacl() as ?g) "
               // + "bind (jsh:sh_shacl() as ?g) "
               //+ "bind (xt:print(xt:turtle(?g)) as ?p) "
                + "graph ?g { [] sh:result ?r }"
                + "}  "
                + "function us:test() {set(traceService=false)}";
                       
        exec.query(i);
        Mappings map = exec.query(q);
        //System.out.println(map);
        assertEquals(4, map.size());
    }
    
    //@Test
    public void testshape4() throws EngineException {
        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        String i = "insert data { "
                + "us:John us:location <http://fr.dbpedia.org/resource/France>, 'France' . "
                + "us:John foaf:name 'Jean'@fr ; foaf:knows us:Jack . "
                + "us:Jack us:location <http://dbpedia.org/resource/Nice> ; foaf:name 'Jack' . "
                
                 + "us:shape a sh:NodeShape ;"
                 + "sh:targetSubjectsOf us:location ;"
                 + "sh:property ["
                 + "sh:path ("
                 + "us:location [xsh:filter([sh:nodeKind sh:IRI])]"
                 + "[sh:alternativePath ("
                 + "("
                 + "[xsh:filter([sh:pattern 'http://fr.dbpedia.org/resource'])]"               
                 + "[<http://fr.dbpedia.org/sparql> (rdf:type  rdfs:label) ]  )"
                 + "("
                 + "[xsh:filter([sh:pattern 'http://dbpedia.org/resource'])]"               
                 + "[<http://dbpedia.org/sparql> (rdf:type rdfs:label)]  )"
                 + ") ] );"
                 + "sh:datatype rdf:langString "
                 + "]"
                
                + "}" ;
        
         String q = 
                "select ?r where { "
                + "bind (us:test() as ?t)"
                + "bind (sh:shacl() as ?g) "
                + "bind (xt:print(xt:turtle(?g)) as ?p) "
                + "graph ?g { [] sh:result ?r }"
                + "}  "
                + "function us:test() {sh:trace(false); set(traceService=false)}";
                       
        exec.query(i);
        Mappings map = exec.query(q);
        //System.out.println(map);
        assertEquals(true, map.size() >0);
    }
    
    
    
    
     @Test
    public void testshape3() throws EngineException {
        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        String i = "insert data { "
                + "us:John us:location <http://fr.dbpedia.org/resource/France>, 'France' . "
                + "us:John foaf:name 'Jean'@fr ; foaf:knows us:Jack . "
                + "us:Jack us:location <http://dbpedia.org/resource/Nice> ; foaf:name 'Jack' . "
                
                + "us:shape a sh:NodeShape ;"
                + "sh:targetSubjectsOf us:location ;"
                + "sh:property ["
                + "sh:path (us:location [xsh:filter([sh:nodeKind sh:IRI])]"
                + "[xsh:filter([sh:or ("
                + "[sh:not [sh:pattern 'http://fr.dbpedia.org/resource']]"
                + "[sh:hasValue <http://fr.dbpedia.org/resource/Francee>] "
               // + "[sh:minLength 100] "
                + "[sh:not [sh:minLength 100]] "
                + ")])]"
                + "[<http://fr.dbpedia.org/sparql> (rdf:type rdfs:label)]  "
                + ");"
                + "sh:datatype rdf:langString "
                + "]"
                + "}" ;
        
         String q = 
                "select ?g ?r where { "
                + "bind (us:test() as ?t)"
                + "bind (sh:shacl() as ?g) "
               // + "bind (xt:print(xt:turtle(?g)) as ?p) "
                + "graph ?g { [] sh:result ?r }"
                + "}  "
                + "function us:test() {set(traceService=false)}";
                       
        exec.query(i);
        Mappings map = exec.query(q);
//        IDatatype dt = (IDatatype) map.getValue("?g");
//        Graph g = (Graph) dt.getPointerObject();
//        System.out.println(Transformer.turtle(g));
        assertEquals(1, map.size());
    }

    
    
      @Test
    public void testshape2() throws EngineException {
        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);
             
        String i =  "insert data { "
                + "us:John us:location <http://fr.dbpedia.org/resource/France> ; rdfs:label 'Augustin'@fr "
                + "us:Jack us:location <http://fr.dbpedia.org/resource/Nice> "
                
                + "us:shape a sh:NodeShape ;"
                + "sh:targetSubjectsOf us:location ;"
                               
                + "sh:property ["
                + "sh:path (us:location [<http://fr.dbpedia.org/sparql>  (rdf:type  ) ] ) ;"
                + "sh:hasValue  dbo:Country ;"
                + "sh:node ["
                + "sh:property ["
                + "sh:path ( [<http://fr.dbpedia.org/sparql> (rdfs:label ) ] ) ;"
                + "sh:datatype  rdf:langString " 
                + "]"
                + "]"
                + "] ;"
                
                + "sh:property ["
                + "sh:path (rdfs:label [<http://fr.dbpedia.org/sparql>  ( [sh:inversePath rdfs:label ]  ) ] ) ;"
                + "sh:nodeKind sh:IRI "
                + "]"
                
                + "}";
        
        String q = "select ?r where { "
                + "bind (sh:shacl() as ?g) "
               // + "bind (xt:print(xt:turtle(?g)) as ?p) "
                + "graph ?g { [] sh:result ?r }"
                + "}  ";
        
        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(4, map.size());
    }
    
    
    
      //@Test
    public void testshape1() throws EngineException {
        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);
        String i = "insert data { "
                + "us:John us:location <http://fr.dbpedia.org/resource/France> "
                + "us:Jack us:location <http://fr.dbpedia.org/resource/Nice> "
                
                + "us:shape a sh:NodeShape ;"
                + "sh:targetSubjectsOf us:location ;"
                + "sh:property ["
                + "sh:path (us:location [sh:alternativePath ("
                + "[xsh:service (<http://corese.inria.fr/sparql> rdf:type ) ]"
                + "[xsh:service (<http://fr.dbpedia.org/sparql>  rdf:type ) ]"
                + " ) ] );"
                + "sh:hasValue dbo:Country ;"
                + "sh:pattern 'http://' ;"
                + "sh:minCount 10"
                + "]"
                
                + "}";
        
        String q = 
                "select ?r where { "
                //+ "bind (sh:trace(true) as ?tt)"
                + "bind (sh:shacl() as ?g) "
               // + "bind (xt:print(xt:turtle(?g)) as ?p) "
                + "graph ?g { [] sh:result ?r }"
                + "}  ";
        
        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(3, map.size());
    }
    
    
    
    
    
    
    @Test
    public void process9() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        
        String q = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "select * where {"
                + "filter (?t = 12) "
                + "filter exists { "
                + "{?x h:name ?n filter exists { values ?v { 1 }  ?x h:name ?nn } } union { graph ?g { ?a ?p ?b } ?x h:age ?aa } filter (?x = ?y) "
                + "optional { select * where {?x h:age ?a bind (1 as ?z) }}  minus { ?x h:name ?m } "
                + "}"
                + "}";

        Query qq = exec.compile(q);
        ASTQuery ast = exec.getAST(qq);
        Exp exp = ast.getBody();
       
        VariableScope scope1 = VariableScope.subscope().setFilter(true).setExist(true);
        VariableScope scope2 = VariableScope.inscope().setFilter(true).setExist(true);
        VariableScope scope3 = VariableScope.allscope().setFilter(true).setExist(true);
               
        assertEquals(0, exp.getSubscopeVariables().size());
        assertEquals(0, exp.getInscopeVariables().size());
        assertEquals(0, exp.getAllVariables().size());
        assertEquals(12, exp.getFilterVariables().size());
        
        assertEquals(3, exp.getVariables(scope1).size());
        assertEquals(12, exp.getVariables(scope2).size());
        assertEquals(13, exp.getVariables(scope3).size());                     
    }
    
    @Test
    public void process8() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "@trace @show @debug "
               // + "@variable "
                + "@type  kg:exist "
            + "@federate "
                + "<http://fr.dbpedia.org/sparql?limit=20> "
                + "<http://corese.inria.fr/sparql> "
                + "select * "
                + "where { "
                + "?x h:name ?n   "
                + "filter ( not exists { ?x h:age ?a } ) "
                + "bind (strlang(?n, 'fr') as ?m)"
                + "optional {?y rdfs:label ?m optional { ?y foaf:isPrimaryTopicOf ?c }}"
                + "} "
                + "limit 20";
        Query qq = exec.compile(q);
        //System.out.println(qq.getAST());
        
        Mappings map = process(exec, q);
        //System.out.println(map);
        Assert.assertEquals(9, map.size());
    }
    
    
  
    @Test
    public void process7() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q1 = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "@federate  <http://fr.dbpedia.org/sparql>  "
                + "<http://corese.inria.fr/sparql>"
                + "select * "
                + "where { "
                + "?x h:name ?n   "
                + "filter ( not exists { ?x h:age ?a } ) "
                + "bind (strlang(?n, 'fr') as ?m)"
                + " ?y rdfs:label ?m   minus {  ?y foaf:isPrimaryTopicOf ?c }"
                + "}";
        
        String q2 = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "@federate  <http://fr.dbpedia.org/sparql>  "
                + "<http://corese.inria.fr/sparql>"
                + "select * "
                + "where { "
                + "?x h:name ?n, ?c   "
                + "filter ( not exists { ?x h:age ?a } ) "
                + "bind (strlang(?n, 'fr') as ?m)"
                + " ?y rdfs:label ?m   minus {  ?y foaf:isPrimaryTopicOf ?c }"
                + "}";


        Query qq1 = exec.compile(q1);
        Assert.assertEquals(2, exec.getAST(qq1).getBody().size());
        Query qq2 = exec.compile(q2);
        Assert.assertEquals(1, exec.getAST(qq2).getBody().size()); 
        
    }
    
    @Test
    public void process6() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q1 = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "@federate  <http://fr.dbpedia.org/sparql>  "
                + "<http://corese.inria.fr/sparql>"
                + "select * "
                + "where { "
                + "?x h:name ?n   "
                + "filter ( not exists { ?x h:age ?a } ) "
                + "bind (strlang(?n, 'fr') as ?m)"
                + " ?y rdfs:label ?m   optional { ?y foaf:name ?m . ?y foaf:isPrimaryTopicOf ?c }"
                + "}";
        
         String q2 = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "@federate  <http://fr.dbpedia.org/sparql>  "
                + "<http://corese.inria.fr/sparql>"
                + "select * "
                + "where { "
                + "?x h:name ?n, ?c   "
                + "filter ( not exists { ?x h:age ?a } ) "
                + "bind (strlang(?n, 'fr') as ?m)"
                + " ?y rdfs:label ?m   optional { ?y foaf:name ?m . ?y foaf:isPrimaryTopicOf ?c }"
                + "}";

        Query qq1 = exec.compile(q1);
        Assert.assertEquals(2, exec.getAST(qq1).getBody().size());        
        Query qq2 = exec.compile(q2);
        Assert.assertEquals(1, exec.getAST(qq2).getBody().size());     
    }
    
    
      @Test
    public void process5() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);               
        String q1 = "prefix h: <http://www.inria.fr/2015/humans#>"
                 + "@federation <http://example.org>  <http://corese.inria.fr/sparql> <http://fr.dbpedia.org/sparql> "
                 + "select * where {"
                 + "?x h:name ?n "
                 + "filter (?x != ?z || ?n != ?a)"
                 + "?z h:age ?a  "
                 + "}";
         
        String q2 = "prefix h: <http://www.inria.fr/2015/humans#>"
                 + "@federation <http://example.org>  <http://corese.inria.fr/sparql> <http://fr.dbpedia.org/sparql> "
                 + "select * where {"
                 + "?x h:name ?n "
                 + "?z h:age ?a  "
                 + "}";
        
        Query qq1 = exec.compile(q1);
        Assert.assertEquals(1, exec.getAST(qq1).getBody().size());
        Query qq2 = exec.compile(q2);
        Assert.assertEquals(2, exec.getAST(qq2).getBody().size());
        
    }
    
 
    
    
    
    
  
    
    
    @Test
    public void process1() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "@federate <http://fr.dbpedia.org/sparql> "
                + "<http://corese.inria.fr/sparql>"
                + "select * "
                + "where { "
                + "?x h:name ?n "
                + "filter not exists {?x h:age ?a} "
                + "optional {?y rdfs:label ?n optional { ?y foaf:isPrimaryTopicOf ?c }}"
                + "}";

        Mappings map = process(exec, q);
       Assert.assertEquals(9, map.size());
    }
    
    @Test
    public void process2() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "@federate <http://fr.dbpedia.org/sparql> "
                + "<http://corese.inria.fr/sparql>"
                + "select * "
                + "where { "
                + "?x h:name ?n filter not exists {?x h:age ?a} "
                + "?y rdfs:label ?n optional { ?y foaf:isPrimaryTopicOf ?c }"
                + "}";

        Mappings map = process(exec, q);
        Assert.assertEquals(2, map.size());
    }
    
    
    @Test
    public void process3() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "@federate <http://dbpedia.org/sparql>  <http://fr.dbpedia.org/sparql>"
                + "select * "
                + "from  named <http://dbpedia.org> "
                + "from  named <http://fr.dbpedia.org>"
                + "where {"
                + "  graph ?g {?x rdfs:label \"Antibes\"@fr ; a ?t}"
                + "}";

        Mappings map = process(exec, q);
        Assert.assertEquals(43, map.size());
    }
    
    
     @Test
    public void process4() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "@federate <http://dbpedia.org/sparql>  <http://fr.dbpedia.org/sparql>"
                + "select * "
                + "from   <http://dbpedia.org> "
                + "from   <http://fr.dbpedia.org>"
                + "where {"
                + "  ?x rdfs:label \"Antibes\"@fr ; a ?t"
                + "}";

        Mappings map = process(exec, q);
        Assert.assertEquals(43, map.size());
    }
    
    
     @Test
    public void testmserv() throws LoadException,  EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q =
                "@trace @federate "
              //  + "<http://fr.dbpedia.org/sparql> "
                        + " <https://dbpedia.org/sparql> "
                        + "select distinct ?l where { "
                        + "?x rdfs:label 'Paris'@fr, ?l "
                        + "filter langMatches(lang(?l), 'en') "
                        + "}"
                        + "order by ?l";

        Mappings map = process(exec, q);
        assertEquals(1, map.size());
    }


    
      
    public void testServAnnot() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String qq = "select "
                + "(us:foo() as ?f)"
                + "(us:bar() as ?b)"
                + "where {}"
                + "@federate <http://fr.dbpedia.org/sparql>"
                + "package {"
                + "@federate <http://dbpedia.org/sparql>"
                + "function us:foo(){"
                + "let (?g = construct  where {?x rdfs:label ?l} limit 10)"
                + "{?g}}"
                + "function us:bar(){"
                + "let (?m = select *  where {?x rdfs:label ?l} limit 10)"
                + "{?m}}"
                + "}";

        Mappings map = process(exec, qq);
        IDatatype dg = (IDatatype) map.getValue("?f");
        IDatatype dm = (IDatatype) map.getValue("?b");
        Graph gg = (Graph) dg.getPointerObject();
        Mappings mm = (Mappings) dm.getPointerObject();
        assertEquals(10, gg.size());
        assertEquals(10, mm.size());
    }
    
       @Test
    public void testService() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q1 = "@federate <http://fr.dbpedia.org/sparql>"
                + "select * where {?x ?p ?y } limit 10";
        Mappings m1 = process(exec, q1);
        assertEquals(10, m1.size());

        String q2 = "@federate <http://fr.dbpedia.org/sparql>"
                + "construct where {?x ?p ?y } limit 10";
        Mappings m2 = process(exec, q2);
        Graph g2 = (Graph) m2.getGraph();
        assertEquals(10, g2.size());
    }
    
     @Test
    public void testSparqlInriaAccess() throws EngineException {
        String query = "prefix h: <http://www.inria.fr/2015/humans#>\n" +
                "@federate <http://corese.inria.fr/sparql>  \n" +
                "select  * {\n" +
                " ?x h:name ?n \n" +
                "}";
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(query);
        assertEquals("Result", 16, map.size());

    }
    
//     @Test
//    public void testService2() throws EngineException, LoadException {
//        Graph g = Graph.create();
//        QueryProcess exec = QueryProcess.create(g);
//        String q = "select * where {"
//
//                + "values (?s ?l) { "
//                + "(<http://dbpedia.org/sparql>     'Antibes'@en  )"
//                + "(<http://fr.dbpedia.org/sparql>  'Antibes'@fr  )"
//                + "} "
//
//                + " { ?x rdfs:label ?l } union "
//                + "{ "
//                + "service ?s {"
//                + "select * where {?x rdfs:label ?l} limit 1 "
//                + "}"
//                + "} "
//                + "}";
//
//        Mappings map = exec.query(q);
//        //System.out.println(map);
//        assertEquals(2, map.size());
//        Node s1 = map.get(0).getNode("?s");
//        assertEquals("http://dbpedia.org/sparql", s1.getLabel());
//        Node s2 = map.get(1).getNode("?s");
//        assertEquals("http://fr.dbpedia.org/sparql", s2.getLabel());
//    }

    
}
