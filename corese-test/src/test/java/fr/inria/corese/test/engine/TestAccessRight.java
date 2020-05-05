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
    
         @Test
    public void test8() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        AccessRight.setActive(true);
        exec.getAccessRight().setMode(AccessRight.GT_MODE);

        String i1 = "insert data { graph us:g1 { us:Jack foaf:knows us:Jim  ; rdfs:label 'Jack' } }";
        String i2 = "insert data { graph us:g2 { us:Jack foaf:knows us:Jim  ; rdfs:label 'Jack' } }";
        
        String q = "select * where { graph ?g { ?x ?p ?y } }";

        AccessRightDefinition def = exec.getAccessRightDefinition();
        
        def.getGraph().define(NSManager.USER+"g1", AccessRight.PROTECTED);
        def.getGraph().define(NSManager.USER+"g2", AccessRight.RESTRICTED);
        
        exec.query(i1);
        exec.query(i2);
        
        Mappings map = exec.query(q);        
        assertEquals(0, map.size());
        
        exec.getAccessRight().setWhere(AccessRight.PROTECTED);        
        map = exec.query(q);  
        assertEquals(2, map.size());        
        
        exec.getAccessRight().setWhere(AccessRight.RESTRICTED);        
        map = exec.query(q); 
        assertEquals(4, map.size());           
    }
    
    
    
    
    
     @Test
    public void test7() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        AccessRight.setActive(true);
        
        exec.getAccessRight().setInsert(AccessRight.PROTECTED);
        AccessRightDefinition def = exec.getAccessRightDefinition();
        def.getPredicate().define("http://www.inria.fr/2015/humans#age",       AccessRight.PRIVATE);
        def.getNode().define("http://www.inria.fr/2015/humans-instances#John", AccessRight.RESTRICTED);
        
        
        String i = "load </user/corby/home/AADemo/coursshacl/data/human1.rdf>";
        exec.query(i);

        String q = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "select * where { ?x h:age ?a }";  
        
        Mappings map = exec.query(q);
        assertEquals(0, map.size());
        
        exec.getAccessRight().setWhere(AccessRight.PROTECTED);
        map = exec.query(q);
        assertEquals(0, map.size());
        
        exec.getAccessRight().setWhere(AccessRight.PRIVATE);
        map = exec.query(q);
        assertEquals(4, map.size());
        
        String q2 = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "select * where { ?x h:name 'John' }";
        
        exec.getAccessRight().setWhere(AccessRight.PROTECTED);
        map = exec.query(q2);
        assertEquals(0, map.size());
        
        exec.getAccessRight().setWhere(AccessRight.RESTRICTED);
        map = exec.query(q2);
        assertEquals(1, map.size());
        
    }
       
    @Test
    public void test6() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        AccessRight.setActive(true);
        
        Load load = Load.create(g);
        load.getAccessRight().setInsert(AccessRight.PROTECTED);
        AccessRightDefinition def = load.getAccessRight().getAccessRightDefinition();
        def.getPredicate().define("http://www.inria.fr/2015/humans#age",       AccessRight.PRIVATE);
        def.getNode().define("http://www.inria.fr/2015/humans-instances#John", AccessRight.RESTRICTED);
        
        load.parse(data+"human/human1.rdf");

        String q = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "select * where { ?x h:age ?a }";  
        
        Mappings map = exec.query(q);
        assertEquals(0, map.size());
        
        exec.getAccessRight().setWhere(AccessRight.PROTECTED);
        map = exec.query(q);
        assertEquals(0, map.size());
        
        exec.getAccessRight().setWhere(AccessRight.PRIVATE);
        map = exec.query(q);
        assertEquals(4, map.size());
        
        String q2 = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "select * where { ?x h:name 'John' }";
        
        exec.getAccessRight().setWhere(AccessRight.PROTECTED);
        map = exec.query(q2);
        assertEquals(0, map.size());
        
        exec.getAccessRight().setWhere(AccessRight.RESTRICTED);
        map = exec.query(q2);
        assertEquals(1, map.size());
        
    }
    
       @Test
    public void test5() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        AccessRight.setActive(true);

        String i1 = "insert data {us:John foaf:knows us:Jack ; rdfs:label 'John' }";
        String i2 = "insert data {us:Jack foaf:knows us:Jim  ; rdfs:label 'Jack' }";
        String i3 = "insert data { graph us:g1 { us:James us:knows us:James  ; us:label 'James' } }";
        
        String q = "select * where { ?x ?p ?y }";

        AccessRightDefinition def = exec.getAccessRightDefinition();
        def.getPredicate().define(NSManager.FOAF, AccessRight.PRIVATE);
        def.getPredicate().define(RDF.RDFS, AccessRight.PRIVATE);
        def.getGraph().define(NSManager.USER+"g1", AccessRight.PROTECTED);
        def.getNode().define(NSManager.USER+"James", AccessRight.PROTECTED);

        exec.query(i1);
        exec.query(i2);
        exec.query(i3);

        exec.getAccessRight().setWhere(AccessRight.PRIVATE);
        exec.getAccessRight().setMode(AccessRight.EQ_MODE);
        
        Mappings map = exec.query(q);
        assertEquals(4, map.size());
        
        
    }
    
      @Test
    public void test4() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        AccessRight.setActive(true);

        String i1 = "insert data {us:John foaf:knows us:Jack ; rdfs:label 'John' }";
        String i2 = "insert data {us:Jack foaf:knows us:Jim  ; rdfs:label 'Jack' }";
        String i3 = "insert data { graph us:g1 { us:James us:knows us:James  ; us:label 'James' } }";
        
        String q = "select * where { ?x ?p ?y }";

        AccessRightDefinition def = exec.getAccessRightDefinition();
        def.getPredicate().define(NSManager.FOAF, AccessRight.PRIVATE);
        def.getPredicate().define(RDF.RDFS, AccessRight.PRIVATE);
        def.getGraph().define(NSManager.USER+"g1", AccessRight.PROTECTED);
        def.getNode().define(NSManager.USER+"James", AccessRight.PRIVATE);
        
        exec.query(i1);
        exec.query(i2);
        exec.query(i3);

        exec.getAccessRight().setWhere(AccessRight.PROTECTED);
        Mappings map = exec.query(q);
        assertEquals(0, map.size());
        
    }
    
     @Test
    public void test3() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        AccessRight.setActive(true);

        String i1 = "insert data {us:John foaf:knows us:Jack ; rdfs:label 'John' }";
        String i2 = "insert data {us:Jack foaf:knows us:Jim  ; rdfs:label 'Jack' }";
        String i3 = "insert data { graph us:g1 { us:James us:knows us:James  ; us:label 'James' } }";
        
        String q = "select * where { ?x ?p ?y }";

        exec.getAccessRightDefinition().getPredicate().define(NSManager.FOAF, AccessRight.PRIVATE);
        exec.getAccessRightDefinition().getPredicate().define(RDF.RDFS, AccessRight.PRIVATE);
        exec.getAccessRightDefinition().getGraph().define(NSManager.USER+"g1", AccessRight.PROTECTED);
        //exec.getAccessRightDefinition().getNode().define(NSManager.USER+"James", AccessRight.PRIVATE);
        
        exec.query(i1);
        exec.query(i2);
        exec.query(i3);

        exec.getAccessRight().setWhere(AccessRight.PROTECTED);
        Mappings map = exec.query(q);
        assertEquals(2, map.size());
        
    }
    

    @Test
    public void test2() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        AccessRight.setActive(true);

        String i1 = "insert data {us:John foaf:knows us:Jack ; rdfs:label 'John' }";
        String i2 = "insert data {us:Jack foaf:knows us:Jim  ; rdfs:label 'Jack' }";
        String q = "select * where { ?x ?p ?y }";

        exec.getAccessRightDefinition().getPredicate().define(NSManager.FOAF, AccessRight.PROTECTED);
        exec.getAccessRightDefinition().getPredicate().define(RDF.RDFS, AccessRight.PRIVATE);
        
        exec.query(i1);
        exec.query(i2);

        exec.getAccessRight().setWhere(AccessRight.PUBLIC);
        Mappings map = exec.query(q);
        assertEquals(0, map.size());
        
        exec.getAccessRight().setWhere(AccessRight.PROTECTED);
        map = exec.query(q);
        assertEquals(2, map.size());
        
        exec.getAccessRight().setWhere(AccessRight.PRIVATE);
        map = exec.query(q);
        assertEquals(4, map.size());

    }
    
    

    @Test
    public void test1() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        AccessRight.setActive(true);

        String i1 = "insert data {us:John foaf:knows us:Jack .}";
        String i2 = "insert data {us:Jack  foaf:knows us:Jim .}";

        exec.getAccessRight().setInsert(AccessRight.PUBLIC);
        exec.query(i1);
        exec.getAccessRight().setInsert(AccessRight.PROTECTED);
        exec.query(i2);

        String q = "select * where { ?x ?p ?y }";

        exec.getAccessRight().setWhere(AccessRight.PUBLIC);
        Mappings map = exec.query(q);
        Assert.assertEquals(1, map.size());

        exec.getAccessRight().setWhere(AccessRight.PROTECTED);
        map = exec.query(q);
        Assert.assertEquals(2, map.size());

        String i3 = "insert { ?x foaf:knows ?z } where { ?x foaf:knows ?y . ?y foaf:knows ?z }";

        exec.getAccessRight().setWhere(AccessRight.PUBLIC);
        map = exec.query(i3);
        Assert.assertEquals(0, map.size());

        exec.getAccessRight().setWhere(AccessRight.PROTECTED);
        exec.getAccessRight().setInsert(AccessRight.PROTECTED);
        map = exec.query(i3);
        Assert.assertEquals(1, map.size());

        exec.getAccessRight().setWhere(AccessRight.PUBLIC);
        map = exec.query(q);
        Assert.assertEquals(1, map.size());

        exec.getAccessRight().setWhere(AccessRight.PROTECTED);
        map = exec.query(q);
        Assert.assertEquals(3, map.size());

        String i4 = "delete {?x ?p ?y} where {?x ?p ?y}";
        exec.getAccessRight().setDelete(AccessRight.PUBLIC);
        exec.getAccessRight().setWhere(AccessRight.PRIVATE);
        Assert.assertEquals(3, g.size());
        map = exec.query(i4);
        Assert.assertEquals(2, g.size());

        exec.getAccessRight().setDelete(AccessRight.PRIVATE);
        map = exec.query(i4);
        Assert.assertEquals(0, g.size());
    }

    void test(Graph g) throws EngineException {
        QueryProcess exec = QueryProcess.create(g);
        exec.getAccessRight().setWhere(AccessRight.PUBLIC);

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
