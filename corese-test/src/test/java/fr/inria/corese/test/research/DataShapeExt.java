package fr.inria.corese.test.research;
import fr.inria.corese.compiler.parser.NodeImpl;
import java.io.IOException;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.shacl.Shacl;
import fr.inria.corese.core.shacl.ShaclJava;
import fr.inria.corese.core.transform.Transformer;
import org.junit.Test;


import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import javax.xml.transform.TransformerException;



import static org.junit.Assert.*;
/**
 *
 * @author corby
 */
public class DataShapeExt {
    
        static String data  = Thread.currentThread().getContextClassLoader().getResource("data/").getPath() ;

    
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
        
       Shacl shacl = new Shacl(g);
       Graph gg = shacl.eval();
       System.out.println(Transformer.turtle(gg));
         System.out.println(gg.size());
        //assertEquals(11, gg.size());
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
+ "function us:exp6() {let (exp =  @(rq:and (rq:lt h:trouserssize (rq:mult 10 h:shirtsize)) (rq:not (rq:lt h:age 0)) ) ) {exp}}"                  
+ "function us:exp7() {let (exp =  @(rq:coalesce h:undef true ) ) {exp}}"                 
+ "function us:exp8() {let (exp =  @(rq:exist h:age) ) {exp}}"                  
+ "function us:exp9() {let (exp =  @(rq:if (rq:exist h:age) (rq:lt h:age 18) true)) {exp}}"                  
+ "function us:exp10(){let (exp =  @(rq:coalesce  (rq:lt h:age 18) true)) {exp}}"                  
                ;       
       Mappings 
       map = exec.query(q, map("?fun", NSManager.USER+"exp1"));
       //System.out.println(map);
       assertEquals(2, map.size());
       
       map = exec.query(q, map("?fun", NSManager.USER+"exp2"));
       //System.out.println(map);
       assertEquals(1, map.size());
       
       map = exec.query(q, map("?fun", NSManager.USER+"exp3"));
       //System.out.println(map);
       assertEquals(5, map.size());
       
       map = exec.query(q, map("?fun", NSManager.USER+"exp4"));
       //System.out.println(map);
       assertEquals(3, map.size());
       
       map = exec.query(q, map("?fun", NSManager.USER+"exp5"));
       //System.out.println(map);
       assertEquals(1, map.size());
       
       map = exec.query(q, map("?fun", NSManager.USER+"exp6"));
       //System.out.println(map);
       assertEquals(7, map.size());
       
       map = exec.query(q, map("?fun", NSManager.USER+"exp7"));
       //System.out.println(map);
       assertEquals(8, map.size());
       
       map = exec.query(q, map("?fun", NSManager.USER+"exp8"));
       //System.out.println(map.size());
       assertEquals(8, map.size());
       
       map = exec.query(q, map("?fun", NSManager.USER+"exp9"));
       //System.out.println(map);
       assertEquals(2, map.size());
       
       map = exec.query(q, map("?fun", NSManager.USER+"exp10"));
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
        
       Shacl shacl = new Shacl(g);
       Graph gg = shacl.eval();
       //System.out.println(Transformer.turtle(gg));
        assertEquals(11, gg.size());
    }
    
      //@Test
    public void testshacljavaexp2() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.parse(data + "test/human1.rdf");
        ld.parse(data + "test/human2.rdf");
        ld.parse(data + "test/shapeexp2.ttl");
        
       ShaclJava shacl = new ShaclJava(g);
       Graph gg = shacl.eval();
       //System.out.println(Transformer.turtle(gg));
       assertEquals(2, gg.size());
    }
    
    @Test
    public void testshaclexp4() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.parse(data + "test/human1.rdf");
        ld.parse(data + "test/human2.rdf");
        ld.parse(data + "test/shapeexp3.ttl");        
       Shacl shacl = new Shacl(g);
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
        
       Shacl shacl = new Shacl(g);
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
        
       Shacl shacl = new Shacl(g);
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
        
       Shacl shacl = new Shacl(g);
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
        
       Shacl shacl = new Shacl(g);
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
        
       Shacl shacl = new Shacl(g);
       Graph gg = shacl.eval();
       //System.out.println(Transformer.turtle(gg));
       assertEquals(11, gg.size());
    }
    
     @Test
    public void testshacljavaexp1() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.parse(data + "test/human1.rdf");
        ld.parse(data + "test/human2.rdf");
        ld.parse(data + "test/shapeexp1.ttl");
        
       ShaclJava shacl = new ShaclJava(g);
       Graph gg = shacl.eval();
       //System.out.println(Transformer.turtle(gg));
        assertEquals(11, gg.size());
    }
    
    
        @Test
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
        assertEquals(1, map.size());
    }
    
    
    
         @Test
    public void testshacl() throws EngineException, LoadException, IOException, TransformerException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(data + "test/myshape.ttl");
        
        Shacl shacl = new Shacl(g);
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
    
    
}
