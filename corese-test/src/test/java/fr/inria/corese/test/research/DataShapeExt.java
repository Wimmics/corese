package fr.inria.corese.test.research;

import fr.inria.corese.compiler.parser.NodeImpl;
import java.io.IOException;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.shacl.Shacl;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.kgram.api.core.Edge;
import org.junit.Test;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;

import static org.junit.Assert.*;

/**
 *
 * @author corby
 */
public class DataShapeExt {

    private static final String NBRESULT = NSManager.SHACL + "result";

    static String data = Thread.currentThread().getContextClassLoader().getResource("data/").getPath();

    class MyShacl extends Shacl {

        MyShacl(Graph g) {
            super(g);
            //init(g);
        }

        void init(Graph g) {
            RuleEngine re = RuleEngine.create(g);
            re.setProfile(RuleEngine.OWL_RL);
            try {
                re.process();
            } catch (EngineException ex) {
                Logger.getLogger(DataShapeExt.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    
    Graph myinit() throws EngineException, LoadException, IOException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.parse(data + "test/human1.rdf");
        ld.parse(data + "test/human2.rdf");
        ld.parse(data + "test/human.rdfs");
        return g;
    }
    
               @Test
    public void testtfun6() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(tfun6());
        Shacl shacl = new Shacl(g); 
        Graph gg = shacl.eval();  
        System.out.println(Transformer.turtle(gg));
        gg.init();
        assertEquals(2, shacl.nbAbstractResult(gg));
    }
    
    String tfun6() {
        String i = "insert data {"
                + "[] sh:booleanDetail true ."
                + "us:test a sh:NodeShape ;"
                + "sh:targetSubjectsOf foaf:knows ;"
                + "sh:property ["
                + "sh:path (foaf:knows foaf:name) ;"
                + "sh:or ([sh:nodeKind sh:Literal][sh:nodeKind sh:BlankNode]) ;"
                + "sh:minCount 1;"
                + "sm:message();"
                + "sr:result() "
                + "]"
                + "."
                
                + "us:John foaf:name 'John' ; foaf:knows us:Jim ."
                + "us:Jim foaf:name us:Jim, 'Jim' ; foaf:knows 'Jack'  "
                + "}"
                
                + "@public function sr:result(report, detail, url, sh, oper, s, p, o, exp) {"
                + "sh:merge(report, detail, url)"
                + "}"
                
               
                ;
        return i;
     }
    
             @Test
    public void testtfun5() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(tfun5());
        Shacl shacl = new Shacl(g); 
        Graph gg = shacl.eval();  
        System.out.println(Transformer.turtle(gg));
        gg.init();
        assertEquals(2, shacl.nbAbstractResult(gg));
    }
    
    String tfun5() {
        String i = "insert data {"
                + "[] sh:booleanDetail true ."
                + "us:test a sh:NodeShape ;"
                + "sh:targetSubjectsOf foaf:knows ;"
                + "sh:property ["
                + "sh:path (foaf:knows foaf:name) ;"
                + "sh:or([sh:nodeKind sh:Literal][sh:nodeKind sh:BlankNode]) ;"
                + "sh:minCount 1;"
                + "sm:message();"
               // + "sr:result() "
                + "]"
                + "."
                
                + "us:John foaf:name 'John' ; foaf:knows us:Jim ."
                + "us:Jim foaf:name us:Jim, 'Jim' ; foaf:knows 'Jack'  "
                + "}"
                
                + "@public function sr:result(report, detail, url, sh, oper, s, p, o, exp) {"
                + "let (bn = bnode()) {"
                + "sh:store(report, url, us:mydetail, bn) ;"
                + "sh:store(report, bn, us:arg, xt:list(s, o));"
                + ""
                + "}"
                + "}"
                
               
                ;
        return i;
     }
    
    
    
          @Test
    public void testtfun4() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(tfun4());
        Shacl shacl = new Shacl(g); 
        Graph gg = shacl.eval();  
        System.out.println(Transformer.turtle(gg));
        assertEquals(2, shacl.nbResult(gg));
        assertEquals(26, gg.size());
    }
    
    String tfun4() {
        String i = "insert data {"
                + "us:test a sh:NodeShape ;"
                + "sh:targetSubjectsOf foaf:knows ;"
                + "sh:property ["
                + "sh:path (foaf:knows foaf:name) ;"
                + "sh:nodeKind sh:Literal ;"
                + "sh:minCount 1;"
                + "sm:message();"
                + "sr:result() "
                + "]"
                + "."
                
                + "us:John foaf:name 'John' ; foaf:knows us:Jim ."
                + "us:Jim foaf:name us:Jim, 'Jim' ; foaf:knows 'Jack'  "
                + "}"
                
                + "@public function sr:result(report, detail, url, sh, oper, s, p, o, exp) {"
                + "let (bn = bnode()) {"
                + "sh:store(report, url, us:mydetail, bn) ;"
                + "sh:store(report, bn, us:arg, xt:list(s, o));"
                + "}"
                + "}"
                
               
                ;
        return i;
     }
    
    
    
