package fr.inria.corese.test.engine;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.datatype.RDF;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.AccessRight;
import fr.inria.corese.sparql.triple.parser.AccessRightDefinition;
import fr.inria.corese.sparql.triple.parser.NSManager;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author corby
 */
public class TestAccessRight {
    static String data  = Thread.currentThread().getContextClassLoader().getResource("data/").getPath() ;
    
     
         //@Test
   
    
    
     //@Test
    public void test9() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        AccessRight.setActive(true);
        String i1 = "insert data {us:John foaf:knows us:Jack ; foaf:name 'John' .}";           
        String q = "select * where { ?x foaf:knows ?y ; foaf:name ?n }";
        AccessRight access = new AccessRight();
        access.setInsert(AccessRight.ACCESS_MAX);

        AccessRightDefinition.getSingleton()
                .getPredicate()
                .define(NSManager.FOAF, AccessRight.RESTRICTED);
        
        access.getAccessRightDefinition()
                .getPredicate()
                .define(NSManager.FOAF+"knows", AccessRight.PUBLIC)
                .define(NSManager.FOAF+"name",  AccessRight.PUBLIC)
                ;
        
        exec.query(i1);

        access.setWhere(AccessRight.PUBLIC);
        Mappings map = exec.query(q);
        assertEquals(1, map.size());
        
       
        
