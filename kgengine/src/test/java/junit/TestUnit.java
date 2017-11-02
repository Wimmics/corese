package junit;

import java.util.Date;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.BuildImpl;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import java.util.logging.Logger;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestUnit {

    //static String data = "/home/corby/NetBeansProjects/kgram/trunk/kgengine/src/test/resources/data/";
    static String data = TestUnit.class.getClassLoader().getResource("data").getPath() + "/";
    static String rule = TestUnit.class.getClassLoader().getResource("rule").getPath() + "/";
    //static String webapp = TestUnit.class.getClassLoader().getResource("webapp").getPath() + "/";
    static String QUERY = TestUnit.class.getClassLoader().getResource("query").getPath() + "/";
    static String root = data;
    static String text = "/home/corby/NetBeansProjects/kgram/trunk/kgengine/src/test/resources/text/";
    static String ndata = "/home/corby/workspace/kgtool/src/test/resources/data/";
    static String cos2 = "/home/corby/workspace/coreseV2/src/test/resources/data/ign/";
    static String cos = "/home/corby/workspace/corese/data/";
    static Graph graph;
    private String SPIN_PREF = "prefix sp: <" + NSManager.SPIN + ">\n";
    private String FOAF_PREF = "prefix foaf: <http://xmlns.com/foaf/0.1/>\n";
    private String SQL_PREF = "prefix sql: <http://ns.inria.fr/ast/sql#>\n";
    private String NL = System.getProperty("line.separator");

    public IDatatype getSqrt(Object dist) {
        IDatatype dt = (IDatatype) dist;
        Double distSqrt = Math.sqrt(dt.doubleValue());
        return DatatypeMap.newInstance(distSqrt);
    }

    class MyBuild extends BuildImpl {

        MyBuild(Graph g) {
            super(g);
        }

        public String getID(String b) {
            return b;
        }
    }

    //@BeforeClass
    static public void init() {
       // QueryProcess.definePrefix("c", "http://www.inria.fr/acacia/comma#");

    }


    
     public void testBindIndex() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        
        String q = "select "
                + "(us:test(3, 3) as ?t) "
                + "where {}"
                
                + "function us:test(?n,?n)  {xt:display('here') ;"
                + "let (?n = ?n) {"
                + "for (?x in (xt:iota(?n))) {"
                + "if (?x > 1) {"
                + "return (?x)}"
                + "} "
                + "}"
                + "}"
                
               ;
        
        Mappings map = exec.query(q);
        System.out.println(map);
        Assert.assertEquals(0, map.size());
     }
    
     IDatatype getValue(Mappings m, String name){
         return (IDatatype) m.getValue(name);
     }
     
     public static void main(String[] args) {
        try {
            new TestUnit().testsort();
        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (LoadException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
     }
     
     
 
    public void testExtFun18() throws EngineException {
        
        String init = "prefix ex: <http://example.org/> "
              + "insert data {"
              + "ex:John rdf:value 1 ; rdfs:label 2"
              + "}";
        
         String q = "prefix tr: <http://ns.inria.fr/sparql-datatype/triple#>"
                 +  "prefix gr: <http://ns.inria.fr/sparql-datatype/graph#>"
                 + "select (us:foo() as ?t) "
                 + "where {}"
                 
                 + "function us:foo() {"
                 + "query(construct where {?x ?p ?y}) = query(construct where {?x ?p ?y})"
                 + "}"
                 
                 + "function gr:equal(?g1, ?g2) {xt:display('gr: ', ?g1, ?g2);"
                 + "  mapevery(lambda(?t1, ?t2) { ?t1 = ?t2 }, ?g1, ?g2)"
                 + "}"
                 
                 + "function tr:equal(?t1, ?t2) {xt:display('tr: ', ?t1, ?t2);"
                 + "  gr:equal(?t1, ?t2)"
                 + "}"
                 
                 ;
         
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init); 
        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?t");
        assertEquals(true, dt.booleanValue());
    }    
    
    
    
    
    public void testfun() {
        Date d1 = new Date();
        int n = 100000000;
        MyTest t = new MyTest();
        for (int i = 0; i < n; i++) {
            t.test(1, 2, 3, 4);
        }
        Date d2 = new Date();
        System.out.println("Time: " + (d2.getTime() - d1.getTime()) / (1000.0));
    }
    
    void test(int a, int b, int c, int d) {
        fun(a, b, c, d);
    }
    void fun(int a, int b, int c, int d) {
        bar(a, b, c,  d);
    }
    
    void bar(int a, int b, int c, int d) {
    }
    
    
    class MyTest extends TestUnit {

        
    }
    
    
     public void testNewEval() throws EngineException, LoadException {
        String q = "select (aggregate(let(?y = ?x){?y}) as ?list) where {"
                + "values ?x {unnest(xt:iota(5))}"
                + "}";
        
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        System.out.println(map);
     }
     
     
    
    public IDatatype fun(IDatatype dt1, IDatatype dt2) {
        String str = concat(dt1, dt2);
        return DatatypeMap.newLiteral(str);
    }

    String concat(IDatatype dt1, IDatatype dt2) {
        return dt1.stringValue()+ "." + dt2.getLabel();
    }

    

    
     IDatatype getValue(Mapping map, String name) {
        return (IDatatype)map.getValue(name);
    }
    
    

    public void testapply3() throws EngineException {
        QueryProcess exec = QueryProcess.create(Graph.create());
                
        String q = "select (us:test() as ?t) where {"
                + "}"
                
                + "function us:test(){"
                + "let (?funcall = rq:funcall, ?map = rq:maplist, ?apply = rq:reduce, ?plus  = rq:plus, "
                + "?funlist = @(rq:plus rq:mult)){"
                + "funcall(?funcall, ?apply, ?plus, funcall(?map, ?apply, ?funlist, xt:list(xt:iota(5))))"
                + "}"
                + "}"               
                ;
                
        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?t");
        assertEquals(135, dt.intValue());
    }    
    
 
     
     public void testfocus() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select (us:foo() as ?f) where {"
                + ""
                + "}"
                
                + "function us:foo() {"
                + "let (?g = construct {us:John rdfs:label 'John'} where {}) {"
                + "xt:focus(?g, us:bar())"
                + "}"
                + "}" 
                
                + "function us:bar() {"
                + "let (select (count(*) as ?c) where {?x ?p ?y}) { ?c }"
                + "}"
                
                ;
        Mappings map = exec.query(q);
        IDatatype dt = getValue(map, "?f");
        assertEquals(1, dt.intValue());
     }
     
    public void testLetQuery() throws EngineException {
        String init
                = "insert data {"
                + "us:John rdf:value 1"
                + "}";
       
       // 1000 000 = 5.148  vs 2.923 sans fun

        String q = "select ?t "
                //+ "(us:pattern(?t) as ?b) where "
                + "{"
                + "values ?t { unnest(xt:iota(1000000)) }"
                //+ "filter exists {?x ?p ?t}"
                + "filter us:pattern(?t) "
               
                + "}"
                
                + "function us:pattern(?v){"
                + "exists { ?x ?p ?v}"
               
                + "}";

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Date d1 = new Date();
        Mappings map = exec.query(q);
        Date d2 = new Date();
        System.out.println("Time: " + (d2.getTime() - d1.getTime()) / (1000.0));        
        System.out.println(map.size());
    }
    
    
    
 
  public void testagg() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select "
                + "(count(*) as ?cc)"
                + "(st:agg_and(?n = 0) as ?b) "
                + "(count(?n) as ?c) (avg(?n) as ?avg) (sum(?n) as ?sum) "
                + "(min(?n) as ?min) (max(?n) as ?max) (sample(?n) as ?sam) "
                + "(group_concat(?n ; separator = ' ; ') as ?gr) "
                + "where {"
                + "values ?n { unnest(xt:iota(5)) }"
                + "}";
        
        Mappings map = exec.query(q);
        System.out.println(map);
 }
 
  
     public void testbnode() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        
        String q = "select * where {"
               
                + "bind (xt:debug(true) as ?d)"
                + "bind (us:test(1, 2) as ?t)"
                + "bind(bnode('a') as ?b1)"
                + "bind(bnode('a') as ?b2)"
                + "bind(bnode() as ?b)"
                + "}"
                
                + "function us:test(?x, ?y) {"
                + "us:fun(?x, ?y)"
                + "}"
                
                 + "function us:fun(?y, ?z) {"
                + ""
                + "}"
               ;
        
        Mappings map = exec.query(q);
        System.out.println(map);
        for (Mapping m : map){
            assertEquals(true, m.getValue("?b1") == m.getValue("?b2"));
            assertEquals(false, m.getValue("?b1") == m.getValue("?b3"));
            
        }
        //Assert.assertEquals(0, map.size());
     }
 
 
   public void testsort() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        QueryLoad ql = QueryLoad.create();
        Load ld = Load.create(g);
        ld.parse("/home/corby/NetBeansProjects/corese-github/kgengine/src/test/resources/data/test/human.rdfs");
        String q = ql.readWE(
                "/home/corby/NetBeansProjects/corese-github/kgengine/src/test/resources/data/query/sort.rq");
        // String q2 = ql.readWE(data + "junit/query/method2.rq");

        Mappings map = null;
        System.out.println("warm up");
        //Interpreter.testNewEval = true;
        map = exec.query(q);
        
        int n = 10;
        long total = 0;
        for (int i = 0; i < n; i++) {
           Date d1 = new Date();
           map = exec.query(q);
           Date d2 = new Date();
           long time = d2.getTime() - d1.getTime();
           System.out.println(i + " : " + (time) / (1000.0));
           total += time;
       }
        System.out.println(map);
        System.out.println("Time: " + (total) / (n * 1000.0));

        String q2 = "select (us:fun(2) as ?t) where {}"
                + ""
                + "@trace "
                + "function us:fun(?y) {"
                + "let (select * where { ?x a ?y }) {"
                + "bound(?y)"
                + "}"
                + "}";

    }
    
}