         @Test
    public void testtfun3() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(tfun3());
        Shacl shacl = new Shacl(g); 
        Graph gg = shacl.eval();  
        System.out.println(Transformer.turtle(gg));
        assertEquals(2, shacl.nbResult(gg));
    }
    
    String tfun3() {
        String i = "insert data {"
                + "us:test a sh:NodeShape ;"
                + "sh:targetSubjectsOf foaf:knows ;"
                + "sh:property ["
                + "sh:path (foaf:knows foaf:name) ;"
                + "sh:nodeKind sh:Literal ;"
                + "sh:minCount 1;"
                //+ "sh:message 'person with name:';"
               // + "sh:messageFunction[ us:mess(foaf:name) ]"
                + "sm:message(); "
                //+ "sm:mess()"
              //  + "xsh:display(true)"
                + "]"
                + "."
                
                + "us:John foaf:name 'John' ; foaf:knows us:Jim ."
                + "us:Jim foaf:name us:Jim, 'Jim' ; foaf:knows 'Jack'  "

                + "}"
                
                + "@public function sx:path(source, node, exp) {"
                + "let ((path) = exp) {"
                + "sh:pathfinder(path, node)"
                + "}"
                + "}"
                 
                + "@public function sm:mess(shape, node, value, exp) {"
                + "xt:turtle(node)"
                + "}"
                ;
        return i;
     }
    
    
    
        @Test
    public void testtfun2() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(tfun2());
        Shacl shacl = new Shacl(g); 
        Graph gg = shacl.eval();  
        //System.out.println(Transformer.turtle(gg));
        assertEquals(2, shacl.nbResult(gg));
    }
    
    String tfun2() {
        String i = "insert data {"
                + "us:test a sh:NodeShape ;"
                + "sh:targetSubjectsOf foaf:knows ;"
                + "sh:property ["
                + "sh:path [sx:path((foaf:knows foaf:name))] ;"
                + "sh:nodeKind sh:Literal ;"
                + "sh:minCount 1;"
                //+ "sh:message 'person with name:';"
               // + "sh:messageFunction[ us:mess(foaf:name) ]"
                + "sm:message((foaf:knows foaf:name))"
              //  + "xsh:display(true)"
                + "]"
                + "."
                
                + "us:John foaf:name 'John' ; foaf:knows 'Jim' ."
                + "us:Jim foaf:knows 'Jack'  "

                + "}"
                
                + "@public function sx:path(source, node, exp) {"
                + "let ((path) = exp) {"
                + "sh:pathfinder(path, node)"
                + "}"
                + "}";
        return i;
     }
    
    
       @Test
    public void testtfun() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(tfun());
        Shacl shacl = new Shacl(g); 
        Graph gg = shacl.eval();        
        System.out.println(Transformer.turtle(gg));
        Edge edge = gg.getEdge(NSManager.SHACL+"resultMessage2");
        assertEquals(true, edge != null);
        IDatatype dt = (IDatatype) edge.getNode(1).getDatatypeValue();
        assertEquals(true, dt.isList());
    }
    
    String tfun() {
        String i = "insert data {"
                + "us:test a sh:NodeShape ;"
                + "sh:targetFunction [ us:target(foaf:Person foaf:knows) ] ;"
                + "sh:property ["
                + "sh:path foaf:knows ;"
                + "sh:nodeKind sh:IRI ;"
                //+ "sh:message 'person with name:';"
               // + "sh:messageFunction[ us:mess(foaf:name) ]"
                + "sm:message(foaf:name)"
              //  + "xsh:display(true)"
                + "]"
                + "."
                
                + "us:John a foaf:Person ; foaf:name 'John' ; foaf:knows 'Jim' ."
                + "us:Jim foaf:knows 'Jack'  "

                + "}"
                
                + "@public "
                + "function us:mess(shape, source, node, exp) {"
                + "let ((path) = exp, "
                + "     list = sh:pathfinder(path, source),"
                + "     (val) = list) {"
                + "return (list)"
                + "}"
                + "}"
                
                + "@public "
                + "function us:target(exp) {"
                + "let ((type pred) = exp) {"
                + "let (select type pred (aggregate(distinct ?x) as ?list)"
                + "where {"
                + "?x a ?type ; ?pred ?val"
                + "}) {"
                + "return(list)"
                + "}"
                + "}"
                + "}" 
                
                ;
        return i;
     }
    
    
    // xsh:function evaluate path and evaluate shape on result of path
      @Test
    public void testeval() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(peval());
        Shacl shacl = new Shacl(g); 
        Graph gg = shacl.eval();        
        System.out.println(Transformer.turtle(gg));
        assertEquals(true, shacl.conform(gg));
    }
    
    
    String peval() {
        String i = "insert data {"
                + "us:test a sh:NodeShape ;"
                + "sh:targetNode us:John ;"
                + "sh:property ["
                + "sh:path foaf:knows ;"
                + "sx:path (foaf:name [sh:datatype xsd:string]) "
                + "]"
                + "."
                
                + "us:John foaf:knows us:Jim "
                + "us:Jim foaf:name 'Jim' "

                + "}"
                
                // function evaluate path and evaluate shape on result of path
                + "@public "
                + "function sx:path(source, node, exp) {"
                + "let ((path shape) = exp,"
                + "     list = sh:pathfinder(path, node),"
                + "     (value) = list,"
                + "     res = sh:eval(shape, value)) {"
                + "xt:print('us:test', source, node, value);"
                + "res"
                + "}"
                + "}"
                
                
                ;
        return i;
    }
    
    
      // xsh:pathFunction with path target node list
      // xsh:nodeFunction with path target nodes one by one
       @Test
    public void testpathfun() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(pf());
        Shacl shacl = new Shacl(g); 
        Graph gg = shacl.eval();        
        System.out.println(Transformer.turtle(gg));
        assertEquals(true, shacl.conform(gg));
    }
    
    
    String pf() {
        String i = "insert data {"
                + "us:test a sh:NodeShape ;"
                + "sh:targetNode us:John ;"
                + "sh:property ["
                + "sh:path foaf:knows ;"
                + "xsh:pathFunction [ us:path(true) ] ;"
                + "xsh:nodeFunction [ us:path(false) ]"
                + "]"
                + "."
                
                + "us:John foaf:knows us:Jim "

                + "}"
                
                
                + "@public "
                + "function us:path(source, node, exp) {"
                + "let ((islist) = exp) {"
                + "xt:print('us:test', source, node);"
                + "if (islist, xt:isList(node), !xt:isList(node))"
                + "}"
                + "}"
                
                
                ;
        return i;
    }
    
    
    
      
       @Test
    public void testfun1() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(qq1());
        Shacl shacl = new Shacl(g); 
        //shacl.setTrace(true);
        Graph gg = shacl.eval();
        //assertEquals(2, shacl.nbResult(gg));
        
       
        System.out.println(Transformer.turtle(gg));
        //assertEquals(2, shacl.nbResult(gg));
    }
    
      String qq1() {
        String i = "insert data {"
                + "graph us:g1 { us:Person rdfs:subClassOf us:Human }"
                + "graph us:g2 { us:John a us:Person us:Person rdfs:subClassOf us:Animal}"
                
                + "[] a sh:NodeShape ;"
                + "xsh:targetNode us:John ;"
                + "xsh:failure (true)"
                + "}";
        return i;
    }
    
    
    
       @Test
    public void testfrom1() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(q1());
        Shacl shacl = new Shacl(g); Graph gg = shacl.eval();
        assertEquals(2, shacl.nbResult(gg));
        
        g = Graph.create();        
        exec = QueryProcess.create(g);
        exec.query(q2());
        shacl = new Shacl(g);  gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        assertEquals(2, shacl.nbResult(gg));
    }
    
      String q1() {
        String i = "insert data {"
                + "graph us:g1 { us:Person rdfs:subClassOf us:Human }"
                + "graph us:g2 { us:John a us:Person us:Person rdfs:subClassOf us:Animal}"
                
                + "[] a sh:NodeShape ;"
                + "xsh:targetNode us:John ;"
                + "sh:property ["
                + "sh:path ([xsh:from (us:g1 us:g2 )] "
                + "rdf:type rdfs:subClassOf);"
                + "xsh:function[xsh:failure()]"
                + "]"
                + "}";
        return i;
    }
    
    
     String q2() {
        String i = "insert data {"
                + "graph us:g1 { us:Person rdfs:subClassOf us:Human  }"
                + "graph us:g2 { us:John a us:Person . us:Person rdfs:subClassOf us:Animal "
                + "us:Person owl:equivalentClass us:Human}"
                
                + "[] a sh:NodeShape ;"
                + "xsh:targetNode us:John ;"
                + "sh:property ["
                + "sh:path ([xsh:from (us:g1 us:g2 )] "
                + "[xsh:triplePath xsh:subject] [xsh:triplePath (xsh:subject [] us:Human)] [xsh:nodePath xsh:predicate]"
                + ");"
                + "xsh:function[xsh:failure()]"
                + "]"
                + "}";
        return i;
    }
    
    Graph myshacl(String i, String q) throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        exec.query(q);
        Shacl shacl = new Shacl(g); 
        Graph gg = shacl.eval();    
        return gg;
    }  
    
     @Test
    public void testpp1() throws EngineException, LoadException, IOException, TransformerException {
        Shacl shacl = new Shacl(Graph.create());
        
         Graph gg = myshacl(gg(), qp1());
         assertEquals(7, shacl.nbResult(gg));

         gg = myshacl(gg(), qp2());
         assertEquals(5, shacl.nbResult(gg));

         gg = myshacl(gg(), qp3());
         assertEquals(3, shacl.nbResult(gg));

         gg = myshacl(gg(), qp4());
         assertEquals(6, shacl.nbResult(gg));

         gg = myshacl(gg(), qp5());
         assertEquals(3, shacl.nbResult(gg));

         gg = myshacl(gg(), qp6());
         assertEquals(3, shacl.nbResult(gg));

         gg = myshacl(gg(), qp7());
         assertEquals(3, shacl.nbResult(gg));

         gg = myshacl(gg(), qp8());
         assertEquals(0, shacl.nbResult(gg));

         gg = myshacl(gg(), qp9());
         assertEquals(3, shacl.nbResult(gg));

         gg = myshacl(gg(), qp10());
         assertEquals(5, shacl.nbResult(gg));
         //System.out.println(Transformer.turtle(gg));

    }
    
    String qp10() {
        String i = "insert data {"
                + "[] a sh:NodeShape ;"
                + "xsh:targetTriplesOf foaf:knows ;"
                + "sh:property ["
                + "sh:path ("
                + "[xsh:triplePath ( [](xsh:preceding xsh:predicate) (xsh:source xsh:object))]"
                + ");"
               // + "xsh:display(true);"
                + "xsh:failure()"
                + "]"
                + "}";
        return i;
    }
    
        String qp9() {
        String i = "insert data {"               
                + "[] a sh:NodeShape ;"
                + "xsh:targetTriplesOf foaf:knows ;"
                + "sh:property ["
                + "sh:path ("
                + "[xsh:triplePath (us:Jack [][] (xsh:preceding xsh:graph))]"
                + ");"
               // + "xsh:display(true);"
                + "xsh:failure()"
                + "]"
                + "}";
        return i;
    }
    
       String qp8() {
        String i = "insert data {"               
                + "[] a sh:NodeShape ;"
                + "xsh:targetTriplesOf foaf:knows ;"
                + "sh:property ["
                + "sh:path ("
                + "[xsh:triplePath (us:James (xsh:preceding xsh:predicate))]"
                + ");"
               // + "xsh:display(true);"
                + "xsh:failure()"
                + "]"
                + "}";
        return i;
    }
    
       String qp7() {
        String i = "insert data {"               
                + "[] a sh:NodeShape ;"
                + "xsh:targetTriplesOf foaf:knows ;"
                + "sh:property ["
                + "sh:path ("
                + "[xsh:triplePath ((xsh:preceding xsh:predicate))]"
                + "[xsh:triplePath ([] rdfs:member (xsh:preceding xsh:graph))]"
                + ");"
                //+ "xsh:display(true);"
                + "xsh:failure()"
                + "]"
                + "}";
        return i;
    }
    
     String qp6() {
        String i = "insert data {"               
                + "[] a sh:NodeShape ;"
                + "xsh:targetTriplesOf foaf:knows ;"
                + "sh:property ["
                + "sh:path ([xsh:triplePath ([] rdfs:member (xsh:preceding xsh:graph))]);"
                //+ "xsh:display(true);"
                + "xsh:failure()"
                + "]"
                + "}";
        return i;
    }
    
    String qp5() {
        String i = "insert data {"               
                + "[] a sh:NodeShape ;"
                + "xsh:targetTriplesOf foaf:knows ;"
                + "sh:property ["
                + "sh:path ([xsh:triplePath ((xsh:preceding xsh:predicate))]);"
                //+ "xsh:display(true);"
                + "xsh:failure()"
                + "]"
                + "}";
        return i;
    }
    
     String qp4() {
        String i = "insert data {"               
                + "[] a sh:NodeShape ;"
                + "xsh:targetTriplesOf foaf:knows ;"
                + "sh:property ["
                + "sh:path ([xsh:triplePath ([] (xsh:preceding xsh:predicate)  us:Jim)]);"
               // + "xsh:display(true);"
                + "xsh:failure()"
                + "]"
                + "}";
        return i;
    }
    
 
    
      String qp3() {
        String i = "insert data {"               
                + "[] a sh:NodeShape ;"
                + "xsh:targetTriplesOf foaf:knows ;"
                + "sh:property ["
                + "sh:path ([xsh:triplePath ((xsh:preceding xsh:subject) [] us:Jim)]);"
               // + "xsh:display(true);"
                + "xsh:failure()"
                + "]"
                + "}";
        return i;
    }
    
     String qp2() {
        String i = "insert data {"               
                + "[] a sh:NodeShape ;"
                + "xsh:targetTriplesOf foaf:knows ;"
                + "sh:property ["
                + "sh:path ([xsh:triplePath ((xsh:preceding xsh:subject) (xsh:preceding xsh:predicate))]);"
                //+ "xsh:display(true);"
                + "xsh:failure()"
                + "]"
                + "}";
        return i;
    }
    
     String qp1() {
        String i = "insert data {"               
                + "[] a sh:NodeShape ;"
                + "xsh:targetTriplesOf foaf:knows ;"
                + "sh:property ["
                + "sh:path ([xsh:triplePath ((xsh:preceding xsh:subject))]);"
                + "xsh:failure()]"
                + "}";
        return i;
    }
     
     String gg() {
        String i = "insert data {"
                + "us:John foaf:knows us:Jack, us:Jim "
                + "us:Jack foaf:knows us:Jim "
                + "foaf:knows rdfs:domain foaf:Person "
                + "foaf:knownBy owl:inverseOf foaf:nows "                
                + "kg:default a rdf:Graph "
                + "us:John rdfs:member kg:default "
                + "}";
        return i;
    }
    
    
  
    
         //  predicates of the schema used on the predicates of a resource 
    //@Test
    public void testshaclext13() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = init();
        String i = "prefix h: <http://www.inria.fr/2015/humans#> "
                + "prefix i: <http://www.inria.fr/2015/humans-instances#> "
                + "insert data {"                            
                + "[] us:prop us:test "
                + "us:test a sh:NodeShape ;"
                + "xsh:targetNode h:Person ;"
                + "rdfs:label 'test' ;"                             
                + "sh:property ["
                + "sh:path ([sh:oneOrMorePath [xsh:predicatePath xsh:node]] "
                + "[xsh:filter ([sh:patternIn ( rdf: rdfs:) ])]"
                + ");"
                + "sh:display(true)"
                + "]"
                + "}";
        
        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Shacl shacl = new MyShacl(g);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        //assertEquals(2, shacl.nbResult(gg));
    }
    
    
    
        // path on predicate
    @Test
    public void testshaclext12() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        String i = "prefix h: <http://www.inria.fr/2015/humans#> "
                + "prefix i: <http://www.inria.fr/2015/humans-instances#> "
                + "insert data {"                            
                + "[] us:prop us:test "
                + "us:test a sh:NodeShape ;"
                + "xsh:targetClass sh:NodeShape ;"
                + "rdfs:label 'test' ;"                             
                + "sh:property ["
                + "sh:path ([xsh:predicatePath xsh:node] );"
                + "sh:patternIn ( sh: rdf:)"
                + "]"
                + "}";
        
        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Shacl shacl = new MyShacl(g);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        assertEquals(2, shacl.nbResult(gg));
    }
    
    @Test
    public void testshaclext111() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = init();
        String i = "prefix h: <http://www.inria.fr/2015/humans#> "
                + "prefix i: <http://www.inria.fr/2015/humans-instances#> "
                +"insert data {"                              
                + "us:test a sh:NodeShape ;"
                + "xsh:targetTriplesOf rdfs:domain ;"                             
                + "sh:property["
                + "sh:path ("
                + "[xsh:triplePath ([] (xsh:source xsh:subject) )] "
                + "[xsh:nodePath xsh:subject] "
                + "[xsh:notExist (("
                + "[xsh:triplePath (xsh:subject rdf:type )] "
                + "[xsh:nodePath xsh:object]"
                + "[sh:zeroOrMorePath rdfs:subClassOf] "
                + "[xsh:equal (xsh:object)]"
                + "))] "
                + ");"
                + "xsh:failure() ;"
               // + "xsh:display(true)"
                + "]"
                + "}";
        
        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Shacl shacl = new MyShacl(g);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        assertEquals(0, shacl.nbResult(gg));
    }
    
      // path on position
    @Test
    public void testshaclext11() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = init();
              
        String i = "prefix h: <http://www.inria.fr/2015/humans#> "
                + "prefix i: <http://www.inria.fr/2015/humans-instances#> "
                +"insert data {"                              
                + "us:test a sh:NodeShape ;"
                + "xsh:targetTriplesOf rdfs:domain ;"                             
                + "sh:property["
                + "sh:path ("
                + "[xsh:triplePath ([] (xsh:source xsh:subject) )] "
                + "[xsh:notExist (("
                + "[xsh:triplePath ((xsh:preceding xsh:subject) rdf:type (xsh:source xsh:object))] "
                + "))] "
                + ");"
                + "xsh:failure() ;"
                //+ "xsh:display(true)"
                + "]"
                + "}";
        
        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Shacl shacl = new MyShacl(g);
        Graph gg = shacl.eval();
       // System.out.println(Transformer.turtle(gg));
        assertEquals(14, shacl.nbResult(gg));
    }
    
     // path on position
    @Test
    public void testshaclext10() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        String i = "prefix h: <http://www.inria.fr/2015/humans#> "
                + "prefix i: <http://www.inria.fr/2015/humans-instances#> "
                +"insert data {"
                + "us:John a foaf:Person ; foaf:knows us:Jack, us:Jim "
                + "us:Jack a foaf:Person ; foaf:knows us:Jim "
                
                + "us:test a sh:NodeShape ;"
                + "xsh:targetTriplesOf foaf:knows ;"                             
                + "sh:property["
                + "sh:path ("
                + "[xsh:triplePath ((xsh:preceding xsh:object) (xsh:source xsh:predicate)  )]  "
                + "[xsh:triplePath ((xsh:source xsh:subject)   (xsh:source xsh:predicate) (xsh:preceding xsh:object) )]  "
                + ");"
                + "sh:minCount 1"
                + "]"

                + "}";
        
        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Shacl shacl = new MyShacl(g);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        assertEquals(2, shacl.nbResult(gg));
    }
    
    // path on position
    @Test
    public void testshaclext9() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        String i = "prefix h: <http://www.inria.fr/2015/humans#> "
                + "prefix i: <http://www.inria.fr/2015/humans-instances#> "
                +"insert data {"
                + "us:John a foaf:Person ; foaf:knows us:Jack, us:Jim "
                + "us:Jack a foaf:Person ; foaf:knows us:John "
                
                + "us:test a sh:NodeShape ;"
                + "xsh:targetTriplesOf foaf:knows ;"                             
                + "sh:property["
                + "sh:path "
                + "[xsh:triplePath ((xsh:preceding xsh:object) (xsh:source xsh:predicate) (xsh:source xsh:subject) )]  "
                + ";"
                + "sh:minCount 1"
                + "]"

                + "}";
        
        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Shacl shacl = new MyShacl(g);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        assertEquals(1, shacl.nbResult(gg));
    }
    
    
    // path on position
    @Test
    public void testshaclext8() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        String i = "prefix h: <http://www.inria.fr/2015/humans#> "
                + "prefix i: <http://www.inria.fr/2015/humans-instances#> "
                +"insert data {"
                + "us:John a foaf:Person ; foaf:knows us:Jack, us:Jim "
                + "us:Jack a foaf:Person ; foaf:knows us:John "
                
                + "us:test a sh:NodeShape ;"
                + "xsh:targetClass foaf:Person ;"
                + "sh:property ["
                + "sh:path ("
                + "[xsh:triplePath (xsh:subject foaf:knows)] "
                + "[xsh:notExist( [xsh:triplePath ((xsh:preceding xsh:object) (xsh:preceding xsh:predicate) (xsh:preceding xsh:subject) )]) ]  "
                + ");"

                + "xsh:failure()"
                + "]"
                + "}";
        
        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Shacl shacl = new MyShacl(g);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        assertEquals(1, shacl.nbResult(gg));
    }
    
        // path on predicate
    @Test
    public void testshaclext7() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = init();
        String i = "prefix h: <http://www.inria.fr/2015/humans#> "
                + "prefix i: <http://www.inria.fr/2015/humans-instances#> "
                +"insert data {"
                + "us:test a sh:NodeShape ;"
                + "xsh:targetNode i:Laura ;"
                + "sh:property ["
                + "    sh:path ([sh:oneOrMorePath [xsh:triplePath xsh:subject]]"
                + "[xsh:nodePath xsh:predicate]"
                + "[xsh:filter ([sh:pattern h:])]"
                + "[xsh:notExist (rdfs:range)]"
                + ")"
                + ";"

                + "xsh:failure()"
                + "]"
                + "}";
        
        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Shacl shacl = new MyShacl(g);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        assertEquals(6, shacl.nbResult(gg));
    }
    
    
    
       // path on graph
    @Test
    public void testshaclext6() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = init();
        String i = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "prefix i: <http://www.inria.fr/2015/humans-instances#>"
                + "insert data {"
                + "[] a sh:NodeShape ;"
                + "xsh:targetNode i:Laura, i:Gaston ;"
                + "sh:property ["
                + "sh:path ([xsh:triplePath xsh:subject][xsh:nodePath xsh:graph]"
                + "[xsh:triplePath (xsh:graph rdf:type)] [xsh:object h:Person]);"
                //+ "xsh:function [xsh:success(true)];"
                + "sh:minCount 4"
                + "]"
                + "}";
        
        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Shacl shacl = new MyShacl(g);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        assertEquals(0, shacl.nbResult(gg));
    }
    
     // path on graph
    @Test
    public void testshaclext5() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = init();
        String i = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "prefix i: <http://www.inria.fr/2015/humans-instances#>"
                + "insert data {"
                + "[] a sh:NodeShape ;"
                + "xsh:targetNode i:Laura, i:Gaston ;"
                + "sh:property ["
                + "sh:path ([xsh:triplePath xsh:subject][xsh:nodePath xsh:graph]);"
                //+ "xsh:function [xsh:success(true)];"
                + "sh:minCount 2"
                + "]"
                + "}";
        
        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Shacl shacl = new MyShacl(g);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        assertEquals(1, shacl.nbResult(gg));
    }
    
    
    // path on predicates
    @Test
    public void testshaclext4() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = init();
        String i = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "prefix i: <http://www.inria.fr/2015/humans-instances#>"
                + "insert data {"
                + "[] a sh:NodeShape ;"
                + "xsh:targetNode i:Gaston ;"
                + "sh:property ["
                + "    sh:path ("
                + "    [sh:oneOrMorePath [xsh:triplePath xsh:subject] ] "
                + "    [xsh:nodePath xsh:predicate] );"
                + "  sh:pattern h:;"
                + "xsh:success()"
                + "]"
                + "}";
        
        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Shacl shacl = new MyShacl(g);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        assertEquals(6, shacl.nbResult(gg));
    }

    @Test
    public void testshaclext3() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = init();
        String i = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "insert data {"
                + "[] a sh:NodeShape ; sh:targetClass h:Person;"
                + "sh:property ["
                + "sh:path ([xsh:notExist ((rdf:type rdfs:subClassOf rdfs:subClassOf ))]"
                + "     rdf:type [xsh:filter([sh:hasValue h:Person])]) ;"
                + "sh:function [ sh:failure() ] ] "
                + "}";
        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Shacl shacl = new MyShacl(g);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        assertEquals(47, gg.size());
    }
    

    @Test
    public void testshaclext2() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = init();
        String i = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "insert data {"
                + "[] a sh:NodeShape ; sh:targetClass h:Person;"
                + "sh:property ["
                + "sh:path ([xsh:notExist ((rdf:type rdfs:subClassOf rdfs:subClassOf ))]"
                + "     rdf:type [xsh:filter([sh:not [sh:hasValue h:Person]])]) ;"
                + "sh:function [ sh:failure() ] ] "
                + "}";
        
        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Shacl shacl = new MyShacl(g);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        assertEquals(2, gg.size());
    }

    @Test
    public void testshaclext1() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = init();
        String i = "insert data {"
                + "[] a sh:NodeShape ;"
                + "sh:targetTriplesOf rdfs:range  ;"
                + "sh:property ["
                + "sh:path ([xsh:nodePath xsh:subject ][xsh:triplePath xsh:predicate  ]) ;"
                + "    sh:minCount 1]"
                + "}";
        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Shacl shacl = new Shacl(g);
        //shacl.setTrace(true);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        assertEquals(3, nbResult(gg));
    }
    
     @Test
    public void testshaclext0() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = init();
        String i = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "insert data {"
                + "[] a sh:NodeShape ;"
                + "sh:targetSubjectsOf rdfs:domain ;"
                + "sh:property ["
                + "    sh:path ([xsh:triplePath xsh:predicate] "
                + "        [xsh:notEqual (xsh:source xsh:predicate)]"
                + ");"
                + "sh:maxCount 0"
                + "]"
                + "}";
        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Shacl shacl = new Shacl(g);
        //shacl.setTrace(true);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        assertEquals(0, nbResult(gg));
    }
    
    @Test
     public void testshaclext00() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = init();
        String i = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "insert data {"
                + "rdf:type rdf:type rdf:Property "
                + "[] a sh:NodeShape ;"
                + "sh:targetNode rdf:type ;"
                + "sh:property ["
                + "    sh:path ( [xsh:triplePath xsh:predicate]"
                + "        [xsh:equal (xsh:subject xsh:predicate)]"
                + ");"
                + "sh:minCount 1"
                + "]"
                + "}";
        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Shacl shacl = new Shacl(g);
        //shacl.setTrace(true);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        assertEquals(0, nbResult(gg));
    }
     
      @Test
     public void testshaclext000() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = init();
        String i = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "insert data {"
                + "us:n1 us:test us:n2, us:n1 "
                + "[] a sh:NodeShape ;"
                + "sh:targetSubjectsOf us:test ;"
                + "sh:property ["
                + "    sh:path ( us:test [xsh:equal (xsh:source)]"
                + ");"
                + "sh:minCount 1"
                + "]"
                + "}";
        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Shacl shacl = new Shacl(g);
        //shacl.setTrace(true);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        assertEquals(0, nbResult(gg));
    }
     
    

    Graph init() throws EngineException, LoadException, IOException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.parse(data + "test/human1.rdf");
        ld.parse(data + "test/human2.rdf");
        ld.parse(data + "test/human.rdfs");
        return g;
    }

    @Test
    public void testshaclexp3() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.parse(data + "test/human1.rdf");
        ld.parse(data + "test/human2.rdf");
        ld.parse(data + "test/shapeexp2.ttl");

        String q = "@import <http://ns.inria.fr/sparql-template/function/datashape/main.rq> "
                + "select * where {}"
                + "bind (sh:shacl() as ?g)"
                + "}";

        Shacl shacl = new MyShacl(g);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        //System.out.println(gg.size());
        assertEquals(0, nbResult(gg));
    }

    @Test
    public void testshaclexp2() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.parse(data + "test/human1.rdf");
        ld.parse(data + "test/human2.rdf");

        String q = "prefix sh: <http://www.w3.org/ns/shacl#> ."
                + "prefix h:  <http://www.inria.fr/2015/humans#>"
                + "prefix i:  <http://www.inria.fr/2015/humans-instances#>"
                + "@import <http://ns.inria.fr/sparql-template/function/datashape/main.rq> "
                + "select * where {"
                + "bind (sh:start(xt:graph()) as ?st)"
                + "?x  h:age ?a "
                + "optional { ?x h:trouserssize ?t }"
                + "optional { ?x h:shirtsize ?s }"
                + "filter (sh:compute(?x, sh:compile(funcall(?fun)))) "
                + "}"
                + "order by ?x "
                + "values ?fun {UNDEF}"
                + "function us:exp1() {let (exp =  @(rq:lt h:age 25)  ) {exp}}"
                + "function us:exp2() {let (exp =  @(rq:gt h:shoesize h:shirtsize)  ) {exp}}"
                + "function us:exp3() {let (exp =  @(rq:or (rq:lt h:trouserssize h:shirtsize) (rq:lt h:age 50) ) ) {exp}}"
                + "function us:exp4() {let (exp =  @(rq:if (rq:lt h:age 50) false true)   ) {exp}}"
                + "function us:exp5() {let (exp =  @(rq:eq (rq:self) (rq:self i:John) )) {exp}}"
               // + "function us:exp6() {let (exp =  @(rq:and (rq:lt h:trouserssize (rq:mult 10 h:shirtsize)) (rq:not (rq:lt h:age 0)) ) ) {exp}}"
                + "function us:exp7() {let (exp =  @(rq:coalesce h:undef true ) ) {exp}}"
                + "function us:exp8() {let (exp =  @(rq:exist h:age) ) {exp}}"
                + "function us:exp9() {let (exp =  @(rq:if (rq:exist h:age) (rq:lt h:age 18) true)) {exp}}"
                + "function us:exp10(){let (exp =  @(rq:coalesce  (rq:lt h:age 18) true)) {exp}}";
        Mappings map = exec.query(q, map("?fun", NSManager.USER + "exp1"));
        //System.out.println(map);
        assertEquals(2, map.size());

        map = exec.query(q, map("?fun", NSManager.USER + "exp2"));
        //System.out.println(map);
        assertEquals(1, map.size());

        map = exec.query(q, map("?fun", NSManager.USER + "exp3"));
        //System.out.println(map);
        assertEquals(5, map.size());

        map = exec.query(q, map("?fun", NSManager.USER + "exp4"));
        //System.out.println(map);
        assertEquals(3, map.size());

        map = exec.query(q, map("?fun", NSManager.USER + "exp5"));
        //System.out.println(map);
        assertEquals(1, map.size());