        map = exec.query(q);
       
    
    }
    
    
    
         @Test
    public void test8() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
                AccessRight access = new AccessRight();

        AccessRight.setActive(true);
        access.setMode(AccessRight.GT_MODE);
        access.setInsert(AccessRight.ACCESS_MAX);

        String i1 = "insert data { graph us:g1 { us:Jack foaf:knows us:Jim  ; rdfs:label 'Jack' } }";
        String i2 = "insert data { graph us:g2 { us:Jack foaf:knows us:Jim  ; rdfs:label 'Jack' } }";
        
        String q = "select * where { graph ?g { ?x ?p ?y } }";

        AccessRightDefinition def = access.getAccessRightDefinition();
        
        def.getGraph().define(NSManager.USER+"g1", AccessRight.PROTECTED);
        def.getGraph().define(NSManager.USER+"g2", AccessRight.RESTRICTED);
        
        exec.query(i1, access);
        exec.query(i2, access);
        
        Mappings map = exec.query(q, access);        
        assertEquals(0, map.size());
        
        access.setWhere(AccessRight.PROTECTED);        
        map = exec.query(q, access);  
        assertEquals(2, map.size());        
        
        access.setWhere(AccessRight.RESTRICTED);        
        map = exec.query(q, access); 
        assertEquals(4, map.size());           
    }
    
    
    
    
    
     @Test
    public void test7() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        AccessRight access = new AccessRight();
        AccessRight.setActive(true);
        AccessRight.setMode(AccessRight.GT_MODE);
        //access.setDebug(true);
        // access right granted to insert edge with access_max assigned access right
        access.setInsert(AccessRight.ACCESS_MAX);
        // default access right assigned to inserted edges
        access.setDefine(AccessRight.PROTECTED);
        
        AccessRightDefinition def = access.getAccessRightDefinition();
        def.getPredicate().define("http://www.inria.fr/2015/humans#age",       AccessRight.PRIVATE);
        def.getNode().define("http://www.inria.fr/2015/humans-instances#John", AccessRight.RESTRICTED);
        
        
        //String i = "load </user/corby/home/AADemo/coursshacl/data/human1.rdf>";
        String i = String.format("load <%shuman/human1.rdf>", data);
       //load.parse(data+"human/human1.rdf");

        exec.query(i, access);
                      
        String q = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "select * where { ?x h:age ?a }";  
        
        Mappings map = exec.query(q, access);
        System.out.println(map);
        assertEquals(0, map.size());
        
        access.setWhere(AccessRight.PROTECTED);
        map = exec.query(q, access);
        assertEquals(0, map.size());

        access.setWhere(AccessRight.PRIVATE);
        map = exec.query(q, access);
        assertEquals(4, map.size());
        
        String q2 = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "select * where { ?x h:name 'John' }";
        
        access.setWhere(AccessRight.PROTECTED);
        map = exec.query(q2, access);
        assertEquals(0, map.size());
        
        access.setWhere(AccessRight.RESTRICTED);
        map = exec.query(q2, access);
        assertEquals(1, map.size());
        
    }
       
    @Test
    public void test6() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        AccessRight access = new AccessRight();
        AccessRight.setActive(true);
        AccessRight.setMode(AccessRight.GT_MODE);
        
        Load load = Load.create(g);
        load.getAccessRight().setInsert(AccessRight.ACCESS_MAX);
        load.getAccessRight().setDefine(AccessRight.PROTECTED);
        AccessRightDefinition def = load.getAccessRight().getAccessRightDefinition();
        def.getPredicate().define("http://www.inria.fr/2015/humans#age",       AccessRight.PRIVATE);
        def.getNode().define("http://www.inria.fr/2015/humans-instances#John", AccessRight.RESTRICTED);
        
        load.parse(data+"human/human1.rdf");
        String q = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "select * where { ?x h:age ?a }";  
        
        Mappings map = exec.query(q, access);
        assertEquals(0, map.size());
        
        access.setWhere(AccessRight.PROTECTED);
        map = exec.query(q, access);
        assertEquals(0, map.size());
        
        access.setWhere(AccessRight.PRIVATE);
        map = exec.query(q, access);
        assertEquals(4, map.size());
        
        String q2 = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "select * where { ?x h:name 'John' }";
        
        access.setWhere(AccessRight.PROTECTED);
        map = exec.query(q2, access);
        assertEquals(0, map.size());
        
        access.setWhere(AccessRight.RESTRICTED);
        map = exec.query(q2, access);
        assertEquals(1, map.size());
        
    }
    
       @Test
    public void test5() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        AccessRight access = new AccessRight();
        AccessRight.setActive(true);

        String i1 = "insert data {us:John foaf:knows us:Jack ; rdfs:label 'John' }";
        String i2 = "insert data {us:Jack foaf:knows us:Jim  ; rdfs:label 'Jack' }";
        String i3 = "insert data { graph us:g1 { us:James us:knows us:James  ; us:label 'James' } }";
        
        String q = "select * where { ?x ?p ?y }";

        access.setInsert(AccessRight.ACCESS_MAX);
        AccessRightDefinition def = access.getAccessRightDefinition();
        def.getPredicate().define(NSManager.FOAF, AccessRight.PRIVATE);
        def.getPredicate().define(RDF.RDFS, AccessRight.PRIVATE);
        def.getGraph().define(NSManager.USER+"g1", AccessRight.PROTECTED);
        def.getNode().define(NSManager.USER+"James", AccessRight.PROTECTED);

        exec.query(i1, access);
        exec.query(i2, access);
        exec.query(i3, access);

        access.setWhere(AccessRight.PRIVATE);
        access.setMode(AccessRight.EQ_MODE);
        
        Mappings map = exec.query(q, access);
        assertEquals(4, map.size());
        
        
    }
    
      @Test
    public void test4() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        AccessRight access = new AccessRight();
        AccessRight.setActive(true);

        String i1 = "insert data {us:John foaf:knows us:Jack ; rdfs:label 'John' }";
        String i2 = "insert data {us:Jack foaf:knows us:Jim  ; rdfs:label 'Jack' }";
        String i3 = "insert data { graph us:g1 { us:James us:knows us:James  ; us:label 'James' } }";
        
        String q = "select * where { ?x ?p ?y }";

        access.setInsert(AccessRight.ACCESS_MAX);
        AccessRightDefinition def = access.getAccessRightDefinition();
        def.getPredicate().define(NSManager.FOAF, AccessRight.PRIVATE);
        def.getPredicate().define(RDF.RDFS, AccessRight.PRIVATE);
        def.getGraph().define(NSManager.USER+"g1", AccessRight.PROTECTED);
        def.getNode().define(NSManager.USER+"James", AccessRight.PRIVATE);
        
        exec.query(i1, access);
        exec.query(i2, access);
        exec.query(i3, access);

        access.setWhere(AccessRight.PROTECTED);
        Mappings map = exec.query(q, access);
        assertEquals(0, map.size());
        
    }
    
     @Test
    public void test3() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        AccessRight access = new AccessRight();
        AccessRight.setActive(true);

        String i1 = "insert data {us:John foaf:knows us:Jack ; rdfs:label 'John' }";
        String i2 = "insert data {us:Jack foaf:knows us:Jim  ; rdfs:label 'Jack' }";
        String i3 = "insert data { graph us:g1 { us:James us:knows us:James  ; us:label 'James' } }";
        
        String q = "select * where { ?x ?p ?y }";

        access.setInsert(AccessRight.ACCESS_MAX);
        access.getAccessRightDefinition().getPredicate().define(NSManager.FOAF, AccessRight.PRIVATE);
        access.getAccessRightDefinition().getPredicate().define(RDF.RDFS, AccessRight.PRIVATE);
        access.getAccessRightDefinition().getGraph().define(NSManager.USER+"g1", AccessRight.PROTECTED);
        //access.getAccessRightDefinition().getNode().define(NSManager.USER+"James", AccessRight.PRIVATE);
        
        exec.query(i1, access);
        exec.query(i2, access);
        exec.query(i3, access);

        access.setWhere(AccessRight.PROTECTED);
        Mappings map = exec.query(q, access);
        assertEquals(2, map.size());
        
    }
    

    @Test
    public void test2() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        AccessRight access = new AccessRight();
        AccessRight.setActive(true);

        String i1 = "insert data {us:John foaf:knows us:Jack ; rdfs:label 'John' }";
        String i2 = "insert data {us:Jack foaf:knows us:Jim  ; rdfs:label 'Jack' }";
        String q = "select * where { ?x ?p ?y }";
        access.setInsert(AccessRight.ACCESS_MAX);
        access.getAccessRightDefinition().getPredicate().define(NSManager.FOAF, AccessRight.PROTECTED);
        access.getAccessRightDefinition().getPredicate().define(RDF.RDFS, AccessRight.PRIVATE);
        
        exec.query(i1, access);
        exec.query(i2, access);

        access.setWhere(AccessRight.PUBLIC);
        Mappings map = exec.query(q, access);
        assertEquals(0, map.size());
        
        access.setWhere(AccessRight.PROTECTED);
        map = exec.query(q, access);
        assertEquals(2, map.size());
        
        access.setWhere(AccessRight.PRIVATE);
        map = exec.query(q, access);
        assertEquals(4, map.size());

    }
    
    

    @Test
    public void test1() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        AccessRight access = new AccessRight();
        AccessRight.setActive(true);

        String i1 = "insert data {us:John foaf:knows us:Jack .}";
        String i2 = "insert data {us:Jack  foaf:knows us:Jim .}";

        access.setDefineInsert(AccessRight.PUBLIC);
        exec.query(i1, access);
        access.setDefineInsert(AccessRight.PROTECTED);
        exec.query(i2, access);

        String q = "select * where { ?x ?p ?y }";

        access.setWhere(AccessRight.PUBLIC);
        Mappings map = exec.query(q, access);
        Assert.assertEquals(1, map.size());

        access.setWhere(AccessRight.PROTECTED);
        map = exec.query(q, access);
        Assert.assertEquals(2, map.size());

        String i3 = "insert { ?x foaf:knows ?z } where { ?x foaf:knows ?y . ?y foaf:knows ?z }";

        access.setWhere(AccessRight.PUBLIC);
        map = exec.query(i3, access);
        Assert.assertEquals(0, map.size());

        access.setWhere(AccessRight.PROTECTED);
        access.setDefineInsert(AccessRight.PROTECTED);
        map = exec.query(i3, access);
        Assert.assertEquals(1, map.size());

        access.setWhere(AccessRight.PUBLIC);
        map = exec.query(q, access);
        Assert.assertEquals(1, map.size());

        access.setWhere(AccessRight.PROTECTED);
        map = exec.query(q, access);
        Assert.assertEquals(3, map.size());

        String i4 = "delete {?x ?p ?y} where {?x ?p ?y}";
        access.setDelete(AccessRight.PUBLIC);
        access.setWhere(AccessRight.PRIVATE);
        Assert.assertEquals(3, g.size());
        map = exec.query(i4, access);
        Assert.assertEquals(2, g.size());

        access.setDelete(AccessRight.PRIVATE);
        map = exec.query(i4, access);
        Assert.assertEquals(0, g.size());
    }

    void test22(Graph g) throws EngineException {
        QueryProcess exec = QueryProcess.create(g);
        AccessRight access = new AccessRight();
        access.setWhere(AccessRight.PUBLIC);

        String q = "select where {"
                + "bind (us:test(xt:graph()) as ?t)"
                + "}"
                + "function us:test(g) {"
                + "for (at in g) {"
                + "xt:print(at)"
                + "}"
                + "}";

        Mappings map = exec.query(q);
    }

}