//        map = exec.query(q, map("?fun", NSManager.USER + "exp6"));
//        //System.out.println(map);
//        assertEquals(7, map.size());

        map = exec.query(q, map("?fun", NSManager.USER + "exp7"));
        //System.out.println(map);
        assertEquals(8, map.size());

        map = exec.query(q, map("?fun", NSManager.USER + "exp8"));
        //System.out.println(map.size());
        assertEquals(8, map.size());

        map = exec.query(q, map("?fun", NSManager.USER + "exp9"));
        //System.out.println(map);
        assertEquals(2, map.size());

        map = exec.query(q, map("?fun", NSManager.USER + "exp10"));
        //System.out.println(map);
        assertEquals(2, map.size());

    }

    Mapping map(String var, String value) {
        Node q = NodeImpl.createVariable(var);
        Node t = NodeImpl.createResource(value);
        return Mapping.create(q, t);
    }

    @Test
    public void testshaclexp1() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.parse(data + "test/human1.rdf");
        ld.parse(data + "test/human2.rdf");
        ld.parse(data + "test/shapeexp1.ttl");

        Shacl shacl = new MyShacl(g);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        assertEquals(11, gg.size());
    }

    //@Test
//    public void testshacljavaexp2() throws EngineException, LoadException, IOException, TransformerException {
//        Graph g = Graph.create();
//        QueryProcess exec = QueryProcess.create(g);
//        Load ld = Load.create(g);
//        ld.parse(data + "test/human1.rdf");
//        ld.parse(data + "test/human2.rdf");
//        ld.parse(data + "test/shapeexp2.ttl");
//
//        ShaclJava shacl = new ShaclJava(g);
//        Graph gg = shacl.eval();
//        //System.out.println(Transformer.turtle(gg));
//        assertEquals(2, gg.size());
//    }

    @Test
    public void testshaclexp4() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.parse(data + "test/human1.rdf");
        ld.parse(data + "test/human2.rdf");
        ld.parse(data + "test/shapeexp3.ttl");
        Shacl shacl = new MyShacl(g);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        assertEquals(11, gg.size());
    }

    @Test
    public void testshaclexp5() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.parse(data + "test/human1.rdf");
        ld.parse(data + "test/human2.rdf");
        ld.parse(data + "test/shapeexp4.ttl");
        ld.parse(data + "test/shapeexp4.rq");

        Shacl shacl = new MyShacl(g);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        assertEquals(10, gg.size());
    }

    @Test
    public void testshaclexp6() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.parse(data + "test/human1.rdf");
        ld.parse(data + "test/human2.rdf");
        ld.parse(data + "test/shapeexp6.ttl");
        ld.parse(data + "test/shapeexp6.rq");

        Shacl shacl = new MyShacl(g);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        assertEquals(2, gg.size());
    }

    @Test
    public void testshaclexp7() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.parse(data + "test/human1.rdf");
        ld.parse(data + "test/human2.rdf");
        ld.parse(data + "test/shapeexp7.ttl");
        ld.parse(data + "test/shapeexp7.rq");

        Shacl shacl = new MyShacl(g);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        assertEquals(2, gg.size());
    }

    @Test
    public void testshaclexp8() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.parse(data + "test/human1.rdf");
        ld.parse(data + "test/human2.rdf");
        ld.parse(data + "test/shapeexp8.ttl");
        ld.parse(data + "test/shapeexp8.rq");

        Shacl shacl = new MyShacl(g);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        assertEquals(11, gg.size());
    }

    @Test
    public void testshaclexp9() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.parse(data + "test/human1.rdf");
        ld.parse(data + "test/human2.rdf");
        ld.parse(data + "test/shapeexp9.ttl");
        ld.parse(data + "test/shapeexp9.rq");

        Shacl shacl = new MyShacl(g);
        //shacl.setTrace(true);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        assertEquals(1, nbResult(gg));
    }

    @Test
    public void testshaclexp10() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.parse(data + "test/human.rdfs");
        ld.parse(data + "test/human1.rdf");
        ld.parse(data + "test/human2.rdf");
        ld.parse(data + "test/shapeexp10.ttl");

        Shacl shacl = new MyShacl(g);
        Graph gg = shacl.eval();
        //System.out.println(Transformer.turtle(gg));
        assertEquals(33, nbResult(gg));
    }

    // number of failure in report graph
    // number of value of property sh:result
    int nbResult(Graph g) {
        return g.size(DatatypeMap.newResource(NBRESULT));
    }

//    @Test
//    public void testshacljavaexp1() throws EngineException, LoadException, IOException, TransformerException {
//        Graph g = Graph.create();
//        QueryProcess exec = QueryProcess.create(g);
//        Load ld = Load.create(g);
//        ld.parse(data + "test/human1.rdf");
//        ld.parse(data + "test/human2.rdf");
//        ld.parse(data + "test/shapeexp1.ttl");
//
//        ShaclJava shacl = new ShaclJava(g);
//        Graph gg = shacl.eval();
//        //System.out.println(Transformer.turtle(gg));
//        assertEquals(11, gg.size());
//    }

    //@Test
    public void testshacl1() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(data + "test/myshape.ttl");

        QueryProcess exec = QueryProcess.create(g);
        String q = "@import <http://ns.inria.fr/sparql-template/function/datashape/main.rq> "
                + "select ?x where {"
                + "?x a foaf:Person "
                + "bind (sh:shaclnode(?x) as ?g) "
                + "filter (sh:conform(?g))"
                + "}";

        Mappings map = exec.query(q);
        System.out.println(map);
        assertEquals(1, map.size());
    }

    @Test
    public void testshacl() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(data + "test/myshape.ttl");

        Shacl shacl = new MyShacl(g);
        Graph res = shacl.eval();
        assertEquals(20, res.size());

        IDatatype dt = DatatypeMap.newResource(NSManager.USER, "test1");
        res = shacl.shape(dt);
        assertEquals(11, res.size());

        IDatatype obj = DatatypeMap.newResource(NSManager.USER, "John");
        res = shacl.shape(dt, obj);
        assertEquals(2, res.size());

        obj = DatatypeMap.newResource(NSManager.USER, "Jim");
        res = shacl.node(obj);
        assertEquals(2, res.size());
    }
    
    
    
    
      Shacl api2(String q) throws EngineException, LoadException, IOException {
        return api(q, true);
    }
    
    Shacl api(String q, boolean list) throws EngineException, LoadException, IOException {
        Graph g = myinit();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(q);
        Shacl shacl = new Shacl(g);
        //shacl.setTrace(true);
        IDatatype dt = exec.funcall(NSManager.USER + "defshape");
        shacl.funeval((list)?DatatypeMap.list(dt):dt);
        shacl.funparse();
        //System.out.println(Transformer.turtle(shacl.getResult()));
        return shacl;
    }
    
    
 @Test
    public void testapi2() throws EngineException, LoadException, IOException, TransformerException {      
        Shacl shacl = api2(qapi1());
        Graph g = shacl.getResult();
        //System.out.println(Transformer.turtle(g));
        
        shacl = api2(qapi10());
        g = shacl.getResult();
    }
    
    String qapi10() {
        String q =   "prefix h: <http://www.inria.fr/2015/humans#>"
                + "@public function us:defshape() {"
                + "    let (shape = @(sh:shape us:test"
                + "        (sh:targetClass h:Person)"
                + "(sh:property "
                + "(sh:path (sh:sequencePath (h:hasFriend "
                + "(xsh:function(xsh:filter((sh:class h:Woman))))"
                + "))) "
                + "(xsh:function(xsh:display(true)))))"
                + ") {"
                + "shape}"
                + "}";
        
        return q;
    }
    
     String qapi1() {
        String q = "prefix h: <http://www.inria.fr/2015/humans#> "
                + "prefix i: <http://www.inria.fr/2015/humans-instances#> "
//                + "insert data {"
//                + "us:test a sh:NodeShape ;"
//                + "sh:targetClass h:Person ;"
//                + "sh:property ["
//                + "sh:path h:hasFriend ; sh:class h:Person ;"
//                + "sh:and ([sh:class h:Person][sh:pattern i:])"
//                + "] ;"
//                + "sh:property ["
//                + "sh:path h:hasChild ; sh:class h:Person ;"
//                + "] ;"  
//                
//                + "sh:node us:test2 ;"
//                
//                + "sh:and ([sh:class h:Person]"
//                + "[sh:property [ sh:path h:name ; sh:datatype xsd:string]]);"
//                
//                + "sh:property ["
//                + "sh:path sh:hasParent ;"
//                + "sh:qualifiedMinCount 1 ;"
//                + "sh:qualifiedValueShape [sh:class h:Man]"
//                + "]"
//                + "}"
                
                + "@public function us:defshape() {"
                + "    let (shape = @(sh:shape us:test"
                + "        (sh:targetClass h:Person)"
                + "(sh:property (sh:path h:hasFriend) "
                + "(sh:and (sh:class h:Person)(sh:pattern i:)"
                + "  (sh:in (i:Alice i:Sophie i:Gaston))"
                + ")"
                + ")"
                + "    )) {shape}"
                + "}";
        return q;
    }
    
    
    String qapi6() {
        String q = "prefix h: <http://www.inria.fr/2015/humans#> "
                + "prefix i: <http://www.inria.fr/2015/humans-instances#> "
                + "insert data {"
                + "us:test a sh:NodeShape ;"
                + "sh:targetSubjectsOf h:hasFriend "
                + "}"
                + "@public function us:defshape() {"
                + "    let (shape = @(sh:shape us:test "
                + "(sh:targetClass h:Person )"
                + "(sh:property (sh:path h:hasFriend)"
                + "(sh:qualifiedValueShape (sh:class h:Person)(sh:qualifiedValueShapesDisjoint true)(sh:qualifiedMinCount 1)))"
                + "(sh:property (sh:path h:hasChild)"
                + "(sh:qualifiedValueShape (sh:class h:Person)(sh:qualifiedMinCount 1)))"
                + "    )) { shape }"
                + "}";
        
        return q;
    }

    Shacl api(String q) throws EngineException, LoadException, IOException {
        Graph g = myinit();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(q);
        Shacl shacl = new Shacl(g);
        IDatatype dt = exec.funcall(NSManager.USER + "defshape");
        shacl.input().setVariable("?before", DatatypeMap.TRUE);
        shacl.input().setVariable("?defshape", dt);
        shacl.eval();
        return shacl;
    }
    
  
    
    @Test
    public void testapi() throws EngineException, LoadException, IOException, TransformerException {      
        Shacl shacl = api2(qapi1());
        Graph g = shacl.getResult();
        //System.out.println(Transformer.turtle(g));
        assertEquals(1, shacl.nbResult(g));
        
        shacl = api2(qapi2());
        g = shacl.getResult();
        //System.out.println(Transformer.turtle(g));
        assertEquals(1, shacl.nbResult(g));
        
        shacl = api2(qapi3());
        g = shacl.getResult();
        //System.out.println(Transformer.turtle(g));
        assertEquals(0, shacl.nbResult(g));
        
        shacl = api2(qapi4());
        g = shacl.getResult();
        //System.out.println(Transformer.turtle(g));
        assertEquals(1, shacl.nbResult(g));
        
        shacl = api2(qapi5());
        g = shacl.getResult();
        //System.out.println(Transformer.turtle(g));
        
        shacl = api2(qapi6());
        g = shacl.getResult();
        //System.out.println(Transformer.turtle(g));
        
        shacl = api(qapi7(), false);
        g = shacl.getResult();
        System.out.println(Transformer.turtle(g));
    }
    
  
    String qapi7() {
        String q = "prefix h: <http://www.inria.fr/2015/humans#> "
                + "prefix i: <http://www.inria.fr/2015/humans-instances#> "
                + "insert data {"
                + "us:test a sh:NodeShape ;"
                + "sh:targetSubjectsOf h:name ;"
                + "}"
                + "@public function us:defshape() {"
                + "    let (shape = @("
                + "(sh:shape us:test "
                + "(sh:targetSubjectsOf h:name )"
                + "(sh:property (sh:path h:hasFriend)"
                + "(sh:node us:node)"
                + "))"
                + "(sh:shape us:node (sh:class h:Person))"
                + ")) {shape}"
                + "}";
        return q;
    }
    
    
      String qapi5() {
        String q = "prefix h: <http://www.inria.fr/2015/humans#> "
                + "prefix i: <http://www.inria.fr/2015/humans-instances#> "
                + "insert data {"
                + "us:test a sh:NodeShape ;"
                + "sh:targetSubjectsOf h:name ;"
                + "}"
                + "@public function us:defshape() {"
                + "    let (shape = @(sh:shape us:test "
                + "(sh:targetSubjectsOf h:name )"
                + "(sh:property (sh:path (xsh:function (xsh:predicatePath(xsh:subject))))"
                + "(xsh:function (xsh:display(true)))"
                + ")"
                + "    )) {shape}"
                + "}";
        return q;
    }
    
      String qapi4() {
        String q = "prefix h: <http://www.inria.fr/2015/humans#> "
                + "prefix i: <http://www.inria.fr/2015/humans-instances#> "
                + "insert data {"
                + "us:test a sh:NodeShape ;"
                + "sh:targetClass h:Person ;"
                + "}"
                + "@public function us:defshape() {"
                + "    let (shape = @(sh:shape us:test"
                + "        (sh:targetClass h:Person)"                
                + "(sh:property (sh:path (sh:sequencePath ((sh:alternativePath (h:hasChild h:hasFriend )) h:name) )) "
                + "(xsh:function (xsh:display(true)))"
                + "(sh:datatype xsd:string)"
                + ")"
                + "    )) {shape}"
                + "}";
        return q;
    }

      String qapi3() {
        String q = "prefix h: <http://www.inria.fr/2015/humans#> "
                + "prefix i: <http://www.inria.fr/2015/humans-instances#> "
                + "insert data {"
                + "us:test a sh:NodeShape ;"
                + "sh:targetNode i:Sophie ;"
                + "}"
                + "@public function us:defshape() {"
                + "    let (shape = @(sh:shape us:test"
                + "        (sh:targetNode i:Sophie)"                
                + "(sh:property (sh:path h:name) "
                + "(xsh:function (xsh:display(true)))"
                + "(sh:languageIn ('en' 'fr'))"
                + "(sh:hasValue 'Sophie'@fr)"
                + ")"
                + "    )) {shape}"
                + "}";
        return q;
    }
    
     String qapi2() {
        String q = "prefix h: <http://www.inria.fr/2015/humans#> "
                + "prefix i: <http://www.inria.fr/2015/humans-instances#> "
                + "insert data {"
                + "us:test a sh:NodeShape ;"
                + "sh:targetClass h:Person ;"
                + "}"
                + "@public function us:defshape() {"
                + "    let (shape = @(sh:shape us:test"
                + "        (sh:targetClass h:Person)"                
                + "(sh:property (sh:path h:hasFriend) "
                + "(sh:equals h:hasFriend) (sh:class h:Person)(sh:pattern i:)(sh:nodeKind sh:IRI)"
                + "(sh:minLength 5)(sh:minCount 0)"
                + "(xsh:function (xsh:display(true)))"
                + "(sh:property (sh:path h:name)(sh:minCount 1)(sh:datatype xsd:string)"
                + ")"
                + "(sh:node (sh:and (sh:not (sh:pattern h:))))"
                + ")"
                + "    )) {shape}"
                + "}";
        return q;
    }
    
 

}
