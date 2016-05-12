package junit;

import fr.inria.edelweiss.kgtool.util.GraphListen;
import fr.inria.edelweiss.kgtool.util.QueryGraphVisitorImpl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.time.StopWatch;
import org.xml.sax.SAXException;

import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.cg.datatype.CoreseDate;
import fr.inria.acacia.corese.cg.datatype.CoreseStringLiteral;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;

import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.storage.api.IStorage;
import fr.inria.acacia.corese.storage.api.Parameters;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Atom;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.parser.ParserSparql1;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgengine.GraphEngine;
import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.Metadata;
import fr.inria.acacia.corese.triple.parser.Option;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.edelweiss.kgenv.eval.QuerySolver;
import fr.inria.edelweiss.kgenv.parser.ExpandPath;
import fr.inria.edelweiss.kgenv.result.XMLResult;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.api.QueryGraphVisitor;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.core.ValueResolverImpl;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.logic.RDF;
import fr.inria.edelweiss.kgraph.logic.RDFS;
import fr.inria.edelweiss.kgraph.query.QueryEngine;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.query.QueryGraph;
import fr.inria.edelweiss.kgraph.rule.Rule;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.BuildImpl;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.transform.Transformer;
import fr.inria.edelweiss.kgtool.print.RDFFormat;
import fr.inria.edelweiss.kgtool.print.ResultFormat;
import fr.inria.edelweiss.kgtool.print.TemplateFormat;
import fr.inria.edelweiss.kgtool.transform.TemplatePrinter;
import fr.inria.edelweiss.kgtool.print.TripleFormat;
import fr.inria.edelweiss.kgtool.print.XMLFormat;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.GListener.Operation;
import fr.inria.acacia.corese.triple.printer.SPIN;
import fr.inria.edelweiss.kgenv.api.QueryVisitor;
import fr.inria.edelweiss.kgtool.util.QueryManager;
import fr.inria.edelweiss.kgenv.parser.ExpandList;
import fr.inria.edelweiss.kgram.api.core.DatatypeValue;
import fr.inria.edelweiss.kgram.core.Memory;
//import fr.inria.edelweiss.kgram.core.*;
import fr.inria.edelweiss.kgram.filter.Interpreter;
import fr.inria.edelweiss.kgraph.api.GraphListener;
import fr.inria.edelweiss.kgraph.api.Loader;
import fr.inria.edelweiss.kgraph.core.EdgeImpl;
import fr.inria.edelweiss.kgraph.core.GraphStore;
import fr.inria.edelweiss.kgraph.core.Index;
import fr.inria.edelweiss.kgraph.core.NodeImpl;
import fr.inria.edelweiss.kgraph.query.MatcherImpl;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import fr.inria.edelweiss.kgraph.query.ProviderImpl;
import fr.inria.edelweiss.kgtool.load.ExtensionFilter;
import fr.inria.edelweiss.kgtool.load.LoadFormat;
import fr.inria.edelweiss.kgtool.load.RuleLoad;
import fr.inria.edelweiss.kgtool.print.JSONLDFormat;
import fr.inria.edelweiss.kgtool.transform.DefaultVisitor;
import fr.inria.edelweiss.kgtool.transform.TemplateVisitor;
import fr.inria.edelweiss.kgtool.util.GraphStoreInit;
import fr.inria.edelweiss.kgtool.util.SPINProcess;
import fr.inria.edelweiss.kgtool.util.GraphUtil;
import fr.inria.edelweiss.kgtool.util.ValueCache;
import fr.inria.corese.kgtool.workflow.Data;
import fr.inria.corese.kgtool.workflow.ParallelProcess;
import fr.inria.corese.kgtool.workflow.WorkflowParser;
import fr.inria.corese.kgtool.workflow.SemanticWorkflow;
import fr.inria.corese.kgtool.workflow.WorkflowProcess;
import fr.inria.corese.kgtool.workflow.WorkflowVisitor;
import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import static junit.TestQuery1.data;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TestUnit {

    //static String data = "/home/corby/NetBeansProjects/kgram/trunk/kgengine/src/test/resources/data/";
    static String data = TestUnit.class.getClassLoader().getResource("data").getPath() + "/";
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
        QueryProcess.definePrefix("c", "http://www.inria.fr/acacia/comma#");
        //QueryProcess.definePrefix("foaf", "http://xmlns.com/foaf/0.1/");
        graph = start();
       
    }
    
    static Graph start(){
         Graph g = Graph.create(true);
        //graph.setOptimize(true);
        System.out.println("load");
        try {
            init(g);
        } catch (LoadException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }
        g.init();
        return g;
    }

    static void init(Graph g) throws LoadException {
        Load ld = Load.create(g);
        ld.parse(data + "kgraph/rdf.rdf", RDF.RDF);
        ld.parse(data + "kgraph/rdfs.rdf", RDFS.RDFS);
        ld.parse(data + "comma/comma.rdfs");
        ld.parse(data + "comma/commatest.rdfs");
        ld.parse(data + "comma/model.rdf");
       // ld.loadWE(data + "comma/testrdf.rdf");
        ld.parseDir(data + "comma/data");
        ld.parseDir(data + "comma/data2");
        
    }

    // 1.319
    void init2(Graph g) throws LoadException {
        Load ld = Load.create(g);
//        ld.loadWE(data + "cdn/data");
//        ld.loadWE(data + "template/spin/data");
//        ld.loadWE(data + "template/sql/data");
//        ld.loadWE(data + "work/owlrl");
        ld.parseDir(data + "template/owl/data");

    }
    
    
      public static void main2(String[] args) throws EngineException{
          new TestUnit().testFib();
      }
      
   
    public void testFunodfh() throws EngineException {
        String q =  "select * (max(?c) as ?mc) where {"
                + "select (count(*) as ?c) (max(?y) as ?m) where {?x ?p ?y}"
                + "}";
              
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);       
        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?c");
        IDatatype dt2 = (IDatatype) map.getValue("?mc");
        assertEquals(0, dt.intValue());
        assertEquals(0, dt2.intValue());
    }
    
    
   
     
    
    public void testServer29() throws EngineException, LoadException {
        String q = QueryLoad.create().readWE("/home/corby/AAServer/data/query/function.rq");
        QueryProcess exec = QueryProcess.create(Graph.create());
        exec.query(q);
        
        WorkflowParser wp = new WorkflowParser();
        SemanticWorkflow sw = wp.parse(data + "junit/workflow/w2/w29.ttl");        
        Data res = sw.process();
 
       assertEquals(8062, res.getTemplateResult().length());
        System.out.println(res);
        System.out.println(res.getTemplateResult().length());
    }
    
    @Test
    public void testServer1() throws EngineException, LoadException {
        String q = QueryLoad.create().readWE("/home/corby/AAServer/data/query/function.rq");
        QueryProcess exec = QueryProcess.create(Graph.create());
        exec.query(q);
        
        WorkflowParser wp = new WorkflowParser();
        SemanticWorkflow w = wp.parse(data + "junit/workflow/server/w2.ttl", NSManager.USER + "test");
        w.setVisitor(new MyWorkflowVisitor());
        Data res = w.process();
        System.out.println(res);
    }
    
    class MyWorkflowVisitor implements WorkflowVisitor {

        @Override
        public void before(WorkflowProcess wp, Data data) {
            System.out.println("Before: " + wp.getClass().getName());
        }

        @Override
        public void after(WorkflowProcess wp, Data data) {
             System.out.println("After: " + wp.getClass().getName());           
        }
    
    }
     
    
     public void testServer30() throws EngineException, LoadException {
        String q = QueryLoad.create().readWE("/home/corby/AAServer/data/query/function.rq");
        QueryProcess exec = QueryProcess.create(Graph.create());
        exec.query(q);
        
        WorkflowParser wp = new WorkflowParser();
        SemanticWorkflow w = wp.parse(data + "junit/workflow/w2/w30.ttl");
        Data res = w.process();
        System.out.println(res);
    }
    
     public void testServer3() throws EngineException, LoadException {
        String q = QueryLoad.create().readWE("/home/corby/AAServer/data/query/function.rq");
        QueryProcess exec = QueryProcess.create(Graph.create());
        exec.query(q);
        
        WorkflowParser wp = new WorkflowParser();
        SemanticWorkflow w = wp.parse(data + "junit/workflow/server/w3.ttl");
        Data res = w.process();
        System.out.println(res);
    }
     
    public void testGGGG() throws EngineException, LoadException {         
        String q = "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select distinct ?x  (count (?src) as ?count) (max(?src) as ?max)   where { "
                + "?src rdf:type rdfs:Resource   "
                + "graph ?src { ?x c:hasCreated ?y }}"
                + "group by ?x "
                + "order by desc(?max)"
                + "having (?count >= 0)";
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parseDir(data + "comma/data");
        QueryProcess exec = QueryProcess.create(g);       
        Mappings map = exec.query(q);
         System.out.println(map);
    }
     
     
     
    public void testWorkflowMeta() throws EngineException, LoadException {
        SemanticWorkflow sw =  new SemanticWorkflow();
        ParallelProcess p = new ParallelProcess();
        
        for (int i = 1; i <= 26; i++) {
            if (i == 11 || i == 12){
                continue;
            }
            WorkflowParser wp = new WorkflowParser();
            SemanticWorkflow w = wp.parse(data + "junit/workflow/w2/w" + i + ".ttl");
            w.setDebug(false);
            w.setRecDisplay(false);
            p.insert(w);
        }
        sw.add(p);       
      
        Data res = sw.process();       
      
   }  
    
    
   public void testWorkflow26() throws EngineException, LoadException {
       WorkflowParser wp = new WorkflowParser();
       SemanticWorkflow w = wp.parse(data + "junit/workflow/w2/w26.ttl");           
       Data res = w.process(new Data(GraphStore.create()));
       System.out.println(res.isSuccess());
      
   }  
      
  
        
      public void testMI() throws EngineException{
          String i = "insert data {"
                  + "us:Boy rdfs:subClassOf us:Man "
                  + "us:Man rdfs:subClassOf us:Person "

                  + "us:John a us:Boy, us:Man "
                  + "}";
          
          String q = "select * where { ?x a us:Person }";
          
          Graph g = Graph.create(true);
          QueryProcess exec = QueryProcess.create(g, true);
          exec.query(i);
          Mappings map = exec.query(q);
          System.out.println(map);
        }
      
      
     
      
  
           
           
      public void testNSMaa() throws EngineException, IOException {
         NSManager nsm = NSManager.create();
         nsm.setBase("file:///home/corby/");
         String str = nsm.toNamespaceB("/test.ttl");
         System.out.println(str);
     }
      
    
    //01 ?x = c:Icon c:superClassOf c:Icon
    public void testbugdfg() throws EngineException, LoadException {
        Graph g = Graph.create(true);
        init(g);
        String q = "select * where {?x ?p ?y optional{?y rdf:type ?class}"
                + " filter (! bound(?class) && ! isLiteral(?y))}";

        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        System.out.println(map);
        System.out.println(map.size());
        
        
        Entity ent =null;
        Node name = ent.getGraph();
        name.equals(Entailment.RULE);
        name.equals(Entailment.ENTAIL);
    }
    
    
    
    
    
   public void testWorkflow() throws EngineException, LoadException {
//       WorkflowParser wp = new WorkflowParser();
//       WorkflowProcess w = wp.parse("/home/corby/AATest/junit/workflow/workflow.ttl");
       
       Load ld = Load.create(Graph.create());
       ld.parse("/home/corby/AATest/junit/workflow/workflow.sw");
        SemanticWorkflow w = ld.getWorkflow();
       
       System.out.println(w);
       w.setDebug(true);
       Data res = w.process();
       System.out.println(res);
       
   }
   
   
    
    
    
    
   public void testWorkflow11() throws EngineException, LoadException {
       WorkflowParser wp = new WorkflowParser();
       SemanticWorkflow w = wp.parse(data + "junit/workflow/w2/w9.ttl");
       Data res = w.process();       
        assertEquals(false, res.getDatatypeValue().booleanValue());
        System.out.println(res.getDatatypeValue());
   } 
   
    public void testWorkflow2() throws EngineException, LoadException {
        SemanticWorkflow sub = new SemanticWorkflow()
                .addQuery("insert  { "
                + "us:John rdf:value ?val ; st:loop ?loop ; rdfs:label 'Jon' ;"
                + "foaf:knows []} "
                + "where { bind (st:get(st:index) as ?val) bind (st:get(st:loop) as ?loop)}")
                ;
        
        SemanticWorkflow w = new SemanticWorkflow()
                .addQuery("construct where {}")
                .add(sub)
                .addTemplate(Transformer.TURTLE)
                ;
        
        Context c = new Context()
                .export(Context.STL_TEST, DatatypeMap.newInstance(5)); 
        //sub.setContext(c);
        sub.setLoop(5);
        sub.setContext(new Context());
        w.setContext(c);
        Graph g = Graph.create();
        Data res = w.process(new Data(g));        
        assertEquals(12, res.getGraph().size());
       System.out.println(w);
       System.out.println(w.getData());
    }
   
    
    public void testJSON() throws EngineException, LoadException {
        String t =
                "template { st:apply-templates-with(st:json) }"
                + "where {}";

        Graph g = Graph.create(); //createGraph();
        Load ld = Load.create(g);
        ld.parse(data + "jsonld/test.jsonld");

        QueryProcess exec = QueryProcess.create(g);

        Mappings map = exec.query(t);

        String json = map.getTemplateStringResult();
         System.out.println(json);
        assertEquals(1210, json.length());

        Graph gg = Graph.create();
        Load ll = Load.create(gg);
        ll.loadString(json, Load.JSONLD_FORMAT);

        assertEquals(g.size(), gg.size());

    }
   
    public void testDQP() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        
        String q = "select * where {"
                + "?x ?p ?y"
                + "}"
                
                + "function xt:produce(?q){"
                + "let ((?s, ?p, ?o) = ?q, "
                + "      ?g = st:get(st:endpoint)){"
                + "service ?g {}"
                + "}"
                + "}"
                ;
        
   }
   
    public void testContext3() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select * "
                + "where {"
                + "values (?key ?value) { unnest(xt:context()) }"
                //+ "filter st:get(st:type, 'API')" 
                + "}"
                
                + "function us:test(){"
                + "for ((?key, ?val) in xt:context()){"
                + "xt:display(?key, ?val)}"
                + "}";

        Context c = new Context().setName("date", DatatypeMap.newDate());
        c.setName("type", DatatypeMap.newInstance("GUI"));
        exec.setMetadata(new Metadata().add(Metadata.DISPLAY, Metadata.DISPLAY_RDF));
        Mappings map = exec.query(q, c);
       
        System.out.println(map);
        ResultFormat r = ResultFormat.create(map);
        //System.out.println(r);
    }
    
   
    
    
    
    
    
    
    
    
    
    
      public void testDSC() throws EngineException, IOException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        
        String i = "insert data {us:John rdfs:label 'Jon'}";
        String q = "select "
                + "(st:apply-templates-with(st:turtle) as ?b)"
                + "(st:apply-templates-with(st:owl) as ?o)"
                + "(st:get(st:test) as ?t) where {"
                + ""
                + "}";
        
        exec.query(i);
        Context c = new Context().export(NSManager.STL+"test", DatatypeMap.newInstance(true));
        Mappings map = exec.query(q, new Dataset().set(c));
        System.out.println(map);
     }
    
    
    
     public void testDataset2() throws EngineException, IOException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select (us:test() as ?t)  "
                + "from kg:default "
                + "from named kg:test "
                + "where {}"
                               
                + "function us:test(){"
                + "for (?g in xt:from()){xt:display(?g)} ;"
                + "for (?g in xt:named()){xt:display(?g)}"
                + "}";
        
        Mappings map = exec.query(q);
        
     }
    
    
    public void testGLoop() throws EngineException, IOException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select (us:test() as ?t)"
                + "where {}"
                + ""
                + "function us:test(){"
                + "loop ((?s, ?p, ?o) in construct {us:John rdfs:label 'Jon', 'Jim' } where {}){"
                + "?o"
                + "}"
                + "}";

        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype)map.getValue("?t");
        assertEquals("JonJim", dt.stringValue());
    }
      
      
    public void testIO() throws EngineException, IOException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        Load ld = Load.create(g);
        ld.setDebug(true);
        try {            
            ld.parse(data + "junit/data/test.ttl");
            ld.parse(data + "junit/data/test.ttl", "http://example.org/");
            
            ld.parse(data + "junit/data/test.rdf");
            ld.parse(data + "junit/data/test.rdf", "http://example.org/");
            
            ld.parse(data + "junit/data/test.xml", Load.RDFXML_FORMAT);
            
        } catch (LoadException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertEquals(5, g.size());
        System.out.println(g.display());
    }
     
   
     
      public void testFFFF() throws EngineException, LoadException {
         LoadFormat f = new LoadFormat();
         assertEquals(Loader.RDFXML_FORMAT, f.getFormat("test.rdf"));
         assertEquals(Loader.TURTLE_FORMAT, f.getFormat("test.ttl"));
     }
     
    public void testPPSPIN() throws EngineException, LoadException {
        Graph g = createGraph();
        Load ld = Load.create(g);
        //System.out.println("Load");
        ld.parseDir(data + "template/spinhtml/data/");
        QueryProcess exec = QueryProcess.create(g);

        String t1 = "prefix f: <http://example.com/owl/families/> "
                + "template  {  st:apply-templates-with(st:spin)}"
                + "where {}";
        
        String t2 = "prefix f: <http://example.com/owl/families/> "
                + "template  {  st:apply-templates-with('/home/corby/AAData/sttl/spin')}"
                + "where {}";


        Mappings map = exec.query(t1);
        //assertEquals(3060, map.getTemplateResult().getLabel().length());
         System.out.println(map.getTemplateStringResult());
    }
     
     
     
     
     
     
     
    public void testIO2() throws EngineException, IOException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        Load ld = Load.create(g);
        ld.setDebug(true);
        try {
            ld.parse(new File(data + "junit"), new ExtensionFilter(), null,true);                      
        } catch (LoadException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }
        //assertEquals(4, g.size());
        System.out.println(g.display());
    }
      
      
    public void testLP() throws EngineException, IOException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        Load ld = Load.create(g);
        ld.setDebug(true);
        try {
            ld.parse(NSManager.RDF);                      
                        
        } catch (LoadException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println(g.display());
    } 
    
    
       
    public void testIO3() throws EngineException, IOException, LoadException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        Load ld = Load.create(g);
        
        String test = "<John> rdfs:label 'John' ";
        
        ld.loadString(test, Load.TURTLE_FORMAT);
        ld.loadString(test, "http://example.org/", Load.TURTLE_FORMAT);
        ld.loadString(test, "http://example.org/path/", "http://example.org/source/", "http://example.org/base/", Load.TURTLE_FORMAT);
        
        //ld.loadResource("data/junit/data/test.ttl", Load.TURTLE_FORMAT);
        
        System.out.println(g.display());
        
       }
      
      
    public void testRelax1() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        String i = "insert data { "
                + "us:John foaf:name 'John'@en ; foaf:age 11 "
                + "}";
        exec.query(i);

        String q = "@test st:test " +
                 "select * (sim() as ?s) (us:test() as ?t) where {"
                + "us:John foaf:name 'Jon' ; foaf:age 11 "
                + "values ?m {unnest(xt:metadata())}"
                + "filter us:test()"
                + "}"
                + ""
                + "function us:test(){"
               // + "xt:display(xt:metadata());"
                + "mapany(rq:equal, '@test', xt:metadata())"
                + "}";
        
        
        exec.set(new Metadata()
                .add(Metadata.RELAX, Metadata.RELAX_LITERAL)
                //.add(Metadata.DEBUG)
                //.add(Metadata.SERVICE, "http://fr.dbpedia.org/sparql")
                );
          //System.out.println(exec.getMetadata());
        Mappings map = exec.query(q);
        //assertEquals(1, map.size());
        System.out.println(map);
        System.out.println(map.size());
        //System.out.println(map.getQuery().getAST());
    }
       
      
         
       public void testSameas() throws EngineException{
           Graph g = GraphStore.create();                     
           QueryProcess exec = QueryProcess.create(g);
                                
           String i = "insert data { "
                   + "us:John rdfs:label 'John' "
                   + "us:Jack rdfs:label 'Jack' "
                   + "us:John owl:sameAs us:Jack "
                   + "}";
          exec.query(i);  
          
          g.sameas();
                    
          
          String q = "select * where {"
                  + "?x rdfs:label ?l1, ?l2 "
                  + "filter (?l1 != ?l2) "
                  + "}";
          
          Mappings map = exec.query(q);   
           System.out.println(map);
       }
       
       
      
         
          public void testUnnestGraph() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        QueryProcess.definePrefix("c", "http://www.inria.fr/acacia/comma#");

        String q = "prefix ex: <htp://example.org/>"
                + "select ?s ?p ?o  where {"
                + "bind (us:test() as ?g)"
                + "values (?s ?p ?o) {unnest(?g)}"
                + "graph ?g {?s ?p ?o}"
                + "}"
                + "function us:test(){"
                + "let (?g = construct { us:John rdfs:label 'John', _:b2 } where {})"
                + "{?g}"
                + "}";
        
        Mappings map = exec.query(q);
              System.out.println(map);
             
//        assertEquals(1, map.size());
//        IDatatype dt = (IDatatype) map.getValue("?x");
//        assertEquals(1, dt.intValue());
    }
          
          
          
          
          
          public void testUnnestSelect() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
     

        String q = "prefix ex: <htp://example.org/>"
                + "select *  where {"
                + "values (?x ?y ?path) {unnest(us:test())}"
                + "graph ?path { ?a ?p ?b }"
                + "}"
                + "function us:test(){"
                + "let (?m = select * where {"
                + "bind (us:graph() as ?g)"
                + "graph ?g {?x foaf:knows+ :: ?path  ?y}"
                + "})"
                + "{?m}"
                + "}"
                
                +"function us:graph(){"
                + "let (?g = construct  {"
                + "us:John foaf:knows us:Jack "
                + "us:Jack foaf:knows us:Jim} where {})"
                + "{?g}"
                + "}"
                
                ;
        
        Mappings map = exec.query(q);
              System.out.println(map);
        assertEquals(4, map.size());
//        IDatatype dt = (IDatatype) map.getValue("?x");
//        assertEquals(1, dt.intValue());
    }
          
          
          
    public void testUnnestNSM() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        QueryProcess.definePrefix("c", "http://www.inria.fr/acacia/comma#");

        String q = "prefix ex: <htp://example.org/>"
                + "select *  where {"
                + "values (?p ?n) {unnest(st:prefix())}"
                + "}"
                + "function us:test(){"
                + "for ((?p, ?n) in st:prefix()){"
                + "xt:display(?p, ?n)"
                + "}"
                + "}";

        Mappings map = exec.query(q);
             System.out.println(map);
        
    }
    
    
    
       public void testEventCall() throws EngineException{
           Graph g = GraphStore.create();                     
           QueryProcess exec = QueryProcess.create(g);
           String q = "select * where {"
                   + "bind (st:set(st:test, 'test') as ?test) "
                   + "values (?key ?val) { unnest(xt:context()) } "                   
                   + "}"                                                                                    
                   + "function xt:start(?q){ st:set(st:count, 0)  } " 
                
                   ;
           
           Mappings map = exec.query(q);
           assertEquals(2, map.size());          
           System.out.println(map);                   
     }
     
       public void testGraphUnnestlsdhfshfl() throws EngineException{
           Graph g = GraphStore.create();                     
           QueryProcess exec = QueryProcess.create(g);
           
           String q = "select * where {"
                   + "bind (us:graph() as ?g)"
                   + "graph ?g {"
                   + "?x ?p ?y "
                   + "?y ?q ?z"
                   + "}"
                   + "}"
                   
                   
                   +"function us:graph(){"
                   + "let (?g = construct {?s ?p ?o} where {values (?s ?p ?o) {unnest(xt:query())}})"
                   + "{?g}"
                   + "}"
                   
                   + "function xt:produce(?q){"                  
                   + "xt:query()"
                   + "}"
                   ;
           
           Mappings map = exec.query(q);
           System.out.println(map);
           
     }
    
         
         
       public void testGraphUnnest() throws EngineException{
           Graph g = GraphStore.create();                     
           QueryProcess exec = QueryProcess.create(g);
           Graph g1 = Graph.create();                     
           QueryProcess exec1 = QueryProcess.create(g1);
                     
           String i = "prefix ex: <htp://example.org/>"
                   + "insert data { "
                   + "us:John rdfs:label 'John'"
                   + "ex:John rdfs:label 'John'"
                   + "}";
                     
           String q = "prefix ex: <htp://example.org/>"
                   + "select * "
                   + "where {"
                   + " filter us:check() "
                  
                   + "?x foaf:name ?y ; ?p ?z "
                   
                   + "filter mapany(us:test, ?x, st:prefix())"
                  
                   + "}"
                 
                   + "function us:check(){"
                   + "let (?q = xt:query()){"
                   + "for (?t in ?q){"
                   + "let ((?s, ?p, ?o) = ?t){"
                   + "if (isURI(?p) && not exists {?x ?p ?y}){"
                   + "xt:display('Missing: ', ?t)}"
                   + "}}}"
                   + "}"
                   
                   + "function us:trace() {"
                   + "for (?def in st:prefix()){"
                   + "xt:display(?def)"
                   + "}"
                   + "}"
                   
                   +"function us:test(?uri, ?def){"
                   + "xt:display(?def);"
                   + "let ((?p, ?n) = ?def){"
                   + "strstarts(?uri, ?n)"
                   + "}"
                   + "}"
                                     
                   ;
           
           String q2 = "prefix ex: <htp://example.org/>"
                   +"select (us:test() as ?t) where {}"
                   
                   + "function us:test(){"
                   + "maplist (rq:self, st:prefix())"
                   + "}"
                   ;
                      
           exec.query(i);                           
           Mappings map = exec.query(q);           
            System.out.println(map);
       } 
       
       
        public void testValuesExp() throws EngineException, LoadException{
           Graph g = GraphStore.create();                     
           QueryProcess exec = QueryProcess.create(g); 
           String q = "select * (us:test(?t) as ?d) where {"
                   + "values ?t { unnest(let (?g = construct {us:John rdfs:label 'John', 'Johnny'} where {}){ ?g }) }"
                   + "}"
                   
                   +"function us:test(?t){"
                   + "for (?e in ?t){xt:display(?e)} ;"
                   + "let ((?s, ?p, ?o) = ?t){xt:display(?s)};"
                   + "let ((?s, ?p, ?o, ?g) = ?t){xt:display(?s, ?g)}"
                   + "}";
           
           Mappings map = exec.query(q);
           System.out.println(map);
           System.out.println(map.getQuery().getAST());
           
       }
       
        public void testPointer() throws EngineException, LoadException{
           Graph g = GraphStore.create();                     
           QueryProcess exec = QueryProcess.create(g); 
          
           String i = "insert data {"
                   + "us:John rdfs:label 'John', 'Johnny' "
                   + "}";
           
           String qe = 
                   "select * where {"
                   + "bind (unnest(us:construct()) as (?x, ?p, ?y))"
                   + "?x ?p ?y "
                   + "}"
                   
                   +"function us:construct(){"
                   + "let (?g = construct where {?x ?p ?y}){"
                   + "?g}"
                   + "}" 
                   
                   + "function us:select(){"
                   + "let (?s = select * where { bind(unnest(xt:iota(5)) as ?e) }){"
                   + "for (?m in ?s){"
                   + "map (xt:display, ?m)"
                   + "}}}"                  
                  ;                    
           
           exec.query(i);
           Mappings map = exec.query(qe);
           System.out.println(map);
           
           
           String q = "select * where {"
                   + "bind (unnest(us:cons()) as ?t)"
                   + "bind (unnest(?t) as (?s, ?p, ?o))"
//                   + "bind (us:select() as ?s)"
//                   + "bind (unnest(?s) as (?x, ?y))"
                   + "}"
                   
                   + "function us:cons(){"
                   + "let (?g = construct {us:John rdfs:label 'John', 'Johnny'} where {})"
                   + "{?g}"
                   + "}"
                   
                   + "function us:select(){"
                   + "let (?s = select * where { "
                   + "bind(unnest(xt:iota(5)) as ?e) "
                   + "bind (?e + 1 as ?f)"
                   + "})"
                   + "{?s}"
                   + "}"
                   ;
           
//           Mappings map = exec.query(q);
//           System.out.println(map);
           
       }
      
         
        public void testListenqsdfgq() throws EngineException, LoadException{
           Graph g = GraphStore.create();                     
           QueryProcess exec = QueryProcess.create(g); 
           g.addListener(new GL());
           Load ld = Load.create(g);
           ld.loadWE(RDF.RDF);
           
           String i = "insert data {us:John rdfs:label 'John'}";
           
           String u = "delete  where {?x a ?t}";
           
           exec.query(i);
           
           exec.query(u);

        }
        
        class GL implements GraphListener {

        @Override
        public void addSource(Graph g) {
        }

        @Override
        public boolean onInsert(Graph g, Entity ent) {
            return true;        
        }

        @Override
        public void insert(Graph g, Entity ent) {
            System.out.println("Insert: " + ent);     
        }

        @Override
        public void delete(Graph g, Entity ent) {
            System.out.println("Delete: " + ent);     
        }

        @Override
        public void start(Graph g, Query q) {
        }

        @Override
        public void finish(Graph g, Query q, Mappings m) {
        }

        @Override
        public void load(String path) {
            System.out.println("Load: " + path);     
        }
        
        }
        
        public void testNewfghdfg() throws EngineException{
           Graph g = GraphStore.create();                     
           QueryProcess exec = QueryProcess.create(g);
           
           String q = "select * "
                   + "(us:test() as ?t) "
                   + "where {}"
                   + ""
                   + "function us:test(){"
                   + "mapfun(rq:concat, rq:self, xt:add(xt:iota(5), 6))"
                   + "}";
           
           Mappings map  = exec.query(q);
           
            System.out.println(map.getQuery().getAST());
            System.out.println(map);
           
        }
        
       public void testExtNode() throws EngineException{
           Graph g = GraphStore.create();                     
           QueryProcess exec = QueryProcess.create(g);
           Graph g1 = Graph.create();                     
           QueryProcess exec1 = QueryProcess.create(g1);
                     
           String i = "insert data { us:prop rdfs:label 'prop' }";
                     
           String q = "select * where {"
                   + "bind (unnest(us:define()) as (?s, ?p, ?o))"
                 
                   + "}"
                   
                   + "function us:define(){"
                   + "let (?g = construct {us:prop1 rdfs:label 'prop' us:prop rdfs:label 'prop2'} where {})"
                   + "{ "
                   + "?g }"
                   + "}"
                                     
                   ;
                      
           exec.query(i);                           
           Mappings map = exec.query(q);           
            assertEquals(2, map.size());
       }
           
           
           
           
           
           
           
           
      public void testList() throws EngineException{
           Graph g = Graph.create();                     
           QueryProcess exec = QueryProcess.create(g);                
          String q = "select * (group_concat(?elem) as ?e) where {"
                  + "bind (xt:list(xt:list(1, 2), xt:list(3, 4)) as ?list)"
                  + "bind (unnest(?list) as ?sublist)"
                  + "bind (unnest(?sublist) as ?elem)"
                  + "}"
                  + "group by ?sublist";
          
          Mappings map = exec.query(q);
          
            System.out.println(map);
           
        }
      
      
       
        public void testUnion2() throws EngineException{
           Graph g = Graph.create();
           QueryProcess exec = QueryProcess.create(g);

           String q = "function xt:main(){"
                   + "us:test()"
                  
                   + "}"
                  
                   + "function us:union(){"
                   + "let ("
                   + "?g1 = construct {us:John foaf:knows us:Jim}  where {},"
                   + "?g2 = construct {us:Jim  foaf:knows us:Jack} where {}){"
                   + "xt:union(?g1, ?g2)"
                   + "}"
                   + "}"
                   
                   +"function us:test(){"
                   + "let (?g = us:union(),"
                   + "?m = select * where { graph ?g {?x foaf:knows+ ?y}})"
                   + "{ ?m }"
                   + "}"
                   
                   ;
           
           IDatatype dt = exec.eval(q);
           Mappings map =  dt.getPointerObject().getMappings();
           System.out.println(map);
           assertEquals(3, map.size());
       }
       
       
       public void testUnion() throws EngineException{
           Graph g1 = Graph.create();                     
           QueryProcess exec1 = QueryProcess.create(g1);
                     
           String i1 = "insert data {"
                   + "us:John foaf:knows us:Jim "                  
                   + "us:Jim foaf:knows us:Jack "
                   + "}"; 
           
           String q = "function xt:main(){"
                   + " us:test()"
                   + "}"
                                  
                   + "function us:test(){"
                   + "let ("
                   + "?m1 = select * where { ?x foaf:knows ?y },"
                   + "?m2 = select * where { ?z foaf:knows ?t }){"
                   + "xt:display(xt:union(?m1, ?m2));"
                   + "xt:display(xt:join(?m1, ?m2));"
                   + "xt:display(xt:minus(?m1, ?m2));"
                   + "xt:display(xt:optional(?m1, ?m2))"
                   
                   
                   + "}"
                   + "}";
           
           exec1.query(i1);
           
           
         
           IDatatype dt = exec1.eval(q);
                    
//           Mappings m = dt.getPointerObject().getMappings();
//           System.out.println(m.toString(true));
//           System.out.println(m.size());
           
       }
       
       
       
       
       
       public void testFormat(){
           Constant c1 = Constant.create("test");
           System.out.println(c1);
           System.out.println(c1.getDatatypeValue());
           
           Constant c2 = Constant.create("test", null, null);
           System.out.println(c2);
           System.out.println(c2.getDatatypeValue());
           
           c2 = Constant.create("2000-01-01", NSManager.XSD + "date");
           System.out.println(c2);
           System.out.println(c2.getDatatypeValue());
           
            c2 = Constant.create(12);
           System.out.println(c2);
           System.out.println(c2.getDatatypeValue());
       }
       
       
       
       
       
        public void testApprox() throws EngineException, LoadException {
             Graph g = Graph.create(); 
             QueryProcess exec = QueryProcess.create(g);
             
             String i = "insert data {"
                     + "us:John us:name 'John' "
                     + "us:Jack us:name 'Jack' "
                     + "us:Jim  us:name 'Jim' "
                    
                     + "}";
             
             String q = "@relax "
                     + "select * (sim() as ?s) where {"
                     + "?x xt:name 'Jon'"
                     + "}"
                     + "order by desc(?s)";
             
             exec.query(i);
             
             Mappings map = exec.query(q);
            assertEquals(3, map.size());
            IDatatype dt = (IDatatype) map.getValue("?x");
             assertEquals(NSManager.USER+"John", dt.stringValue());
       }
       
       
       
       
       
       
       
       
       
       
       
       
       
       
       
       
       
       
       
       
       
        public void testFuncall() throws EngineException {
             Graph g = createGraph(); 
             QueryProcess exec = QueryProcess.create(g);
             String q = "@debug package {"
                     
                     + "function xt:main(){"
                     + "apply(xt:plus, xt:iota(5));"
                     + "let (?fun = xt:mult){funcall(?fun, 1, 2)}"
                     + "}"
                     + ""
                     + "function us:fun(){"
                     + "let (?list = xt:list(us:foo, us:bar)){"
                     + "map(us:process, ?list)}"
                     + "}"
                     
                     + "function us:process(?fun){"
                     + "let (?r = funcall(?fun)){"
                     + "xt:display(?r)}"
                     + "}"
                     
                     +"function us:foo(){"
                     + "let (?m = select (1 as ?x) where {})"
                     + "{ ?m }"
                     + "}"
                     
                     +"function us:bar(){"
                     + "let (?m = construct {us:John rdfs:label 'John' } where {})"
                     + "{ ?m }"
                     + "}"
                     
                     + "}"
                     ;
                                     
             IDatatype dt = exec.eval(q);
             System.out.println(dt);
             
     }
       
        public void testLoadFun() throws EngineException {
             Graph g = createGraph(); 
             QueryProcess exec = QueryProcess.create(g);
             exec.setLoadFunction(!true);
             String q = "prefix f: <file:///home/corby/AATest/query/fun1#>"
                     + "@import "
                     + "select "
                     + "(f:test() as ?t) "
                     + "(f:foo() as ?f) "
                     + "where {}";
             Mappings map = exec.query(q);
             assertEquals("Hello", strValue(map, "?t"));
             System.out.println(map);
             DatatypeValue[] xx = new IDatatype[0];
     }
       
       
      public void testSubqueryFun() throws EngineException {
             Graph g = createGraph(); 
             QueryProcess exec = QueryProcess.create(g);
             
             String i = "insert data { us:John rdfs:label 'John' }"
                     
                     + "@public package {"
                     + "function us:test(){"
                     + "let ((?y) = select * where {?x ?p ?y}){?y}"
                     + "}"
                     + "}";
             
             String q = "select * (us:test() as ?r) (us:foo() as ?s) "
                     + "where {"
                     + "select * where {select * (us:test() as ?t) (us:foo() as ?f) "
                     + "where {}}"
                     + "}"
                     
                     + "function us:foo(){"
                     + "let ((?b) = select * where {?a ?q ?b}){ us:test() }"
                     + "}";
             
             exec.query(i);
             Mappings map = exec.query(q);
             System.out.println(map);
             System.out.println(map.size());
             assertEquals("John", strValue(map, "?r"));
             assertEquals("John", strValue(map, "?s"));
             assertEquals("John", strValue(map, "?t"));
             assertEquals("John", strValue(map, "?f"));
             
      }
       
      String strValue(Mappings m, String v){
         return ((IDatatype) m.get(0).getValue(v)).stringValue();
      }
      
      public void testImport() throws EngineException {
             Graph g = createGraph(); 
             QueryProcess exec = QueryProcess.create(g);
             
             String q = 
                     "prefix ex: <http://ns.inria.fr/sparql-extension/aggregate#>"
                     + "@debug "
                     + "select * "
                     + "(ex:median(xt:iota(5)) as ?m)"
                     + "(ex:sigma(xt:iota(5)) as ?s)"
                     + ""
                     + "where {}";
             exec.setLoadFunction(true);
             Mappings map = exec.query(q);
             IDatatype dm = (IDatatype) map.getValue("?m");
             IDatatype ds = (IDatatype) map.getValue("?s");
             assertEquals(3, dm.intValue());
             assertEquals(1.41421, ds.doubleValue(), 0.01);
             //1.41421
             System.out.println(map);
             System.out.println(map.size());
      }
      
     
       public void testCustom() throws EngineException {
             Graph g = createGraph(); 
             QueryProcess exec = QueryProcess.create(g);
                                  
             String q = "function xt:main(){"
                     + "cs:fib(30) ;"
                     + "xt:display(us:test(<http://fr.dbpedia.org/resource/Antibes>)) "
                     + "}"
                     
                     + "@service <http://fr.dbpedia.org/sparql>"
                     + "function us:test(?x){"
                     + "let (?l = select * where { ?x rdfs:label ?l } limit 1)"
                     + "{?l}"
                     + "}"
                     ;
              Date d1 = new Date();
              IDatatype dt = null;
              int n = 1;
              for (int i = 0; i < n; i++){
              dt = exec.eval(q);
              }
             Date d2 = new Date();
            System.out.println("Time : " + (d2.getTime() - d1.getTime()) / (n * 1000.0));
             System.out.println(dt);
      }
      
      
       public void testAnnot() throws EngineException {
             Graph g = createGraph(); 
             QueryProcess exec = QueryProcess.create(g);
             
             String i = "@public {"
                     + "function us:foo(){"
                     + "us:bar()"
                     + "}\n"
                     
                     +"@debug function us:bar(){"
                     + "10"
                     + "}"                     
                     + "}";
             
             String q = "function xt:main(){"
                     + "cs:test(2);"
                     + "us:foo()"
                     + "}";
             
             exec.query(i);
             
             IDatatype dt = exec.eval(q);
             assertEquals(10, dt.intValue());
      }
      
      
      
       public void testMainFun() throws 
              EngineException, URISyntaxException, MalformedURLException, IOException, LoadException{
             Graph g = createGraph(); 
             QueryProcess exec = QueryProcess.create(g);
             
             String uri ="load rdf:";
             
             String q =
                     "select * where {}"
                     + "function xt:main(){"
                     + "let (?m = service <http://fr.dbpedia.org/sparql>{"
                     + "select * where {?x rdfs:label ?y} limit 10})"                    
                     + "{"
                    // + "xt:display(?m)"
                     + "}"
                     + "}"
                     
                     +" "
                     + " function xt:service(?u, ?q, ?m){"
                     + "xt:display(?m)"
                     + "}"
                     
                     +"@export @debug @test @trace "
                     + "function us:test(){ xt:display('Hello')}"
                     ;
             
             
            String qq = "function xt:main(){us:test()}";
            
             
             Mappings map = exec.query(q);
             System.out.println(map);
              map = exec.query(qq);           

       }
      
      
           
       public void testService() throws EngineException, LoadException {
            Graph g = createGraph();
            QueryProcess exec = QueryProcess.create(g);
            
            String q1 = "@service <http://fr.dbpedia.org/sparql>"
                    + "select * where {?x ?p ?y } limit 10";
            Mappings m1 = exec.query(q1);
            assertEquals(10, m1.size());
            
            String q2 = "@service <http://fr.dbpedia.org/sparql>"
                    + "construct where {?x ?p ?y } limit 10";
            Mappings m2 = exec.query(q2);
            Graph g2 = (Graph) m2.getGraph();
            assertEquals(10, g2.size());
            
               
       }
      
     
      
      //application/sparql-results+xml;
        public void testUnbound() throws EngineException{
             Graph g = createGraph(); 
             QueryProcess exec = QueryProcess.create(g);
             
             String q = "select (us:test() as ?res)"
                     + "where {}"
                     
                     + "function us:test(){"
                     + "let ((?x) = select * where { optional { ?x rdf:value ?y }}){"
                      + "xt:display(if (bound(?x), ?x, false));"                    
                    + "if (bound(?x), true, false)"                    
                     + "}"
                     + "}";
         Mappings map = exec.query(q);
         IDatatype dt = (IDatatype) map.getValue("?res");
         assertEquals(true, dt != null);
         assertEquals(false, dt.booleanValue());
          System.out.println(map);
      }
      
      
        public void testUnboundjh() throws EngineException{
             Graph g = createGraph(); 
             QueryProcess exec = QueryProcess.create(g);
             
             String q = "select (us:test() as ?res)"
                     + "where {}"
                     
                     + "function us:test(){"
                     + "let (?m = select * where { optional { ?x rdf:value ?y }}){"
                     + "xt:get(?m, '?x') = 'Unbound'^^dt:system"                    
                     + "}"
                     + "}";
         Mappings map = exec.query(q);
        
          System.out.println(map.getQuery().getAST());
      }
      
      
      
         public void testFib() throws EngineException{
             Graph g = createGraph(); 
             QueryProcess exec = QueryProcess.create(g);
        
             String q =  
                    "function xt:main(){"
                     + "us:fib(30)"
                     + "}" 
                     
              +     "function us:fib(?n) {"
                     + "if (?n <= 2){ 1 }"
                     + "else { us:fib(?n - 1) + us:fib(?n - 2) }"
                     + "} ";
           
             
            Mappings map = null;
            
            Date d1 = new Date();
            int n = 10;
            for (int i = 0; i<n; i++){
                map = exec.query(q);
            }
            Date d2 = new Date();
            System.out.println("Time : " + (d2.getTime() - d1.getTime()) / (n * 1000.0));

            System.out.println(map);
            System.out.println(Interpreter.count);
            
            
        
        }
      
    public void testSolution() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select * where {"
                + "bind (unnest(xt:iota(100)) as ?n)"
                + "}"
                
                + "function xt:solution(?q, ?ms){"
                + "for (?m in ?ms){"
                + "if (! us:check(?m)){"
                + "xt:reject(?m)}"
                + "}"                
                + "}"
                + "function us:check(?m){"
                + "rand() <= 0.5"
                + "}";
        
        Mappings map = exec.query(q);
        System.out.println(map.size());
        assertEquals(true, map.size() < 75);
     }
       
    public void testPPOWLdfg() throws EngineException, LoadException {
        Graph g = createGraph();
        Load ld = Load.create(g);
        //System.out.println("Load");
        ld.loadWE(data + "template/owl/data/primer.owl"); 
        QueryProcess exec = QueryProcess.create(g);
        
         String t1 ="prefix f: <http://example.com/owl/families/> "
                + "template  {  st:apply-templates-with(st:owl)}"
                + "where {}";
         
         String t2 ="prefix f: <http://example.com/owl/families/> "
                + "template  {  st:apply-templates-with(st:turtle)}"
                + "where {}";
         
         
         Mappings map = exec.query(t1);
         
          System.out.println(map.getTemplateStringResult());
         
         assertEquals(7574, map.getTemplateResult().getLabel().length());

        
        
    }
      public void testMain() throws EngineException{
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);
        
        String i = "insert data {"
                + "[] a us:Test ; us:width 2 ; us:length 3 "
                + "}";
        
        String q = "function xt:main(){"
                + "us:test()"
                + "}"
                
                + "function us:test(){"
                + "let (?m = select ?x (us:surface(?x) as ?s) where {?x a ?t}){"
                + "let ((?s, ?x) = ?m){"
                + "kg:display(?x); kg:display(?s);"
                + "?m"
                + "}"
                + "}}"
                
                + "function us:surface(?x){"
                + "let ((?l, ?w) =  select * where {?x us:length ?l ; us:width ?w }){"
                + "?l * ?w}"
                + "}"
                              
                ;
        
        exec.query(i);
        gs.init();
        Mappings map = exec.query(q);
        System.out.println(map);
        Mappings m = (Mappings) map.getNodeObject(ASTQuery.MAIN_VAR);
        IDatatype dt = (IDatatype) m.getValue("?s");
        assertEquals(6, dt.intValue());
      }
      
      
     
     
      
    public void testVD13() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q =
                "prefix cal: <http://ns.inria.fr/sparql-extension/calendar/>"
                + "select ?day  where {"
                + "bind (unnest(mapmerge(xt:span, mapselect(xt:prime, xt:iota(1901, 2000)))) as ?day)"               
                + "}"
                + "function xt:span(?y) { "
                + "mapselect (xt:check, \"Friday\", maplist(cal:date, ?y, xt:iota(1, 12), 13)) "
                + "}"
                + "function xt:check(?d, ?x) { (xt:day(?x) = ?d) }"               
                ;
        
        Mappings map = exec.query(q);
        assertEquals(23, map.size());

    }
     
     
     
     
    
      public void testConcat() throws EngineException {
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);

        String q =  "select (apply(xt:concat, xt:list('a', 'b')) as ?a) "
                + "(kg:concat('a', 'b') as ?b)"
                + "(concat('a', 'b') as ?c)"

                + "where {}" ;
         Mappings map = exec.query(q);
       
        System.out.println(map);
        
//        Processor.test();
     }
     
     
      public void testExport() throws EngineException {
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);

        String i = "export {"
                + "function us:foo(){us:bar()}"
                
                + "function us:bar(){'bar'}"
                + "}";
        
        String q ="function xt:main(){us:foo()}"
                
                +"function us:bar(){'my bar'}"
                ;
        
        String q2 ="function xt:main(){us:foo()}"  ;
        
        Mappings map = exec.query(i);
        map = exec.query(q);
        System.out.println(map);
        
        map = exec.query(q2);
        System.out.println(map);
     }
     
     
     
        public void testCallBack() throws EngineException {
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);

        String q = "select * (us:test() as ?t) where {"
                + " ?x ?p ?y "
                + "}"
               
                + "function us:test(){"
                + "let ((?s) = select * where { ?s ?p ?o }) {?s}"
                + "}"
               
                + "function xt:produce(?q){"
                + "xt:display(?q);"
                + "	xt:list(?q)"
                + "}";
        
        Mappings map = exec.query(q);
         System.out.println(map);
    }

     
     
     
      public void testMethod() throws EngineException{
      GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);
        
        String init = 
                "insert data {"
                + "foaf:Man rdfs:subClassOf foaf:Person "
                + "us:John a foaf:Man "
                + "us:Boat a foaf:Thing "
                
                + "us:bar a xt:Method ;"
                + "xt:name us:title ;"
                + "xt:input (foaf:Person) ;"
                + "xt:output xsd:string ."
                + ""
                
                + "us:foo a xt:Method ;"
                + "xt:name us:title ;"
                + "xt:input (foaf:Thing) ;"
                + "xt:output xsd:string ."               
                + "}";
        
        String q = "select (us:method(us:title, ?x) as ?t) where {"
                + "?x a foaf:Man"
                + "}"               
                
                + "function us:method(?m, ?x){"
                + "let ((?fun) = select * where {"
                + "?x rdf:type/rdfs:subClassOf* ?t . "
                + "?fun a xt:Method ; xt:name ?m ; xt:input(?t)})"
                + "{ ?fun }"
                + "}"
                
                +"function us:bar(?x){"
                + "'bar'"
                + "}"
                
                +"function us:foo(?x){"
                + "'foo'"
                + "}";
        
        
        exec.query(init);       
        Mappings map = exec.query(q);
         System.out.println(map);
//        IDatatype dt = (IDatatype) map.getValue(("?t"));
//        assertEquals("bar", dt.stringValue());       
     }
     
     
     
     public void testsqiodhfjsqd() throws EngineException{
      GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);
        String q = "function xt:main(){"
                + "for ((?s, ?p, ?o) in construct {[] rdf:value 1, 2, 3} where {}){"
                + "xt:display(?o)"
                + "}"
                + "}";
        
        Mappings map = exec.query(q);
        
         System.out.println(map.getQuery().getAST());
     }
     
      public void testListen2() throws EngineException, LoadException {
         GraphStore gs = GraphStore.create();
         QueryProcess exec = QueryProcess.create(gs);
         String i =  
             "function xt:main() {" +
            "   xt:tune(xt:listen)" +
            "}" +
            //"select * where {?x ?p ?y }" +
            "function xt:insert(?t){" +
            "  xt:display(?t)" +
            "}" +
            "" +
            "function xt:delete(?t){" +
            "  xt:display(?t)" +
            "}" +
            "" +
            "function xt:load(?p){" +
            "  xt:display(?p)" +
            "}" +
            "" +
            "function xt:begin(?q, ?ast){" +
            "  xt:display(?ast)" +
            "}" +
            "" +
            "function xt:end(?q, ?ast, ?ms){" +
            "  xt:display(?ms)" +
            "}";
         
         String q = "select * where {?x ?p ?y}";
         Mappings map = exec.query(i);
         ASTQuery ast = (ASTQuery)map.getQuery().getAST();
         System.out.println(ast);
         System.out.println(ast.getDefine());
          map = exec.query(q);
      }
      
     
     
      public void testListen() throws EngineException, LoadException {

         String i =  ""
                + "function xt:begin(?q, ?ast){xt:display(?ast)}"
                 
                + "function xt:end(?q, ?ast, ?ms){xt:display(?ms)}"
                 
                + "function xt:load(?p){xt:display(?p)}"
                 
                + "function xt:insert2(?e){"
                + "let ((?s, ?p, ?o) = ?e){"
                + "if (?p != rdf:type){kg:display(?e)}"
                + "}}"
                 
                 +"function xt:onInsert(?t){"
                 + "xt:display(?t)"
                 + "}";
         
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);
        Query def = exec.compile(i);
        gs.addListener(new GraphListen(def));
  
        String q = "select (now() as ?d) where {}";
        
        
        Load ld = Load.create(gs);
        ld.loadWE(NSManager.RDF);
        
        Mappings map = exec.query(q);
        
        
     }
     
    
     
    public void testexistsjlkk() throws EngineException {

        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);
        
        String init = "insert data {"
                + "[] rdf:value 1, 2, 3 ."
                + "[] rdfs:label 'a', 'b', 3 ."
                + "}";

        String qe = "select * (us:test() as ?T) where {"
                + "?x rdf:value ?y "
                + "?z rdfs:label ?y "
                + "}"
                + ""
                
                + "function us:test(){"              
                + "for (?t in construct where {?x ?p ?y}){"
                + "let ((?s, ?p, ?o) = ?t){"
                + "kg:display(?s); kg:display(?p); kg:display(?o)"
                + "}  "
                + "}"                
                + "}"
                
                +"function xt:produce(?q){"
                + "let (?g = construct where {?x ?p ?y}){"
                + "?g"
                + "}"
                + "}"
                              
                
                + "";

      

        exec.query(init);
        Mappings map = exec.query(qe);
         System.out.println(map);
        // assertEquals(3, map.size());
        
//        Mappings map = exec.query(q);
//        IDatatype dt = (IDatatype) map.getValue("?t");
//        assertEquals(1, dt.intValue());
    }
         
      public void testexists () throws EngineException{

        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);
        
        String i = "insert data {"
                + "[] us:width 2 ; us:length 3"
                + "[] us:width 3 ; us:length 4"
                + "}";
        
        String q = "select * (us:surface(?x) as ?s) where {"
                + "?x us:width ?w "
                + "}"
                
                +"export {"
                + "function us:surface(?x){"
                + "let ((?l, ?w) =  select * where {?x us:length ?l ; us:width ?w }){"
                + "?l * ?w"
                + "}"
                + "}"
                + "}"
                ;
        
        String q2 = 
                "select (us:test() as ?s) (us:surface(?x) as ?t) where {"
                 + "?x us:width ?w "
                + "}"
                
                + "function us:test(){"
                + "let (?sum = 0){"
                + "for (?m in select * where { ?x us:width ?w }){"
                + "let ((?w, ?x) = ?m){  "
                + "set (?sum = ?sum + ?w)"
                + "}}; ?sum"
                + "}"
                + "}";
        
        exec.query(i);
        Mappings map = exec.query(q);
             //System.out.println(map);
        //System.out.println(map.getQuery().getAST());     
        IDatatype dt = (IDatatype) map.getValue("?s");
        assertEquals(6, dt.intValue());
        
         map = exec.query(q2);
             System.out.println(map);
             
    }
    
    
    public void testeng () throws EngineException, LoadException{
                   Query.STD_PLAN = Query.QP_HEURISTICS_BASED;

        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);
        Load ld = Load.create(gs);
        ld.loadWE(data + "template/owl/data/primer.owl");
        
        String q = "select * where {"
                + "graph eng:describe {"
                + "[] kg:index 0 ; kg:item [ rdf:predicate ?p ; rdf:value ?v ] "
                + "}"
                + "filter exists { ?x ?p ?y }"
                + "}";
        
        Mappings map = exec.query(q);
        System.out.println(map.getQuery());
        
        assertEquals(56, map.size());
        
        // query the SPIN graph of previous query
         q = "select * where {"
                + "graph eng:query {"
                + "[] sp:predicate ?p "
                 + "values ?p { rdf:predicate rdf:value} "
                + "}"
                + "}";
         
         map = exec.query(q);
        
        assertEquals(2, map.size());
    }
    
    
    public void test10cons() {
        String query =
                "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select  *  where {"
//                + "bind ((kg:sparql('"
//                + "prefix c: <http://www.inria.fr/acacia/comma#>"
//                + "construct  where {?x rdf:type c:Person; c:hasCreated ?doc}')) "
//                + "as ?g)"
                + "bind (kg:default as ?g)"
                + "graph ?g { ?a ?p ?b }"
                + "} ";
        Query q = null;
        try {
            init();
          // Query.STD_PLAN = Query.QP_HEURISTICS_BASED;
           QueryProcess exec = QueryProcess.create(graph);
            
            q = exec.compile(query);
            Mappings map = exec.query(q);
            System.out.println(map.getQuery());
            System.out.println(graph.size());
            assertEquals("Result", 5, map.size());
        } 
        catch (EngineException e) {
            System.out.println(q);
            assertEquals("Result", true, e);
        }
        catch (NullPointerException e){
            System.out.println(q);
        }
    }
    
      
    public void testCustomAgg() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init = "insert data {"
                + "[] rdf:value 4, 5, 6, 7"
                + "[] rdf:value 1, 2, 3"
                + "}";

        String q = "select (aggregate(?v, us:mediane) as ?res)"
                + "where {"
                + "  ?x rdf:value ?v "
                + "}"
                + ""
                + "function us:mediane(?list){"
                + "  let (xt:sort(?list) as ?l){"
                + "    let (xt:count(?l) as ?ind, "
                + "kg:display(?ind) as ?pp, "
                + "xt:get(?l, xsd:integer((xt:size(?l) - 1) / 2)) as ?res, "
                + "kg:display(?res) as ?yy){ ?res }"
                + "  }"
                + "}"
                + "";

        exec.query(init);
        Mappings map = exec.query(q);
            System.out.println(map);
        IDatatype dt = (IDatatype) map.getValue("?res");
        assertEquals(4, dt.intValue());

    }
      
       
      
      
        public void fghdfghdfgh() throws EngineException{
             Graph g = createGraph(); 
             QueryProcess exec = QueryProcess.create(g);
            
             String init =  
                "prefix ex: <http://test.fr/>"
              + "insert data {"
              + " [] rdf:value ((1) (2 3)) "
              + "}";
             
             String q = "select * "
                     //+ "(maplist(st:list, ?l) as ?ll)"
                     + "where {"
                     + "?x rdf:rest*/rdf:first ?v "
                    // + "bind (st:list(?v) as ?l)"
                     + "filter ("
                     + "if (?x = ?x) {true} else {false}"
                   //  + "if (?x = ?x, true, false)"
                   //  + "def (?y = 3, ?z = 4){kg:display(?y + ?z)}"
                  //   + "}"
                     + ")"
                     + ""
                     + ""
                     + "}"
                    
                    ;
             
             exec.query(init);
             exec.addResultListener(new ResultListen());
            Mappings map = exec.query(q);
           System.out.println(map);
          // System.out.println(map.getQuery().getAST());
            
        }
      
      
      
      
      public void testExtList() throws EngineException{
          Graph g = Graph.create();
         QueryProcess exec = QueryProcess.create(g);
         
         String q ="prefix lt: <http://ns.inria.fr/sparql-datatype/list#>"
                 + "select   (?l2 <= ?l1 as ?res) "
                 + "where {"
                
                 + "bind (xt:iota(11) as ?l1)"
                 + "bind (xt:reverse(xt:iota(10)) as ?l2)"
                 + "bind (define ("
                 
                 + "function (lt:equal(?x, ?y) = (?x <= ?y && ?y <= ?x)) ,"
                 
                 + "function (lt:lessEqual(?x, ?y) = mapevery (xt:member, ?x, xt:list(?y))) ,"
               
                 + "function (lt:less(?x, ?y) = ( ?x <= ?y && ! (?y <= ?x))) ,"

                 + "function (xt:member(?e, ?l) = mapany (kg:equal, ?e, ?l))"
                 + ""
   //              + "function (xt:sort(?l) = "
                 + ""
                 + ") as ?f)"
                 + "}";
         
         
       
       
         String q4 = "select * where {"
                 + "bind (mapselect (xt:prime, xt:iota(100)) as ?test)"
                 + "}";
         
         String qd = "prefix cal: <http://ns.inria.fr/sparql-extension/calendar/>"
                 + "select * where {"
                 + "bind (unnest(xt:iota(1901, 2000)) as ?y)"
                 + "bind (unnest(xt:iota(1, 12))      as ?m)"
                 + ""
                 + "filter (xt:day(cal:date(?y, ?m, 13)) = 'Friday') "
                 + "}";  
         
         String q5 = 
                 "prefix cal: <http://ns.inria.fr/sparql-extension/calendar/>\n" +
                    "select ?day  where {\n" +
                    "bind (unnest(mapmerge(xt:span, mapselect(xt:prime, xt:iota(1901, 2000)))) as ?day)\n" +
                    "bind (function (xt:span(?y) = \n" +
                    "mapselect (xt:check, \"Friday\", maplist(cal:date, ?y, xt:iota(1, 12), 13))) as ?f1)\n" +
                    "bind (function (xt:check(?d, ?x) = (xt:day(?x) = ?d)) as ?f2)\n" +
                    "}";  
         
         String q6 = "select * "
//                 + "(kg:and(true, true)  as ?b1)"
//                 + "(kg:and(true, false) as ?b2)"
//                 + "(kg:and(false, false) as ?b3)"
//                 + "(kg:and(true, <test>) as ?b4)"
//                 + "(kg:and(<test>, false) as ?b5)"
//                 + "(kg:and(<test>, <test>) as ?b6)"
                 
//                  + "(kg:or(true, true)  as ?b1)"
//                 + "(kg:or(true, false) as ?b2)"
//                 + "(kg:or(false, false) as ?b3)"
//                 + "(kg:or(true, <test>) as ?b4)"
//                 + "(kg:or(<test>, false) as ?b5)"
//                 + "(kg:or(<test>, <test>) as ?b6)"
//                 
                 
                 
                
//                 + "(apply (xt:append, xt:list(xt:iota(1), xt:list(2))) as ?x)"
//                 + "(apply (kg:plus, xt:iota(5)) as ?y)"
//                 + "(apply (kg:mult, xt:iota(5)) as ?z)"
//                 + "(apply (kg:concat, xt:list('a', 'b', 'c')) as ?t)"
                 + "(apply (kg:or, xt:list()) as ?b1)"
                 + "(apply (kg:and, xt:list()) as ?b2)"
                 + "(apply (kg:plus, xt:list()) as ?b3)"
                 + "(apply (kg:mult, xt:list()) as ?b4)"
                 + "(apply (xt:append, xt:list()) as ?b5)"
                 + ""
                
                 + "where {"
                 
    
     + "}";
         
         
         String qq = "select (xt:foo(xt:test(1)) as ?res)"
                 + "where {"
                 + "export {"
                 + "function xt:test(?x) { ?x }"
                 + "function xt:foo(?x)  { ?x + ?x }"
                 + "}"
                 + "}";
         
         String qq2 = "select (xt:foo(xt:test(1)) as ?res)"
                 + "where {"
                 
                 + "}";
         
         Mappings map = exec.query(qq);         
    map = exec.query(qq2);      
    System.out.println(map);
          System.out.println(map.getQuery().getAST());
          ASTQuery ast = (ASTQuery) map.getQuery().getAST();
         for (Expression e : ast.getDefine().getFunList()){
             System.out.println(e);
         }
      }
      
      
      
       
      
      
      public void testVis() throws EngineException{
          Graph g = createGraph(); 
             QueryProcess exec = QueryProcess.create(g);
             String init = "insert data {"
                     + "<John> rdf:value <testuritest> ;"
                     + "rdfs:label 'Johnny' "
                     + "}";
            
             String q = "select *"
                     + "where {"
                     + "<Jo> ?p <uri> ;"
                     + "rdfs:label 'John' "
                     + "bind ("
                     + "function (  xt:match(?x, ?y) = "
                     + "  (contains(?x, ?y) || contains(?y, ?x))"
                     + ")"
                     + "as ?f )"
                     + "}";
             
             exec.query(init);
             exec.setVisitor(new MyQueryVisitor());
             Mappings map = exec.query(q);
             System.out.println(map);
             System.out.println(map.size());
      }
      
      class MyQueryVisitor implements QueryVisitor {
          
          ArrayList<Triple> filter = new ArrayList<Triple>();
          int count = 0;
          ASTQuery ast;

        @Override
        public void visit(ASTQuery ast) {
            this.ast = ast;
            
            for (Exp exp : ast.getBody().getBody()){
                if (exp.isTriple()){
                       process(exp.getTriple());                 
                }
            }
            
            for (Triple t : filter){
                ast.getBody().add(t);
            }
            System.out.println(ast);
        }
        
        void process(Triple t){
            if (t.getObject().isConstant()){
                process(t, t.getSubject(), 0);
                process(t, t.getObject(), 1);
            }
        }
        
        void process(Triple t, Atom at, int i){
            if (at.isConstant()){
                Variable var = new Variable("?_var_" + count++);              
                Term f = ast.createFunction(ast.createQName("xt:match"), var);
                f.add(at);
                if (i == 0) t.setSubject(var); else t.setObject(var);
                filter.add(Triple.create(f));
            }
        }

        @Override
        public void visit(Query query) {
        }
          
      }
      
      
        public void testEEE2() throws EngineException{
             Graph g = createGraph(); 
             QueryProcess exec = QueryProcess.create(g);
            
             String q = "select (datatype(xt:list(1)) as ?l)"
                                  
                     + "(apply  (kg:plus, maplist (us:fun, xt:iota(0, 12))) as ?e)"
                     + "(define ("
                     + "defun us:fac(?n) { if (?n = 0, 1, ?n *  us:fac(?n - 1)) } ,"
                     + "defun us:fun(?x) { 1 / us:fac(?x) }"
                     + ") as ?fun)"
                     + "where {}";
             
             String pi =
                     "select (2 * ?p1 * ?p2 as ?pi)"
                     + "where {"
                     + "bind (apply(kg:mult, maplist (kg:div, xt:iota(2, ?n, 2), xt:iota(1, ?n - 1, 2)))  as ?p1)"
                     + "bind (apply(kg:mult, maplist (kg:div, xt:iota(2, ?n, 2), xt:iota(3, ?n + 1, 2))) as ?p2)"
                    + "}"
                     + "values ?n { 100000 }";
             
             String q2 = "select * "
                     + "where {"
                     + "bind (unnest(xt:list(xt:list(1, 2), xt:list(3, 4))) as (?x, ?y))"
                     + "}";
            
            Mappings map = exec.query(q);
            System.out.println(map);       
                
        }
        
        
        
        
       
        public void testOWLRLfghfdgh() throws EngineException, IOException, LoadException {
        double d = 0.2;
        //System.out.println("Test: " + d);
        //ResultWatcher.LIMIT = d;
        Graph gs = Graph.create();
        Load ld = Load.create(gs);
        //ld.setLimit(50000);
        System.out.println("Load");
        try {
          //ld.loadWE(data + "template/owl/data/primer.owl"); 
         ld.setLimit(1000000);
          ld.loadWE(data + "fma/fma3.2.owl"); 
          
          /**
           * decl Time : 14.82
           * class       19.307
           * prop : 40 ?
           * stmt sans triple : 40 sec
           * avec: crash à 6mn
           * Time : 67.466
                50.124.688
                * 
                * Turtle: Time : 115.393
                   size 146.836.569
           * 
           */
        }
        catch (LoadException e){
            System.out.println(e);
        }
        System.out.println("Size: " + gs.size());

            System.out.println("start");
        Date d1 = new Date();
        Transformer t = Transformer.create(gs, Transformer.OWL);
        t.definePrefix("fma", "http://purl.org/sig/fma/");
        t.definePrefix("bs",  "http://purl.org/sig/fma/base/");
        IDatatype dt = t.process();
        Date d2 = new Date();
         System.out.println("Time : " + (d2.getTime() - d1.getTime()) / 1000.0 );
         System.out.println(dt.stringValue().length());
           // System.out.println(dt.getLabel());
        }
  
               
        public void testlsdhflhdf() throws EngineException {
            Graph g = Graph.create();
            QueryProcess exec = QueryProcess.create(g);
            String q = "prefix db: <http://fr.dbpedia.org/resource/>"
                    + "select * "
                    + "(xt:test(?r) as ?res)"
                    + ""
                    + ""
                    + "where {"
                    + ""
                    + "bind (defun xt:test(?x) {"
                    + "st:call-template('/home/corby/AATest/sttl/fun#test', ?x)"
                    + "} as ?fun)"
                    + "}"
                    + "values ?r { "
                    + "db:Paris db:Antibes "
                    + "} ";
            
            Mappings map = exec.query(q);
            System.out.println(map);
        }
        
       public void testlkhlhlh() {

        Graph g = Graph.create();
        Load ld = Load.create(g);
        try {
            ld.loadWE("root-ontology.owl");
        } 
        catch (LoadException e){
            // ...
        }
        RuleEngine re = RuleEngine.create(g);
            re.setProfile(RuleEngine.OWL_RL);
            re.process();
            System.out.println(g.display());
        }
        
        
      public void testExtDT() throws EngineException{
          Graph g = Graph.create();
         QueryProcess exec = QueryProcess.create(g);
         String q ="prefix dt: <http://example.org/>"
                 + "prefix ex: <http://example.org/test#>"
                 + "select ?res where {"
                 + ""
                 + "bind (package("
                 + "define (ex:equal(?x, ?y)   = (str(?x) = str(?y))) , "
                 + "define (ex:diff(?x, ?y)    = (str(?x) != str(?y))) ,"
                 + "define (ex:less(?x, ?y)    = (str(?x) < str(?y))) ,"
                 + "define (ex:lessEqual(?x, ?y)  = (str(?x) <= str(?y))) ,"
                 + "define (ex:greater(?x, ?y)   = (str(?x) > str(?y))) ,"
                 + "define (ex:greaterEqual(?x, ?y) = (str(?x) >= str(?y)))"
                 + ") as ?def)"
                 + "bind ( "
                 + "'aa'^^dt:test <  'bb'^^dt:test &&"
                 + "'bb'^^dt:test <=  'bb'^^dt:test &&"
                 + "'cc'^^dt:test >  'bb'^^dt:test &&"
                 + "'aa'^^dt:test != 'bb'^^dt:test &&"
                 + "'aa'^^dt:test =  'aa'^^dt:test &&"
                 + "'cc'^^dt:test >= 'bb'^^dt:test && "
                 + " 'aa'^^dt:test not in ('bb'^^dt:test , 'cc'^^dt:test)"
                 + "as ?res)"
                 + "}"
                 + ""
                 + "";
         
        String q2 = 
                 "prefix dt: <http://ns.inria.fr/sparql-datatype/>"
                 + "prefix ex: <http://ns.inria.fr/sparql-datatype/romain#>"
                 + "prefix rm: <http://ns.inria.fr/sparql-extension/spqr/>"
                 + "select ?res ?val  (rm:digit(?val) as ?dig) "
                 + ""
                 + "where {"
                 + "bind(package("
                 + "function (ex:equal(?x, ?y)   = (rm:digit(?x) = rm:digit(?y))) , "
                 + "function (ex:diff(?x, ?y)    = (rm:digit(?x) != rm:digit(?y))) ,"
                 + "function (ex:less(?x, ?y)    = (rm:digit(?x) < rm:digit(?y))) ,"
                 + "function (ex:lessEqual(?x, ?y)  = (rm:digit(?x) <= rm:digit(?y))) ,"
                 + "function (ex:greater(?x, ?y)   = (rm:digit(?x) > rm:digit(?y))) ,"
                 + "function (ex:greaterEqual(?x, ?y) = (rm:digit(?x) >= rm:digit(?y))), "
                 
                 + "function (ex:plus(?x, ?y)  = ex:romain(rm:digit(?x) + rm:digit(?y))) ,"
                 + "function (ex:minus(?x, ?y) = ex:romain(rm:digit(?x) - rm:digit(?y))) ,"
                 + "function (ex:mult(?x, ?y)  = ex:romain(rm:digit(?x) * rm:digit(?y))) ,"
                 + "function (ex:div(?x, ?y)   = ex:romain(rm:digit(?x) / rm:digit(?y))), "
                
                 + "function (ex:romain(?x) = strdt(rm:romain(?x), dt:romain))"
                 + ") as ?p)"
                 + ""
                 + "bind (  'II'^^dt:romain * 'X'^^dt:romain + 'V'^^dt:romain as ?res) "
                 + "bind (maplist(ex:romain,  xt:iota(7)) as ?list)"
                 + "bind (apply (kg:mult, ?list) as ?val)"
                 + " "+ ""
              //   + "bind (ex:romain(xt:romain(?val)) as ?test)"                 
                 + "}";
                 
                 
         Mappings map = exec.query(q2);
         IDatatype dt = (IDatatype) map.getValue("?dig");
         assertEquals(5040, dt.intValue());
     }
      
      
      
      
      
      public void testPPSPINwdfgdwfgd() throws EngineException, LoadException {
        String t1 =
                "prefix f: <http://example.com/owl/families/> "
                + "template  {  st:apply-templates-with(st:spinhtml)}"
                + "where {}";
        
        File f = new File(data + "template/spinhtml/data/");

        for (File ff : f.listFiles()) {
            testSPPP(ff.getAbsolutePath());           
        }

        
    }
      
       public void testSPPP(String path) throws EngineException, LoadException {
        String t1 =
                "prefix f: <http://example.com/owl/families/> "
                + "template  {  st:apply-templates-with(st:spinhtml)}"
                + "where {}";
        Graph g = createGraph();
        Load ld = Load.create(g);
        //System.out.println("Load");
        ld.loadWE(path);

        QueryProcess exec = QueryProcess.create(g);

        Mappings map = exec.query(t1);
        System.out.println(map.getTemplateStringResult());
//        try {
//            Query q = exec.compile(map.getTemplateStringResult());
//            assertEquals(true, true);
//        } catch (EngineException e) {
//            System.out.println(path);
//            System.out.println(e);
//            assertEquals(true, false);
//        }
    }
      
      public void testapply() throws EngineException{
          QueryProcess exec = QueryProcess.create(Graph.create());
          String q = "select "
                  + "(apply(xt:concat, xt:iota('a', 'c')) as ?con)"
                  + "(apply(xt:sum, xt:iota(5)) as ?sum)"
                  + "(apply(xt:prod, xt:iota(5)) as ?mul)"
                  + "(xt:count(xt:iota(10)) as ?len)"
                  + "where {}";
          Mappings map = exec.query(q);
          IDatatype dt1 = (IDatatype) map.getValue("?con");
          IDatatype dt2 = (IDatatype) map.getValue("?sum");
          IDatatype dt3 = (IDatatype) map.getValue("?mul");
          assertEquals("abc", dt1.stringValue());
          assertEquals(15, dt2.intValue());
          assertEquals(120, dt3.intValue());
          System.out.println(map);
      }
      
       
        
         
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
    public void testCandidate() throws EngineException{
         Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        

        String q = "select * where {"
                + "bind (unnest(xt:iota(3)) as ?n)"
                + "}"
              
                + "function xt:solution(?q, ?ms){"
                + "map (kg:display, ?ms)"
                + "}";
        
        String qq = "select * where {"
                + "?x ?p ?y "
                + "filter (?y = ?z)"
                + "bind (?y as ?z)"
                + "}"
                + ""
                + "function xt:produce(?q){"
                + "xt:list(?q)"
                + "}";
        
        Mappings map = exec.query(qq);
        
            System.out.println(map.getQuery());
    }
    public void testCallback() throws EngineException  {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select * "
                + "where {"
                + "{?x foaf:name 'John' ; rdf:value 2 "
                + "?y foaf:name 'John'}"
                + "union { <James> ?q ?y} "
                + "optional { ?y foaf:name 'Bill' }"
                + "filter  exists {?x foaf:knows ?y} "
               // + "graph ?g { ?a rdf:value ?v }"
                + "}"
                
                + "function xt:produce(?q){"
                + "xt:list(?q)"
                + "}";
        
        String q2 = "select * "
                + "where {"
                + "?x foaf:name 'John' ; rdf:value 2 "
                + "?y foaf:name 'John' "
                + "filter not exists {?x foaf:knows ?y} "
                + "}"
                
                + "function xt:produce(?q){"
                + "xt:list(?q)"
                + "}";
        
        Mappings map = exec.query(q);
           System.out.println(map);
        //assertEquals(2, map.size());
        map = exec.query(q2);
        //assertEquals(0, map.size());
        
        List l = Arrays.asList(1, 2);
        l.toArray();
       // l.add(3);
           System.out.println(l);
       }
      
       
    public void testLoopable()  {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        
       
        
        String q = "select * where {"
                + "?x a <Tust> ; rdfs:label ?l "
                + "<John> ?p ?v ;"
                + "rdf:value 1 "
                + "?x rdf:value 2"
                + "}"
                
                + "function xt:candidate(?q, ?t, ?b){"                            
                    + "let ((?s, ?p, ?o) = ?t){"
                    //+ "us:incr(?p)"
                + "let ((?qs, ?qp, ?qo) = ?q){ true }"
                    + "}"               
                + "}"
                
                +"function us:add(?x, ?v){"
                + "st:set(?x, xt:cons(?v, st:get(?x)))"
                + "}"
                
                +"function us:incr(?p){"
                + "let (us:init(?p) as ?i){"
                + "st:set(?p, st:get(?p) + 1)"
                + "}"
                + "}"
                             
                
                +"function us:init(?p){"
                + "coalesce(st:get(?p), st:set(?p, 0))"
                + "}"
                
                +"function xt:start(?q, ?ast){"
                + "true"
                + "}"
        
                +"function xt:solution(?q, ?ms){"
                + "if (xt:size(?ms) = 0){"
                + "us:check(?q)}"
                + "}"
                + ""
                + "function us:check(?q){"
                + "  for (?t in ?q){"
                + "    let ((?s, ?p, ?o) = ?t){"
                + "	if (! us:match(?s, ?p, ?o)){"
                + "	  kg:display(?t)"
                + "	}"
                + "      }"
                + "  }"
                + "}"
                + ""
                + "function us:match(?s, ?p, ?o){"
                + "if (isBlank(?s), "
                + " if (isBlank(?p), "
                + "     if (isBlank(?o), exists {?x ?q ?y}, exists {?x ?q ?o}),"
                + "     if (isBlank(?o), exists {?x ?p ?y}, exists {?x ?p ?o})),"
                + " if (isBlank(?p), "
                + "     if (isBlank(?o), exists {?s ?q ?y}, exists {?s ?q ?o}),"
                + "     if (isBlank(?o), exists {?s ?p ?y}, exists {?s ?p ?o})))"
                + "}"
                ;
        
         String init = "insert data {"
                + "[] rdf:value 1, 2 "
                + "<John> a <Test> "
                + "<Jack> a <Test> ; rdf:value <Test> "
                + "}";
        
        String q2 = "select * "
                + "where {"
                + "?x foaf:name 'John' ; rdf:value 2 "
                + "?y foaf:name 'John' "
               // + "minus { ?x foaf:test ?z }"
                + "filter  exists {?x foaf:knows ?y} "
               // + "bind (unnest(us:test()) as ?t)"
                + "}"
                
                + "function xt:produce(?q){"
                + "xt:list(?q)"
                + "}"
                
                +"function xt:produce1(?q){"
               // + "xt:cons("
                + "let ((?s, ?p, ?o) = ?q){"
                + "if (?p = foaf:name){ kg:display(?q) ; "
                + "xt:list(xt:triple(<Jack>, foaf:name, 'Jack'))"
                + "}"
                + "else {}} "
                //+ "maplist(xt:triple, <John>, rdf:value, xt:iota(5))"
               // + ")"
                + ""
                + ""                            
                + "}"
                
                +"function xt:candidate2(?q, ?t, ?b){"
                + "kg:display(?t) ; kg:display(?b)"
                + "}"
                
                +"function xt:produce2(?q){"
                + "let (?_ = bnode()){"
                + "xt:edge(?_, rdf:value, ?_)"
                + "}"
                + "}"
                           
                
                ;
        try {
            //exec.query(init);
            Mappings map = exec.query(q2);
            System.out.println(map);
        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        //Transformer t = (Transformer) map.getQuery().getTransformer();
       // System.out.println(t.getContext());
       // System.out.println(map.getQuery().getAST());
           
    }
      
       
        
        
        
      public void testExtDTdfg() throws EngineException{
          Graph g = Graph.create();
         QueryProcess exec = QueryProcess.create(g);
         String q ="prefix dt: <http://example.org/>"
                 + "prefix ex: <http://example.org/test#>"
                 + "select ?res where {"
                 + "bind ( "
                 + "'aa'^^dt:test <  'bb'^^dt:test &&"
                 + "'bb'^^dt:test <=  'bb'^^dt:test &&"
                 + "'cc'^^dt:test >  'bb'^^dt:test &&"
                 + "'aa'^^dt:test != 'bb'^^dt:test &&"
                 + "'aa'^^dt:test =  'aa'^^dt:test &&"
                 + "'cc'^^dt:test >= 'bb'^^dt:test && "
                 + " 'aa'^^dt:test not in ('bb'^^dt:test , 'cc'^^dt:test)"
                 + "as ?res)"
                 + "}"
                 
                 + "export {"
                 + "function ex:equal(?x, ?y)   {  (str(?x) = str(?y))} "
                 + "function ex:diff(?x, ?y)    {  (str(?x) != str(?y))}"
                 + "function ex:less(?x, ?y)    {  (str(?x) < str(?y))}"
                 + "function ex:lessEqual(?x, ?y)  {  (str(?x) <=  str(?y))}"
                 + "function ex:greater(?x, ?y)   {  (str(?x) > str(?y))}"
                 + "function ex:greaterEqual(?x, ?y) {  (str(?x) >= str(?y))}"
                 + "}"                 
                 
                 + ""
                 + "";
         
          String q2 = 
                 "prefix dt: <http://ns.inria.fr/sparql-datatype/>"
                 + "prefix ex: <http://ns.inria.fr/sparql-datatype/romain#>"
                 + "prefix rm: <http://ns.inria.fr/sparql-extension/spqr/>"
                 + "select ?res ?val  (rm:digit(?val) as ?dig) "
                 + ""
                 + "where {"
                               
                 + ""
                 + "bind (  'II'^^dt:romain * 'X'^^dt:romain + 'V'^^dt:romain as ?res) "
                 + "bind (maplist(ex:romain,  xt:iota(7)) as ?list)"
                 + "bind (apply (xt:mult, ?list) as ?val)"
                 + " "
                 + "}"
                  
                + "export {"
                 + "function ex:equal(?x, ?y)   { (rm:digit(?x) = rm:digit(?y))} "
                 + "function ex:diff(?x, ?y)    { (rm:digit(?x) != rm:digit(?y))}"
                 + "function ex:less(?x, ?y)    { (rm:digit(?x) < rm:digit(?y))}"
                 + "function ex:lessEqual(?x, ?y)  { (rm:digit(?x) <= rm:digit(?y))}"
                 + "function ex:greater(?x, ?y)   { (rm:digit(?x) > rm:digit(?y))}"
                 + "function ex:greaterEqual(?x, ?y) { (rm:digit(?x) >= rm:digit(?y))} "
                 
                 + "function ex:plus(?x, ?y)  { ex:romain(rm:digit(?x) + rm:digit(?y))}"
                 + "function ex:minus(?x, ?y) { ex:romain(rm:digit(?x) - rm:digit(?y))}"
                 + "function ex:mult(?x, ?y)  { ex:romain(rm:digit(?x) * rm:digit(?y))}"
                 + "function ex:divis(?x, ?y) { ex:romain(rm:digit(?x) / rm:digit(?y))} "
                
                 + "function ex:romain(?x) { strdt(rm:romain(?x), dt:romain)}"
                  + "}"                 
                  ;
                     
          String q3 = 
                    "prefix ex: <http://ns.inria.fr/sparql-datatype/romain#>"                
                  + "prefix rm: <http://ns.inria.fr/sparql-extension/spqr/>"
                  + "select * (us:test(?val) as ?tt) where {"
                  + "bind (unnest(maplist(ex:romain, xt:reverse(xt:iota(10)))) as ?val)"
                  + "}"
                  + "order by desc(?val)"
                  + "function us:test(?x){"
                  + "kg:display(?x);"
                  + "let (?y = ?x + ?x){"
                  + "kg:display(?y) ;"
                  + "?y"
                  + "}"
                  + "}";
         
         Mappings map = exec.query(q);
         IDatatype dt = (IDatatype) map.getValue("?res");
          assertEquals(dt.booleanValue(), true);
          
         map = exec.query(q2);
         dt = (IDatatype) map.getValue("?dig");
         assertEquals(5040, dt.intValue()); 
         
         map = exec.query(q3);
         System.out.println(map);
      }
        
        public void testGeneralizeff() throws EngineException, LoadException, ParserConfigurationException, SAXException, IOException{
             Graph g = Graph.create(true); 
             QueryProcess exec = QueryProcess.create(g);
             String init = "prefix ex: <http://example.org/>"
                     + "insert data { "
                     + "ex:John a ex:OldMan ;"
                     + "ex:author [ a ex:Document ]"
                     + "ex:Jack a ex:Person ;"
                     + "ex:author [ a ex:Document ]"                     
                     + "ex:Man       rdfs:subClassOf ex:Human "
                     + "ex:YoungMan  rdfs:subClassOf ex:Man "
                     + "ex:OldMan    rdfs:subClassOf ex:Man "
                     + "ex:Report    rdfs:subClassOf ex:Document  "
                     + "}";

            // target type more general than query
            // target type brother of query
             String qq = "prefix ex: <http://example.org/>"
                     + "select * (kg:similarity() as ?sim) "                                       
                     + "where {"                                                            
                     + "?x a ex:YoungMan, ?tt ;"
                     + "ex:author [ a ex:Report ] "
                     + ""
                     + "}"
                     
                     + "function xt:candidate(?q, ?t, ?b) { "
                     + "let ((?qs, ?qp, ?qo) = ?q, "
                     + "     (?ts, ?tp, ?to) = ?t) {"
                     + "if (?qp = rdf:type && isURI(?qo), "
                     + "?b || exists { ?qo rdfs:subClassOf/(rdfs:subClassOf*|^rdfs:subClassOf) ?to } ,"
                     + "?b)"
                     + "}"
                     + "}" 
                     
                     + "function us:test(){"
                     + "if (?x = ?y){true}"
                     + "else if (?x != ?y){false}"
                     //+ "else {true}"
                     + "}"
                     + ""
                     ;
           
            exec.query(init);
            Mappings map = exec.query(qq);
            System.out.println(map);
            System.out.println(map.size());
            System.out.println(map.getQuery().getAST());
            //assertEquals(1, map.size());
        }
        
        
        
    public void testSet() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select (us:test() as ?t) where {}"
                + "function us:test(){"
                + "let (?sum = 0){ "
                + "for (?x in xt:iota(100)){"
                + "if (xt:prime(?x)){"
                + "set(?sum = ?sum + 1)"
                + "}"
                + "} ;"
                + "?sum"
                + "}"
                + "}";

        Mappings map = exec.query(q);
        System.out.println(map);
        IDatatype dt = (IDatatype) map.getValue("?t");
        assertEquals(25, dt.intValue());
            System.out.println(map.getQuery().getAST());

    }
        
        
        public void testNew() throws EngineException{
           Graph g = Graph.create();
          QueryProcess exec = QueryProcess.create(g);
          
          String q = "select (us:test() as ?t) where {}"
                  
                  + "function us:test(){"
                  + "let (?sum = 0, "
                  + "for (?x in xt:iota(100)){"
                  + "if (xt:prime(?x)){"
                  + "set(?sum = ?sum + 1)"
                  + "}"
                  + "} as ?t)"
                  + "{"
                  + "?sum}"
                  + "}";
          
          Mappings map = exec.query(q);
            System.out.println(map);
            IDatatype dt = (IDatatype) map.getValue("?t");
            assertEquals(25, dt.intValue());
          
        }
        
    
      public void testList2() throws EngineException{
           Graph g = Graph.create();
          QueryProcess exec = QueryProcess.create(g);
         
          
          String q = "select "
                  + "(xt:list() as ?nil)"
                  + "(xt:list(1, 2) as ?list)"
                  + "(xt:cons(0, ?list) as ?res)"
                  + "(xt:reverse(?res) as ?rev)"
                  + "(xt:first(?res) as ?fst)"
                  + "(xt:rest(?res) as ?rst)"
                  + "(xt:copy(xt:list(1, 2)) as ?cp)"
                  + "(xt:append(xt:list(1, 2), xt:list(3, 4)) as ?app)"
                  + "(map(kg:display, ?app) as ?dsp)"
                  + "(maplist(xt:list, xt:iota(10)) as ?jhg)"
                  + "where {"
                  + "bind (define(xt:copy(?list) = maplist(xt:self, ?list)) as ?fun)"                
                  + "bind ("
                  + "define(xt:append(?l1, ?l2) ="
                  + "if (xt:count(?l1) = 0, xt:copy(?l2),"
                  + "xt:cons(xt:first(?l1), xt:append(xt:rest(?l1), ?l2))))"
                  + "as ?bar)"
                 
                  + "}";
          
          String q1 = "insert {"
                  + "?f rdf:first ?e ; rdf:rest ?r "
                  + "}"
                  + "where {"
                  + "bind ("
                  + "define (xt:bnode(?n) = coalesce(st:get(?n), st:set(?n, bnode())))"
                  + "as ?fun)"
                  + "bind (unnest(xt:iota(10)) as ?e) "
                  + "bind (xt:bnode(?e) as ?f)"
                  + "bind (if (?e = 10, rdf:nil, xt:bnode(?e + 1)) as ?r)"
                  + ""
                
                  + "}";
          
          String q2 = 
                  "insert {"
                  + "?f rdf:first ?e ; rdf:rest ?r "
                  + "}"
                   
                  + "where {"
                  + "bind (maplist(bnode(?x), xt:iota(10))   as ?list)"
                  + "bind (unnest(xt:iota(10)) as ?e) "
                  + "bind (xt:get(?list, ?e - 1) as ?f)"
                  + "bind (if (?e = 10, rdf:nil, xt:get(?list, ?e)) as ?r)"
                  + ""
                
                  + "}";
          
          String q3 ="select (xt:append(xt:list(1, 2), xt:list(3, 4)) as ?a)"
                  + "where {}";
          
           String q4 = "prefix cal: <http://ns.inria.fr/sparql-extension/calendar/>" 
                   + "template { "
                   + "'\\n' cal:month(?m) '\\n' "
                   + "'Mo Tu We Th Fr Sa Su \\n'"
                   + "group {"
                   + "if (?n = 1, xt:space(cal:num(?jour) - 1), '') "
                   + "if (?n < 10, ' ', '') ?n ' ' "
                   + "if (?jour = 'Sunday', '\\n', '')"
                   + "; separator = ''"
                   + "}"
                   + "; separator='\\n'"                   
                   + "}"
                   + "where {"
                   + "bind (unnest(xt:iota(12)) as ?m) "
                   + "bind (unnest(xt:iota(cal:days(?y, ?m))) as ?n)"
                   + "bind (xsd:date(concat(?y, '-', ?m, '-', ?n)) as ?day)"
                   + "bind (xt:day(?day) as ?jour)"
                   + "bind (define(xt:space(?n) = "
                   + "if (?n = 0, '', concat('   ' , xt:space(?n - 1)))) as ?sp)"
                   + "}"
                   + "group by ?m "
                   + "order by ?m "
                   + "values ?y { 2015 }";
           
          
           
           String qq = "select (aggregate(?v, xt:sort_concat) as ?res)"
                   + "where {"
                   + "?x rdf:value/rdf:rest*/rdf:first ?v"
                   + "}"
                   + "function xt:sort_concat(?list){"
                   + "?x = kg:display(?list)){"
                   + "apply(xt:test, xt:sort(?list))"
                   + "}}"
                   + "function xt:test(?x, ?y){"
                   + "concat(?x, ' ', ?y)"
                   + "}" ;
           
           String qq2 = "select (aggregate(?v, us:sigma) as ?sig)"
                   + "where {"
                   + "?x rdf:value ?v "
                   + "filter isLiteral(?v) "
                   + "}"
                   + "function us:sigma(?list){"
                   + "let (?m = us:avg(?list),"
                   + "(?dev = apply (kg:plus, maplist(us:sqdiff, ?list, ?m)) / xt:size(?list))){"
                   + "power(?dev, 0.5)"
                   + "}"
                   + "}"
                   + "function us:avg(?list){"
                   + "apply (kg:plus, ?list) / xt:size(?list) "
                   + "}"
                   + "function us:sqdiff(?x, ?m){"
                   + "power(?x - ?m, 2)"
                   + "}";
           
           String qq3 = "select (aggregate(2 * ?v, us:mediane) as ?med)"
                   + "(aggregate( 2 * ?v, us:avg) as ?avg)"
                   + "where {"
                   + "  ?x rdf:value ?v "
                   + "filter isLiteral(?v)"
                   + "}"
                  
                   + "function us:mediane(?list){"
                   + "  let (?l = xt:sort(?list)){"
                   + "    xt:get(?l, xsd:integer((xt:size(?l) - 1) / 2))"
                   + "  }"
                   + "}"
                   
                   + "function us:avg(?list){"
                   + "apply (xt:plus, ?list) / xt:size(?list) "
                   + "}"                   + "";
         
       String init = "insert data {"
                  + "[] rdf:value ('John' 'James' 'Jim' 'Jack') ."
                  + "[] rdf:value 1, 2, 3, 4, 5, 6, 7"
                   + "[] rdf:value 7 "
                   + "}";   
       
     
       
       String q6 = "select *"
              // + "(us:test(100) as ?res)"
               + "where {"
               + "?x ?p ?y "
               + "}"
               
               + "function us:test(?n){"
               + "let (st:set(us:sum, 0) as ?t,"
               + "for (?x in xt:iota(?n)){"
               + "if (xt:prime(?x)){"
               + "st:set(us:sum, st:get(us:sum) + ?x)}"
               + ""
               + "} as ?u)"
               + "{"
               + "st:get(us:sum)"
               + "}"
               + "}"
                        
               + "function xt:solution(?ms){"
               + "for (?m in ?ms){"
               + "for (?val in ?m){"
               + "if (isURI(?val) && strstarts(?val, rdf:)){"
               + "let (kg:display(?m) as ?t){xt:reject(?m)}"
               + "}"
               + "}"                     
               + "}"
               + "}";
       
       
       String q7 = "select * (us:test(?x) as ?t) "
               + "where {?x ?p ?y}"
               
               + "function xt:result(?q, ?m){"
               + "map(kg:display, ?q)"
              
               + "}"
               
               + "function us:test(?x){}"
               
               + "function xt:candidate2(?q, ?t, ?b){"
               + "let (?q as (?s, ?p, ?o)){"
               + "for (?x in ?q){"
               + "kg:display(isBlank(?x))}"
               + "}"
               + "}";
           
          exec.query(init);
           Mappings       map = exec.query(q7);
           
           //System.out.println(map);
           System.out.println(map);

         
           
       
      }
       
       
  
      
      public void sidfghsgf() throws EngineException{
       QueryProcess exec = QueryProcess.create(Graph.create());
       String q = 
               "prefix cal:  <http://ns.inria.fr/sparql-extension/calendar/>"
               + "prefix spqr: <http://ns.inria.fr/sparql-extension/spqr/>"
               + "select "
              
               + "(maplist(spqr:romain, xt:iota(1, 100)) as ?l)"
               + "(maplist(spqr:digit, ?l) as ?ld)"
               + "where {}"
               + "";
       
       Mappings map = exec.query(q);
      // System.out.println(Interpreter.getExtension());
       System.out.println(map);
              System.out.println(map.size());
       System.out.println(map.getQuery().getAST());

   }
      
      

     
      
     public void testExtFun6() throws EngineException{
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2, 3 "
                + "}";
               
        String q = "prefix ex: <http://example.org/> "
                + "select  * "
                + "where {"
                + "?x rdf:value ?n "
                     
                + "bind (define (xt:test(?n) = "
                + "if (?n = 1, xt:test(?n + 1),"
                + "let (?m = ?n + 1, exists { ?x rdf:value ?m }))) "
                + "as ?foo)"
                
                +"bind (define (xt:fun() = exists {?n rdf:value ?x}) as ?bar)"
                
                + "filter xt:test(?n)"
                + "filter xt:fun()"      
                + "}"             
                ; 
        
        
              
       Graph g = createGraph();                       
        QueryProcess exec = QueryProcess.create(g); 
        exec.query(init);
        Mappings map = exec.query(q);
         System.out.println(map);
         assertEquals(2, map.size());
        // System.out.println(map.getQuery().isFail());
         
     }
    
    Integer gi(int n){
        return n;
    }
    
    
    
    
     public void testExtFun3() throws EngineException{
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2 "
                + "ex:Jack ex:value 2"
                + "}";
        
        String q = "prefix ex: <http://example.org/> "
                + "select * "
                + "where {"
                + "?x rdf:value ?n "
                + "filter exists { select ?n { ?y ?p ?n  filter ( xt:fun(?n)) } } "
                + "bind (define(xt:fun(?n) = "
                + "exists { ?n ?q ?x }) "
                + "as ?f) "
               
                + "}"             
                ; 
        
         String qq = "prefix ex: <http://example.org/> "
                + "select * "
                + "where {"
                + "?x rdf:value ?n "
                + "filter exists { select ?n { ?y ?p ?n    filter ( xt:fun(?n))   "
                + "bind (define(xt:fun(?n) = "
                + "exists { ?n ?q ?x filter kg:display(?n) filter kg:display(?x) }) "
                + "as ?f)"
                 + "} } "
               
                + "}"             
                ; 
         
         String q2 = "prefix ex: <http://example.org/> "
                + "select * "
                + "where {"
                + "?x rdf:value ?n "
                 + "filter xt:fun(?n)"
                 + "bind (define (xt:fun(?n) = "
                 + "(2 in (?n))) "
                 + "as ?fun)"
              
               
                + "}"             
                ; 
          
        
       Graph g = createGraph();                       
        QueryProcess exec = QueryProcess.create(g); 
        exec.query(init);
        Mappings map = exec.query(q2);
        //assertEquals(0, map.size());
        System.out.println(map);
        System.out.println(map.size());
    }
    
   
    
    
    
    public void testFunBug() throws EngineException{
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John ex:name 'John' "
                + "ex:Jack ex:name 'Jack' "
                + "}";
        
        String q = "prefix ex: <http://example.org/> "
                + "select * "
                + "where {"
                + "?x ex:name ?n "
                + "filter ("
                + "let (?n = 'John', "
                + "exists { ?x ?p ?n })"
                + ")"
                + "}"             
                ; 
        
       Graph g = createGraph();                       
        QueryProcess exec = QueryProcess.create(g); 
        exec.query(init);
        Mappings map = exec.query(q);
        System.out.println(map);
    }
    
    
    
    
    
    
    
    
 public void testFun() throws EngineException, LoadException, EngineException {
        Graph g = createGraph();                       
        QueryProcess exec = QueryProcess.create(g);
                      
          String qq =
                   "template  {  st:apply-templates-with('/home/corby/AATest/transform/test')}"
                + "where {}";
          
         Mappings map = null;
         
         Date d1 = new Date();
         for (int i=0; i<1000; i++){
            map = exec.query(qq);         
         } 
         Date d2 = new Date();
         System.out.println("Time : " + (d2.getTime() - d1.getTime()) / (1000.0));

        System.out.println(map); 
            
       
    }    
    
    
        
       
        
    
    public void testNoGlobadfgjhdfghl() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String init = "insert data { "
                + "<John> rdf:value 1, 2, 3 ."
                + "<Jack> rdf:value 1, 2, 3 ."
                + ""
                + "}";


        String qq =
                "select *"
               + "(define (xt:result(?r) = "
                + "let (?d = kg:display(st:get(st:uri)),"
                + "if (st:get(st:uri, <John>),"
                + "let (?t = st:set(st:uri, <Jack>),"
                + "xt:again), xt:continue)))"
                + "as ?f)"
                + "where {"
                + "bind (st:get(st:uri) as ?x)"
                + "?x ?p ?y "
               //+ "filter (?x = st:get(st:uri))"
                + "}";


        exec.query(init);
        Query q = exec.compile(qq);
        Transformer t = Transformer.create(g, Transformer.TURTLE);
        t.getContext().set(Context.STL_URI, DatatypeMap.newResource("John"));
        q.setTransformer(null, t);
        Mappings map = exec.query(q);
        Node n = map.getNode("?z");
        System.out.println(map);
    }     
        
        
        
        
 public void testOOPPTT() throws EngineException{
            IDatatype dt = DatatypeMap.newInstance(10);
             Graph g = Graph.create(true); 
             QueryProcess exec = QueryProcess.create(g);
             String init = "insert data { "
                     + "<John> rdf:value 1, 2, 3, 4, 5, 6, 7, 8 ;"
                     + "rdfs:label 'a', 'b', 'c' ."
                     + ""
                     + "}";
            
            
             String qq = 
                     "select *"
                     + "(define (xt:test(?x) = ?y) as ?f)"
                     + "(xt:test(?x) as ?z)"
                     + "where {"
                     + "?x ?p ?y filter (! strstarts(?p, rdf:))"
                     
                     + "}";
             
            
             exec.query(init);
            Mappings map = exec.query(qq);
            Node n =  map.getNode("?z");
            assertEquals(null, n);
        }
        
        
        
        
    public void testFunfdghf() throws EngineException {
        String q = "select \n"
                + "(define (xt:f(?x) = ?x) as ?f)\n"
                + "(define (xt:f(?x, ?y) = ?x + ?y) as ?g)\n"
                + "(define (xt:f(?x, ?y, ?z) = ?x + ?y + ?z) as ?h)\n"
                + "(xt:f(1) as ?x)\n"
                + "(xt:f(1, 2) as ?y)\n"
                + "\n"
                + "where {\n"
                + "\n"
                + "}";
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        IDatatype dt1 = (IDatatype) map.getValue("?x");
        IDatatype dt2 = (IDatatype) map.getValue("?y");
        assertEquals(1, dt1.intValue());
        assertEquals(3, dt2.intValue());
    }



        public void testServnf(){
            ProviderImpl p = ProviderImpl.create();
            String serv = "http://fr.dbpedia.org/sparql";
            String query = "select * where {<http://fr.dbpedia.org/resource/Louis_XIV_de_France> rdfs:label ?l} limit 10";
        try {
            StringBuffer b = p.doPost2(serv, query);
            System.out.println(b);
        } catch (IOException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
              
    
     
    
         
    
     
     
      
    public void testPPOWL() throws EngineException, LoadException {
                 //QueryProcess.definePrefix("c", "http://www.inria.fr/acacia/comma#");

        Graph g = createGraph();
        Load ld = Load.create(g);
        //System.out.println("Load");
        ld.loadWE(data + "template/owl/data/primer.owl"); 
        QueryProcess exec = QueryProcess.create(g);
        
         String t1 ="prefix f: <http://example.com/owl/families/> "
                + "template  {  st:apply-templates-with(st:owl)}"
                + "where {}";
         
         String t2 ="prefix f: <http://example.com/owl/families/> "
                + "template  {  st:apply-templates-with(st:turtle)}"
                + "where {}";
         
         
         Mappings map = exec.query(t1);
         
         assertEquals(7582, map.getTemplateResult().getLabel().length());

        
        
    }
    
   
   
   
     public void testExtFun() throws EngineException, LoadException {
        Graph g = createGraph();      
        QueryProcess exec = QueryProcess.create(g);
        
         
         String query = 
                 "select *"
               + "(let (?f = define (st:fac(?x)  = if (?x = 1, 1, ?x * st:fac(?x - 1))), "
                 + "st:fac(?x)) as ?r)"
               //  +"(let (?t = st:test(st:fac(?x)), ?t) as ?r) "
                 + "where {"
                 + "bind (5 as ?x)"
                // + "bind  (define (st:test(?x) =  ?x * ?x) as ?gg)"
//                 + "bind  (define (st:test(?x) = let(?y = ?x * ?x, ?y)) as ?gg)"
//               //  + "bind (let (?f = st:test(st:fac(?x)), ?f) as ?r)"
//               //  + "bind (st:test(st:fac(?x)) as ?r)"
//                 + "bind (define(st:fac(?x)  = if (?x = 0, 1, ?x * st:fac(?x - 1))) as ?fun) "
                 + ""
                 + "}";
         
         String query2 = 
                 "select "
                 + "(let (?f = define (st:fac(?x)  = if (?x = 1, 1, ?x * st:fac(?x - 1))), "
                 + " let (?g = define (st:test(?x) = let(?y = ?x * ?x, ?y)), "
                 + "st:test(st:fac(?x)))) as ?r)"
                 + "where {bind (5 as ?x)}"
                 ;
        
         Mappings map = exec.query(query);
                     
         IDatatype dt = (IDatatype) map.getValue("?r");
         
         System.out.println(map);
       
    }
   
     public void testFFFGGG() throws EngineException, LoadException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        String init = "prefix ex: <http://example.org/>"
                + "insert data {"
                + "ex:a a ex:Case "
                + "ex:a ex:p ex:b "
                + "ex:b ex:q ex:c "
                + "ex:c ex:r ex:a "
                + ""
                + "ex:c a ex:Case "
                + "ex:c ex:p ex:d "
                + "ex:d ex:q ex:e "
                + "ex:e ex:r ex:c "
                
                + "ex:e a ex:Case "
                + "ex:e ex:p ex:f "
                + "ex:f ex:q ex:g "
                + "ex:g ex:r ex:e "
                + "}";
        
        
        String q = "prefix ex: <http://example.org/>"
                + "select *"             
                + "where {"
                + "bind ("
                + "define(xt:test(?x, ?n, ?m) ="
                + "if (?m >= ?n, true,"               
                + "exists { "
                + "?x ex:p ?y  "
                + "?y ex:q ?z  "
                + "?z ex:r ?x   "
                + "filter xt:test(?z, ?n, ?m + 1)}"
                + ")) as ?fun)"
                + ""
                + "?x a ex:Case "
                + "filter xt:test(?x, 2, 0)"
                + "}";
        
        String qq = "prefix ex: <http://example.org/>"
                + "select *"             
                + "where {"
                + "bind ("
                + "define(xt:test(?x, ?t, ?n, ?m) ="
                + "if (?m >= ?n, false,"               
                + "exists { "
                + "?x ex:p ?y  "
                + "?y ex:q ?z  "
                + "?z ex:r ?x   "
                + "filter (?z = ?t || xt:test(?z, ?t, ?n, ?m + 1))}"
                + ")) as ?fun)"
                + ""
                + "?x a ex:Case "
                + "filter xt:test(?x, ex:g, 2, 0)"
                + "}";
        
       
        
        exec.query(init);
        Mappings map = exec.query(q);
         System.out.println(map);
         System.out.println(map.size());

    }
   
   
   
      public void testFFF() throws EngineException, LoadException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
String init = "prefix ex: <http://example.org/>"
        + "insert data {"
        + "ex:John foaf:knows ex:Jack "
        + "ex:Jack foaf:knows ex:Jim"
        + "}";

        String qq = "select * where {"
                + ""
               
                + "function xt:test(?x) { "
                + "let (?x as ?r){ "
                + " exists {?x ?q ?y filter xt:test(?y)}"
                + "}"
                + " }"
                + "function xt:candidate(?q, ?t, ?b) { "
                + "let (?t as (?s, ?p, ?o)){ "
                + "map(kg:display, xt:list(?s, ?p, ?o))"
                + "}"
                + "}"
                + "?x ?p ?y "
                + "?y ?t ?z "
                + "filter xt:test(?y)"
                + "}";
        
        
        

        exec.query(init);
        Mappings map = exec.query(qq);
         System.out.println(map);
       System.out.println(map.size());

    }

   
  
   
   
   
   
   
   public void testMess(){
       String result = MessageFormat.format("Text avec {0} variable {1}", "partie", "ytyt");
       System.out.println(result);
   }
   
   
   
   
 public void testCal2() throws EngineException, LoadException {
        Graph g = createGraph(); 
        QueryLoad ql = QueryLoad.create();
        String q = ql.read(data + "query/cal.rq");
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?fr");
        assertEquals("Vendredi", dt.stringValue());
            //System.out.println(Interpreter.getExtension());
            String qq = "prefix cal: <http://ns.inria.fr/sparql-extension/calendar/>"
            +"select *"
                    + "where {"
                    + "?x ?p ?y "
                    + "filter (cal:jour(?y) = 'Mardi' )"
                    + "}";
            
            String init = "insert data { "
                    + "<Day1> rdf:value '2015-06-16'^^xsd:date ."
                    + "<Day2> rdf:value '2015-06-17'^^xsd:date ."
                    + "<Day3> rdf:value '2015-06-23'^^xsd:date ."
                    + "}";
            exec.query(init);
 
                       System.out.println(Interpreter.getExtension());

        //Mappings   m = exec.query(qq);
           //assertEquals(2, m.size());
            
        }  
     
     
     
     
     
     
     
     
     
     public void testBGP() throws EngineException{
         fr.inria.edelweiss.kgenv.parser.Transformer.ISBGP = true;
         String init = "prefix ex: <http://example.org/>"
                 + "insert data {"
                 + "graph ex:g1 {ex:John ex:name 'John' ;"
                 + "ex:author ex:d1, ex:d4 .}"
                 + "graph ex:g2 {ex:Jack ex:name 'Jack' ;"
                 + "ex:author ex:d2 ."
                 + "ex:John  ex:author ex:d3 }"                 + ""
                 + "}";
         
         String q =  "prefix ex: <http://example.org/>"
                 + "select  distinct ?x  "
                 + "from ex:g1 "
                 + "where {"
                 + "?x ex:author ?y "
                 + "?x ex:name ?n "
                // + "filter (?y != ex:d1)"
                 + "}";
         
         Graph g = Graph.create();
         QueryProcess exec = QueryProcess.create(g);
         
         exec.query(init);
         
         Mappings map = exec.query(q);
         System.out.println(map);
         System.out.println(map.getQuery());
     }
     
     
     
     
     
    public void testOWLRLTC() throws EngineException, LoadException {
        Graph g = createGraph();
        Load ld = Load.create(g);
        //System.out.println("Load");
        ld.loadWE(data + "template/owl/data/primer.owl"); 
        QueryProcess exec = QueryProcess.create(g);
        
         String t1 ="prefix f: <http://example.com/owl/families/> "
                + "template  {  st:apply-templates-with(st:owlrl)}"
                + "where {"
                 + "filter st:visit(st:trace)"
                 + "}";
         
        String q = "select   *"
                + "(define (xt:candidate(?q, ?t, ?b) = kg:display(?t)) as ?ff)" +
" where {\n" +
"bind  (\n" +
"  <http://example.com/owl/families/John2> as ?x\n" +
")\n" +
"  ?x ?p ?y\n" +
"  \n" +
" \n" +
"}";
         
        Mappings map = exec.query(q);
          System.out.println(map);
     }
        
        public void testPSPIN() throws EngineException{
            String q = "select * where {"
                    + "?x ?p <http://example.org?x=1&y=2&z=3>"
                    + "filter (?x && ?p <= ?x + 12)"
                    + "}";
            
            SPINProcess sp = SPINProcess.create();
            Graph g = sp.toSpinGraph(q);
            
            String t = "template {"
                  //  + "st:call-template-with(st:spin, st:mode, st:html)"
                    + "st:apply-templates-with(st:hturtle)"
                    + "}"
                    + "where {}";
          
            QueryProcess exec = QueryProcess.create(g);
            Mappings map = exec.query(t);
            System.out.println(map.getTemplateStringResult());
            
        }
        
        
        
        public void testGeneralize() throws EngineException, LoadException, ParserConfigurationException, SAXException, IOException{
             Graph g = Graph.create(true); 
             QueryProcess exec = QueryProcess.create(g);
             String init = "prefix ex: <http://example.org/>"
                     + "insert data { "
                     + "ex:John a ex:OldMan ;"
                     + "ex:author [ a ex:Document ]"
                     + "ex:Jack a ex:Person ;"
                     + "ex:author [ a ex:Document ]"                     
                     + "ex:Man       rdfs:subClassOf ex:Human "
                     + "ex:YoungMan  rdfs:subClassOf ex:Man "
                     + "ex:OldMan    rdfs:subClassOf ex:Man "
                     + "ex:Report    rdfs:subClassOf ex:Document  "
                     + "}";

            // target type more general than query
            // target type brother of query
             String qq = "prefix ex: <http://example.org/>"
                     + "select * (kg:similarity() as ?sim) "
                     + "(define (xt:candidate(?q, ?t, ?b) = "
                     + "let (?qo = xt:object(?q),"
                     + "let (?to = xt:object(?t),"
                     + "if (xt:property(?q) = rdf:type && isURI(?qo), "
                     + "?b || exists { ?qo rdfs:subClassOf/(rdfs:subClassOf*|^rdfs:subClassOf) ?to } ,"
                     + "?b))))"
                     + "as ?f)"
                    
                     + "where {"
                     + "?x a ex:YoungMan, ?tt ;"
                     + "ex:author [ a ex:Report ] "
                     + ""
                     + "}";
             
             String q = "prefix ex: <http://example.org/>"
                     + "select * (kg:similarity() as ?sim) "
                     + "(define (xt:match(?qo, ?to) = "
                     + "exists { {?to rdfs:subClassOf* ?qo } "
                     + "union { ?qo rdfs:subClassOf/(rdfs:subClassOf*|^rdfs:subClassOf) ?to }})"
                     + "as ?f)"
                    
                     + "where {"
                     + "?x a ?t "
                     + "filter xt:match(?t, ex:YoungMan)"
                     + "?x ex:author [ a ?r ] "
                     + "filter xt:match(?r, ex:Report)"
                     + "}";
             
             
           String q2 =  "prefix ex: <http://example.org/>"+ 
                   "select more *"
                   + "(kg:similarity() as ?s)  "
                   + "where {"
                     + "?x a ex:YoungMan, ?tt ;"
                     + "ex:author [ a ex:Report ] "
                   + "}"
                   + "order by desc(?s)" ;
           
            exec.query(init);
            Mappings map = exec.query(q2);
            System.out.println(map);
            System.out.println(map.size());
            
        }
        
        
        public void testBBB() throws EngineException{
             Graph g = createGraph(); 
             QueryProcess exec = QueryProcess.create(g);
             String init = "insert data { <John> rdf:value 1, 2, 3, 4, 5, 6, 7, 8 .}";
             String q =  
                "select * "                     
                     + "(define (xt:foo(?n) = exists {?x rdf:value ?n}) as ?g)"
                     + "(xt:iota(1, 10, 2) as ?l)"            
              + "where {"
                     + "?x rdf:value ?y "
                     + "filter (xt:foo(10) || xt:foo(5))"
             
              + "} limit 1";
             
             q = "select "
                                  
                     + "(apply  (xt:sum(), maplist (xt:fun(?x), xt:iota(0, 12))) as ?res)"
                     + "(define (xt:fac(?n) = if (?n = 0, 1, ?n *  xt:fac(?n - 1))) as ?fac)"
                     + "(define (xt:fun(?x) = 1.0 / xt:fac(?x)) as ?fun)"
                     + "where {}";
             
             q = "SELECT ?f\n" +
                      "(define (xt:fac(?n) = if (?n = 0, 1, ?n *  xt:fac(?n - 1))) as ?fac)" +
"WHERE {\n" +
 //                    "  BIND (\n" +
//"    unnest(xt:iota(1, 10)) \n"
//"  AS ?f)\n" +
                      "values ?f {1 2 3 4 5 6 7 8 9 10}" +
"}";
             
             String qq = "select "
                     + "(define (xt:proc(?x) = st:apply-templates-with(st:turtle, ?x)) as ?t)"
                     + "(apply(concat(), maplist(xt:proc(?x), xt:list(<John>, <Jim>))) as ?cc)"
                     + "where {}";
             
             exec.query(init);
            Mappings map = exec.query(q);
            
            System.out.println(map);
            System.out.println(map.size());
            //System.out.println(map.getQuery());
            //System.out.println(ResultFormat.create(map));
            
        }
            
        
        
        
        
        
        
        /**
         * Time : 0.5833
         * Time : 0.5394
         * 
         * 
         * */
        
        
        public void testFib2(){
             Date d1 = new Date();
            int n = 10;
            int res = -1;
            for (int i = 0; i<10; i++){
                res = fib(30);
            }
            Date d2 = new Date();
            System.out.println("Time : " + (d2.getTime() - d1.getTime()) / (n * 1000.0));
            System.out.println(res);
        }
        
  
        
 public void testCal() throws EngineException, LoadException {
        Graph g = createGraph(); 
        QueryLoad ql = QueryLoad.create();
        String q = ql.read(data + "query/cal.rq");
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?fr");
        assertEquals("Vendredi", dt.stringValue());
            //System.out.println(Interpreter.getExtension());
            String qq = "select *"
                    + "where {"
                    + "?x ?p ?y "
                    + "filter (xt:jour(?y) = 'Mardi' )"
                    + "}";
            
            String init = "insert data { "
                    + "<Day1> rdf:value '2015-06-16'^^xsd:date ."
                    + "<Day2> rdf:value '2015-06-17'^^xsd:date ."
                    + "<Day3> rdf:value '2015-06-23'^^xsd:date ."
                    + "}";
            exec.query(init);
            
            
            
            String db = 
                    "prefix p:  <http://fr.dbpedia.org/property/> "
                    + "select *"
                    
                    + "where {"
                    + ""
                    + "service <http://fr.dbpedia.org/sparql>  {"
                    + "select *  where {"
                    + "?x rdfs:label ?l ; ?p ?v "
                    + "} limit 5"
                    + "}"
                    + "bind (function (xt:service(?n, ?s, ?m) = "
                    + "if (?n = <http://fr.dbpedia.org/sparql>,"
                    + "map (kg:display, xt:list(?n,  ?m)), true)) as ?def)"
                    + "}"
                    + "values ?l { 'Louis XIV'@fr }";

            
        Mappings   m = exec.query(db);
            //System.out.println(m);
            
//       String s = "fun(bb + a*c)"    ;
//            String[] res = s.split("\(");
//            for (String a : res){
//                System.out.println(a);
//            }
        }
        

    public void testPPSPINdfgsdfg() throws EngineException, LoadException {
        Graph g = createGraph();
        Load ld = Load.create(g);
        //System.out.println("Load");
        //ld.load(data + "template/owl/data/primer.owl"); 
        QueryProcess exec = QueryProcess.create(g);
        
         String t1 ="prefix f: <http://example.com/owl/families/> "
                + "template  {  st:apply-templates-with('/home/corby/AATest/template/turtle/template')}"
                + "where {}";
                   
         
         String query = 
                 "select "
                 + "(let (?f = define (st:fac(?x)  = if (?x = 1, 1, ?x * st:fac(?x - 1))), "
                 + "st:fac(?x)) as ?r)"
                 + "where {bind (5 as ?x)}"
                 + "pragma {"
                // + "filter (define (st:fac(?x)  = if (?x = 1, 1, ?x * st:fac(?x - 1))))"
                 + "filter (define (st:test(?x) = let(?y = ?x * ?x, ?y)))"
                 + "}";
         
         String q = 
                 "select (package ( "
                 + "define (st:fib(?n) = if (?n <= 2, 1, st:fib(?n - 1) + st:fib(?n - 2))),"
                 + "define (st:test() = 12)"
                 + ") as ?p)"
                 + "(st:fib(10) as ?fib)"
                 + "where {}";
         
          String qq =
                   "template  {  st:apply-templates-with('/home/corby/AATest/transform/test')}"
                + "where {}";
          
         Mappings map = null;
         for (int i=0; i<1; i++){
            map = exec.query(q);
         }
          System.out.println(map);  
          System.out.println(ResultFormat.create(map));  
                    System.out.println(map.getQuery());  

          
           //System.out.println(map.getTemplateStringResult());     
       
    }
              //  @Test
        public void ttest(){
            HashMap<String, String> m = new HashMap();
            String s = "abc";
            m.put(s, s);
            ArrayList<String> l = new ArrayList();
            Variable v = new Variable("?x");
            for (int i = 0; i < 1000000; i++){
                m.get(s);
                l.add(s);
                l.remove(0);
                v.equals(v);
            }
            
            
        }
        
        int fib(int n){
            if (n <= 2){
                return 1;
            }
            return fib(n - 1) + fib(n - 2);
        }
    
    
    
    public void testohdfoshfoi() throws EngineException{
        String q = "select * where {"
                + "{?x ?p ?y} union "
                + "{?x ?q ?z}  union "
                + "{?z ?r ?t}"
                + "}";
        
        QueryLoad ql = QueryLoad.create();
        q = ql.read(data + "template/spintc/query/test2.rq");
        System.out.println(q);
        SPINProcess sp = SPINProcess.create();
        String str = sp.toSpin(q);
        System.out.println(str);
        
        String str2 = sp.toSpinSparql(q);
        System.out.println(str2);
        
        QueryProcess exec = QueryProcess.create(Graph.create());
        Query qq = exec.compile(q);
        System.out.println(qq.getAST());
    }
    
    public void testLoadfghfg() {
        String path = data + "work/test.owl";
        Graph g = Graph.create();
        Load ld = Load.create(g);
        try {
            ld.loadWE(path, Load.TURTLE_FORMAT);
        } catch (LoadException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(g);
    }
    
    
    public void testMathghfgh() throws LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
//ld.setLimit(10000);
        try {
            ld.loadWE("/home/corby/scale1000.ttl");
        } catch (LoadException e1) {
            e1.printStackTrace();
        }
QueryLoad ql = QueryLoad.create();
String qq = ql.read("/home/corby/AData/work/fuqi/test.rq");

ld.loadWE("/home/corby/AData/work/fuqi");
QueryEngine qe = ld.getQueryEngine();

int i = 0;
for (Query q : qe.getQueries()){
        
        //System.out.println(q.getAST());

        QueryProcess exec = QueryProcess.create(g);

        
            Mappings map = exec.query(q);
            //Node node = map.getTemplateResult();

            System.out.println(i++ + " : " + map.size());
//            System.out.println(map.getQuery().getAST());
//            System.out.println(map.getQuery());

            //assertEquals("result", true, node.getLabel().length() > 10);

        
}


    }
    
   
    public void sidfiqsdhfkjsh() throws EngineException, UnknownHostException, LoadException, IOException{
        Graph g = Graph.create();
        Load ld = Load.create(g);
        Date d1 = new Date();
        System.out.println("start load");
        //ld.loadWE("/home/corby/AData/work/eliot/root-ontology.owl");
//        ld.loadWE("file:///home/corby/AData/template/owl/data/primer.owl");
//        ld.loadWE("file:///home/corby/ADataServer/tutorial/human.rdfs");
        ld.loadWE("http://localhost:8080/data/tutorial/human.rdfs");
        Date d2 = new Date();
        System.out.println("Time : " + (d2.getTime() - d1.getTime()) / (1000.0));

        System.out.println(g.size());
        
        NSManager nsm = NSManager.create();
       // nsm.definePrefix("op", "http://www.imag.fr/ontoprax#");
        TripleFormat tf = TripleFormat.create(g, nsm);
       // tf.without("http://www.w3.org/2004/02/skos/core");
       tf.without("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        
        tf.write("/home/corby/tmp.ttl");
        
        
        String init = "load rdf: ; load rdfs: ";
        QueryProcess exec = QueryProcess.create(g);
        //exec.query(init);
        

    }
    
    GraphStore createGraph(){
        GraphStore g = GraphStore.create();
        Parameters p = Parameters.create();
        p.add(Parameters.type.MAX_LIT_LEN, 2);
        g.setStorage(IStorage.STORAGE_FILE, p);
        return g;
    }
    
    
       
    public void testTCff () throws EngineException, LoadException{
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);
        Load ld = Load.create(gs);
        ld.loadWE(data + "template/owl/data/primer.owl");
        
       Transformer t = Transformer.create(gs, Transformer.OWLRL);
        
       IDatatype dt = t.process();
        
         System.out.println(dt);
        
     }
    
    
   
     public void testaggand() throws EngineException {
        Graph g = Graph.create();
        Load ld = Load.create(g);

        try {           
            ld.loadWE(data + "template/owlrl/data/success.ttl");                                
        } catch (LoadException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        QueryLoad ql = QueryLoad.create();
        String query = "template{ st:atw(st:owlrl) } where{ }";
        
        QueryProcess exec= QueryProcess.create(g);
        
        Mappings map = exec.query(query);
        
        System.out.println(map.getTemplateStringResult());
        
    }
    
    
    public void testfghfghfghfhgfg() throws EngineException {
        Graph g = Graph.create();
        Load ld = Load.create(g);

        try {           
            ld.loadWE("/home/corby/Texte/KGram/xslt/data/xml.ttl");                                
        } catch (LoadException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        QueryLoad ql = QueryLoad.create();
        String query = ql.read("/home/corby/Texte/KGram/xslt/query/q4.rq");
            
        query = "template{ st:atw(st:turtle) } where{ }";
        
        for (Entity ent : g.getEdges()){
            IDatatype dt = (IDatatype) ent.getNode(1).getValue();
            if (dt instanceof CoreseStringLiteral){
                
                System.out.println(dt + " " + ((CoreseStringLiteral) dt).getManager());
            }
        }
       
        Graph g2 = createGraph();
        
        g2.copy(g);
        
       // System.out.println(g2.display());
        
        QueryProcess exec = QueryProcess.create(g2);
        
        Mappings map = exec.query(query);

        //System.out.println(map.getTemplateStringResult());
        
        for (Entity ent : g2.getEdges()){
            IDatatype dt = (IDatatype) ent.getNode(1).getValue();
            if (dt instanceof CoreseStringLiteral){
                
                System.out.println(dt + " " + ((CoreseStringLiteral) dt).getManager());
            }
        }
        
         for (Entity ent : g.getEdges()){
            IDatatype dt = (IDatatype) ent.getNode(1).getValue();
            if (dt instanceof CoreseStringLiteral){
                
                System.out.println(dt + " " + ((CoreseStringLiteral) dt).getManager());
            }
        }
    }
    
    public void testskdghfkjsdf() throws EngineException{
        Graph g = Graph.create();
         Parameters p = Parameters.create();
        p.add(Parameters.type.MAX_LIT_LEN, 2);
        g.setStorage(IStorage.STORAGE_FILE, p);
        QueryProcess exec = QueryProcess.create(g);
        
        String init = "insert data {"
                + "graph <g1> { <John> rdfs:label 'John'@en }"
                + "graph <g2> { <Jim> rdfs:label 'Jim'@fr }"
                + "graph <g3> { <John> rdfs:label 'John2' }"
                + "}";
        
        exec.query(init);
        
        String q = "select * "
                + "(concat(str(?x), ' '  , ?l) as ?res) "
                + "from <g1> "
                + "from <g2> "
                + "from <g3> "
                + "where {?x rdfs:label ?l}";
        
        Mappings map = exec.query(q);
        
        System.out.println(map);
    }
    
    public void testcpl() throws EngineException, LoadException{
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load l = Load.create(g);
        l.loadWE(data + "template/owl/data/primer.owl");
        //String str = ql.read(data+"work/template/indent/start.rq");
        String str = "template {?n ; separator = ''} where {?x a ?t optional { ?x foaf:name ?n}}";
        String str2 = "select (group_concat(?n) as ?out) where {?x a ?t optional { ?x foaf:name ?n}}";
        Mappings map  = exec.query(str);
        System.out.println(map.getQuery().getAST());
        System.out.println(map.size());
        //System.out.println(map);
       System.out.println(map.getTemplateStringResult().length());
        Node n = map.getTemplateResult();
        IDatatype dt = (IDatatype) n.getValue();
        System.out.println(dt.getStringBuilder().length());
    }
    
    
    
    
    
    public void testPPSPINdfgdgfd() throws EngineException, LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        //System.out.println("Load");
        ld.loadWE(data + "template/spinhtml/data/"); 
        QueryProcess exec = QueryProcess.create(g);
        
         String t1 ="prefix f: <http://example.com/owl/families/> "
                + "template  {  st:apply-templates-with(st:spin)}"
                + "where {}";
                   
         
         Mappings map = exec.query(t1);
         System.out.println(map.getTemplateStringResult());
         assertEquals(2096, map.getTemplateResult().getLabel().length());       
        
    }
        
    
    public void testJSONLD() throws LoadException, EngineException {
        Graph g = Graph.create(true);

        QueryProcess exec = QueryProcess.create(g);

        String init = FOAF_PREF
                + "prefix ex: <http://example.org/> "
                + "insert data {"
                + "foaf:knows rdfs:domain foaf:Person ; rdfs:range foaf:Person ."
                + "[] foaf:knows ex:Jim "
                + "ex:Jim foaf:knows <James> "
                + "<Jack> foaf:knows ex:Jim "
                + "<James> a foaf:Person ; foaf:name 'James', 'James'@en ; foaf:age 10"
                + "}";

        exec.query(init);

        JSONLDFormat jf = JSONLDFormat.create(g);
        System.out.println(jf);

    }
    
    
    
     public void testOWLRLfghnghj() throws LoadException, EngineException{
          GraphStore g = GraphStore.create();
        Load ld = Load.create(g);
        ld.loadWE(data + "template/owl/data/primer.owl");
        RuleEngine re = RuleEngine.create(g);
        re.setProfile(RuleEngine.OWL_RL_LITE);
        re.process();
        
        QueryLoad ql = QueryLoad.create();
        String str = ql.read(data + "template/turtlehtml/template/value.rq");
        
        String q = "template{st:call-template-with(st:turtlehtml, st:value)}"
                + "where {}";
        
        QueryProcess exec = QueryProcess.create(g);
       
        
       
        String qqq = "select distinct ?pr  "
                + "from kg:rule "
                + "where { "
                + "?x ?p ?y bind (kg:provenance(?p) as ?pr) "
              //  + "graph ?pr { [] sp:predicate ?q }"
                + "} order by ?q";
        
   
        Mappings map = exec.query(q);
        
        System.out.println(map.size());
        
       
     }
    
    
    
    
    
    public void myastpp() throws LoadException, EngineException {
        GraphStore graph = GraphStore.create();
        QueryProcess exec = QueryProcess.create(graph);
        
        String init = "insert data {"
                + "<John> foaf:knows <Jim>, <Jack> "
                + "graph st:test "
                + "{"
                + "<John> rdfs:label 'John' "
                + ""
                + "}"       
                + "}";
                                     
        exec.query(init);
        
        Graph g = GraphStore.create();
        QueryProcess exec2 = QueryProcess.create(g);
        
         String init2 = "insert data {"
                + "<Jim>  foaf:knows <Jack>, <James> "
                 + "<Jack> foaf:knows <Jesse>"
                 + "<John> rdfs:label 'toto'"  
                 
                + "}";
                                     
        exec2.query(init2);
        
        graph.setNamedGraph(NSManager.STL + "sys", g);
        
        
        String q = "template {"
                + "str(?res)"
                + "}"
                + "where {"
                + "graph st:sys {"
                + "bind (st:atw('/home/corby/AData/template/test') as ?res)"
                + "}"
                + "}";       
        Mappings map = exec.query(q);
        System.out.println(map);
        assertEquals(map.getTemplateStringResult().length(), 0);
        

    }
    
    
    

    
    public void mytest() throws LoadException, EngineException {

        Graph graph = Graph.create();
        
        Load ld = Load.create(graph);
        ld.loadWE("/home/corby/AData/sdk/sdk.rdf");

        QueryProcess exec = QueryProcess.create(graph);
        String query = "select * where {"
                + "?x ?q ?z ."
                
                + "}"
                + "limit 10";
        
        String temp = "prefix c: <http://www.inria.fr/acacia/sdk#>\n" +
"template {\n" +
"  ?a1 \n" +
"}\n" +
"where {\n" +
"\n" +
"?s c:cell \n" +
" ?a1 , ?a2 , ?a3 , ?a4 , ?a5 , ?a6 , ?a7 , ?a8 , ?a9 ,\n" +
" ?b1 , ?b2 , ?b3 , ?b4 , ?b5 , ?b6 , ?b7 , ?b8 , ?b9 ,\n" +
" ?c1 , ?c2 , ?c3 , ?c4 , ?c5 , ?c6 , ?c7 , ?c8 , ?c9 ,\n" +
" ?d1 , ?d2 , ?d3 , ?d4 , ?d5 , ?d6 , ?d7 , ?d8 , ?d9 ,\n" +
" ?e1 , ?e2 , ?e3 , ?e4 , ?e5 , ?e6 , ?e7 , ?e8 , ?e9 ,\n" +
" ?f1 , ?f2 , ?f3 , ?f4 , ?f5 , ?f6 , ?f7 , ?f8 , ?f9 ,\n" +
" ?g1 , ?g2 , ?g3 , ?g4 , ?g5 , ?g6 , ?g7 , ?g8 , ?g9 ,\n" +
" ?h1 , ?h2 , ?h3 , ?h4 , ?h5 , ?h6 , ?h7 , ?h8 , ?h9 ,\n" +
" ?i1 , ?i2 , ?i3 , ?i4 , ?i5 , ?i6 , ?i7 , ?i8 , ?i9 .\n" +
"}\n" +
"limit 1 "
                + "values (?a1 ?a2) { (1 UNDEF) }";
       
        
//            Mappings map = exec.query(query);
//            //System.out.println(map);
//            
//           RDFResultFormat res = RDFResultFormat.create(map);
//           //System.out.println(res);
//           
//           Graph g = Graph.create();
//           Load l = Load.create(g);
//           l.loadString(res.toString(), Load.TURTLE_FORMAT);
//           //System.out.println(graph);
//           
//           Transformer t = Transformer.create(graph, "/home/corby/AData/template/result");
           //System.out.println(t);
           
           Mappings map = exec.query(temp);
           System.out.println(map.getTemplateStringResult());
    }
    
public void testQL() throws LoadException{
        System.out.println("TEST");
        Load ld = Load.create(Graph.create());
        ld.setDebug(true);
        ld.loadWE(data + "cdn/query");
        QueryEngine qe = ld.getQueryEngine();
        System.out.println(qe.getQueries().size());
        for (Query q : qe.getQueries()){
            System.out.println(q.getAST());
        }
    }
    
      public void testvALUES() throws EngineException, LoadException{
        Graph g = Graph.create();
         Load ld = Load.create(g);
        ld.loadWE("/home/corby/AData/cdn/data");
       QueryProcess exec = QueryProcess.create(g);
        
        
       String q = "prefix p:  <http://fr.dbpedia.org/property/>\n"
            + "select  * where { \n"
            + "  service <http://localhost:8080/kgram/sparql>  {"
            + "    <http://fr.dbpedia.org/resource/Emmanuel-Philibert_de_Savoie_(1528-1580)> p:successeur+ ?y "
            //   + "filter(?x = <http://fr.dbpedia.org/resource/Emmanuel-Philibert_de_Savoie_(1528-1580)>)"
            + "   }"
            + "}"
          // + "values ?x { <http://fr.dbpedia.org/resource/Emmanuel-Philibert_de_Savoie_(1528-1580)> }"
            + "";
       
        String q2 = "prefix p:  <http://fr.dbpedia.org/property/>\n"
            + "select  * where { \n"
           // + "  service <http://localhost:8080/kgram/sparql>  {"
               + "values ?x { <http://fr.dbpedia.org/resource/Emmanuel-Philibert_de_Savoie_(1528-1580)> }"
        + "    ?x p:successeur+ ?y "
              // + "filter(?x = <http://fr.dbpedia.org/resource/Emmanuel-Philibert_de_Savoie_(1528-1580)>)"
           // + "   }"
            + "}"
            //   + "values ?x { <http://fr.dbpedia.org/resource/Emmanuel-Philibert_de_Savoie_(1528-1580)> }"
            + "";
        
        Mappings map = exec.query(q2);
       // map = exec.query(q2);
          System.out.println(map);
      }
      
      
      
        
       
    public void testPPSPINfghdfgh() throws EngineException, LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        //System.out.println("Load");
        ld.loadWE(data + "template/spin/data/"); 
        QueryProcess exec = QueryProcess.create(g);
        
         String t1 ="prefix f: <http://example.com/owl/families/> "
                + "template  {  st:apply-templates-with(st:spin)}"
                + "where {}";
                   
         
         Mappings map = exec.query(t1);
            System.out.println(map.getTemplateStringResult());
         assertEquals(2096, map.getTemplateResult().getLabel().length());       
        
    }
        
    
    
       public void testGTT() throws LoadException, EngineException {

            Graph g = Graph.create();
            Load ld = Load.create(g);
            ld.loadWE(RDF.RDF, Load.TURTLE_FORMAT);
            ld.loadWE(RDFS.RDFS, Load.TURTLE_FORMAT);
            
            Transformer t = Transformer.create(g, Transformer.TURTLE, RDF.RDF);
            String str = t.transform();
            System.out.println(str);
            assertEquals(3821, str.length());
            
//            t = Transformer.create(g, Transformer.TURTLE, RDFS.RDFS);
//            str = t.transform();
//            //System.out.println(str);
//            assertEquals(3164, str.length());
//            
//             t = Transformer.create(g, Transformer.TURTLE);
//            str = t.transform();
//            //System.out.println(str);
//            assertEquals(6940, str.length());
       }     
    
     public void testGI() throws EngineException{
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);
        
        String init = "prefix ex: <http://example.org/>"
                + "insert data {"
                + "foaf:John foaf:name 'John' "
                + "foaf:James foaf:name 'James' ; foaf:knows foaf:John "
                
                + "}";
        
        exec.query(init);
        
        String sq =  "template {} "
                + "where {"
                + "filter(st:visit(st:start, st:trace)) "
                + " ?x foaf:name ?n "
                + " ?x foaf:knows ?z "
                + "filter(st:visit(st:exp, ?x))"
               // + "filter(kg:display(?z))"
                
                + "} "
                + "values ?x {<John>}" ;
        
        Mappings map = exec.query(sq);
       //System.out.println(map);
       Query q = map.getQuery();
       Transformer t = (Transformer) q.getTransformer();
       TemplateVisitor tv = t.getVisitor();
       //    System.out.println(tv.visited());
    }
    
   public void testListdfg(){
       Graph g = Graph.create();
       ArrayList<Node> l = new ArrayList<Node>();
       l.add(g.addResource("n1"));
       l.add(g.addResource("n2"));
       Node h = g.addList(l);
       System.out.println(g.display());
       System.out.println(g.getList(h));
   }
   
   
       public void testPDurv() throws LoadException  {

        RuleEngine engine = RuleEngine.create(Graph.create());
        RuleLoad ld2 = RuleLoad.create(engine);
        File rulesDir = new File(data + "work/testrule");
        for (File currentRule : rulesDir.listFiles()) {
                    ld2.loadWE(currentRule.getAbsolutePath());
        }
        for (Rule r : engine.getRules()){
            System.out.println(r.getAST());
        }
    }
    
    
       
       
       
    public void testJoinDistinctfghfdgh() {
        String init =
                "insert data {"
                + "<John> rdfs:label 'John', 'Jack' "
                + "}";

        String query =
                "select  distinct ?x where {"
                + "?x rdfs:label ?n "
                + "{?x rdfs:label ?a} "
                + "{?x rdfs:label ?b} "
                + "}";

        

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            //System.out.println(map);
            assertEquals("Result", 1, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }
 
 
    
 public void testOWLRLfgh() throws LoadException, EngineException{
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.loadWE(data + "template/owl/data/primer.owl");
          
       QueryLoad ql = QueryLoad.create();
       String q = ql.read(data + "template/owlrl/query/test2.rq");
       
       QueryProcess exec = QueryProcess.create(g);
       Mappings map = exec.query(q);
       
        //System.out.println(map.getTemplateStringResult());
        
        Query qq = map.getQuery();
        Transformer t = (Transformer) qq.getTransformer();
        DefaultVisitor tv = (DefaultVisitor) t.getVisitor();
        
                  System.out.println();

       for (IDatatype dt : tv.visited()){
       Transformer tt = Transformer.create(g, Transformer.TURTLE);
           System.out.println(tt.process(dt).getLabel());
       }
       
       
   }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public void test10dfgdhf() throws LoadException {
Graph g = Graph.create(true);
init(g);
        String query = 
                "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select  *  where {"
      + "bind (kg:unnest(kg:sparql('"
                + "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select * where {?x rdf:type c:Person; c:hasCreated ?doc}')) "
      + "as (?a, ?b))"
        + "} ";
        
        query = 
                "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select  *  where {"
      + "bind ((kg:sparql('"
                + "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "construct  where {?x rdf:type c:Person; c:hasCreated ?doc}')) "
      + "as ?g)"
                + "graph ?g { ?a ?p ?b }"
        + "} ";

        try {

            QueryProcess exec = QueryProcess.create(g);

            Mappings map = exec.query(query);
            System.out.println(map.getQuery().getAST());
            System.out.println(map);
            assertEquals("Result", 3, map.size());
            Node node = map.getNode("?a");
            assertEquals("Result", true, node != null);

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }
    
     public void testTrsdqsdf() throws EngineException{
        GraphStore gs = GraphStore.create();
        gs.setHasList(true);
        QueryProcess exec = QueryProcess.create(gs);
        
        String init = "prefix ex: <http://example.org/>"
                + "insert data {"
                + "ex:C4 owl:unionOf (ex:C5 ex:C6 ex:C5) "
               
                + "}";
        
        exec.query(init);
        
        String q = "select * where {"
                + "select *"
                + "  where { "
                + "    ?x owl:unionOf ?l "
                + "    ?l rdf:rest*/rdf:first ?e"
                + "    }"
                + "}"
                ;
        
        Mappings map = exec.query(q);
        System.out.println(map);
     }
    
    public void testTr() throws EngineException{
        GraphStore gs = GraphStore.create();
        gs.setHasList(true);
        QueryProcess exec = QueryProcess.create(gs);
        
        String init = "prefix ex: <http://example.org/>"
                + "insert data {"
                + "ex:C4 owl:unionOf (ex:C5 ex:C6) "
                + "ex:C0 owl:unionOf (ex:C2 ex:C3) "
                + "ex:C1 owl:unionOf (ex:C2 ex:C3) "
                + "}";
        
        exec.query(init);
        
        String q = "select * where {"
                + "select *"
                + "  where { "
                + "    ?x owl:unionOf (?c1 ?c2)  ;"
                + "    owl:unionOf ?l"
                + "    }"
                + "    group by (st:apply-templates-with(st:hash2, ?l) as ?exp)"
                + "}"
                ;
        
        Mappings map = exec.query(q);
        
        System.out.println(map);
        assertEquals(2, map.size());
        
          Index e = gs.getIndex(1);
          int i = 0;
       for (Entity ent : e.getEdges(gs.getPropertyNode(RDF.RDF + "first"), null)){
           ent.getEdge().setIndex(i++);
       }
        
        Index ee = gs.getIndex(Graph.ILIST);
       for (Entity ent : ee.getEdges(gs.getPropertyNode(RDF.RDF + "first"), null)){
           System.out.println(ent);
       }

    }
    
   
    
     
    public void teststart() throws EngineException, IOException, LoadException {
        String init = "insert data {"
                + "<Jack> rdf:value <Jim> "
                + "<Jim> rdf:value <James> "
                + "<Joss> rdf:value <Jules> "
                + "}";
        
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        
        String q = "select * (kg:index(?x) as ?i) (kg:index(?r) as ?j) where {"
                + "?x rdf:value ?y "
                + "bind (rand() as ?r)"
               //  + "bind (rand() as ?r2)"
                
               
               
                + "}";
        
        Mappings map = exec.query(q);
        System.out.println(map);
        
       
        
        ProducerImpl p = (ProducerImpl) exec.getProducer();
         System.out.println(p.getLocalGraph());
    }
      
    
 
 
 
public void testcache(){
     ValueCache c = new ValueCache(1000);
     Date d1 = new Date();
     for (int i = 1;  i< 1000000 ; i++){
     IDatatype dt = DatatypeMap.newInstance(i);
     c.put(dt, dt);
     
      //System.out.println(c.get(dt));
         //System.out.println(c.size());
          //System.out.println(c.getList());
    }
          Date d2 = new Date();
        System.out.println("Load : " + (d2.getTime() - d1.getTime()) / (1000.0));

 }


 
        
public void testOWLRL() throws EngineException, IOException, LoadException {
        double d = 0.2;
        //System.out.println("Test: " + d);
        //ResultWatcher.LIMIT = d;
        GraphStore gs = createGraph();       
        Load ld = Load.create(gs);
        //ld.setLimit(50000);
        System.out.println("Load");
        Date d1 = new Date();
        try {
          //ld.loadWE(data + "template/owl/data/tmp.owl");    
          ld.loadWE(data + "fma/fma3.2.owl");    
            //ld.loadWE(data + "template/owl/data/primer.owl");
//            ld.loadWE(data + "work/dbpedia/dbpedia_3.9.owl");
//            ld.loadWE(data + "work/dbpedia/persondata_en_uris_de.ttl");

        } catch (LoadException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OutOfMemoryError e) {
            System.out.println("Out Of Memory: " + gs.size());
        }
        Date d2 = new Date();
        System.out.println("Load : " + (d2.getTime() - d1.getTime()) / (1000.0));

        System.out.println("Size: " + gs.size());
        System.out.println("NB Nodes: " + gs.getNodeIndex());

        d1 = new Date();
        gs.init();
        d2 = new Date();
        System.out.println("Index : " + (d2.getTime() - d1.getTime()) / (1000.0));
        System.out.println("Size: " + gs.size());

        //ld.load(data + "owlrule/owlrl.rul");
        //ld.load(data + "owlrule/owlrllite.rul");
        //ld.load(data + "owlrule/owlrl.rul");

        System.out.println("Clean ...");
        d1 = new Date();      
       //clean(gs);    
        RuleEngine re = RuleEngine.create(gs);
        //re.cleanOWL();
        re.setProfile(RuleEngine.OWL_RL_LITE);
        //re.setFunTransitive(false);
        //re.setOptTransitive(false);
       d2 = new Date();
        System.out.println("Clean : " + (d2.getTime() - d1.getTime()) / (1000.0));
        System.out.println("Size: " + gs.size());
       
       //re.setProfile(re.OWL_RL);
       // re.setTrace(true);
        //re.setConnect(true);
        
//        re.setOptTransitive(false);        
//        re.setFunTransitive(false);  
        //re.setSpeedUp(false);
        
        // Producer may return similar triples from different named graph. 
        // they will be filtered by construct triple exist checking
        //re.getQueryProcess().getProducer().setMode(Producer.SKIP_DUPLICATE_TEST);        
        System.out.println("Rule ...");
        d1 = new Date();
    
       new GraphUtil(gs).shoot(NSManager.KGRAM+ "d1");
       //shoot(gs, "/tmp/f1.txt");
       re.process();
      // shoot(gs, "/tmp/f2.txt");

       new GraphUtil(gs).shoot(NSManager.KGRAM+ "d2");

        d2 = new Date();
        
        System.out.println("Constraint: " + re.getConstraintViolation());

        for (Rule r : re.getRules()) {
            if (r.getTime() >= 1) {
                System.out.println(r.getIndex() + " " +  r.getTime() + "\n" + r.getAST());
            }
        }

        System.out.println("NB Rules: " + re.getRules().size());
        System.out.println("Graph: " + gs.size());
        System.out.println("Time: " + (d2.getTime() - d1.getTime()) / (1000.0));
        
        
     // test(gs);
       
       
    }
    
    void shoot(Graph g, String file) throws IOException{
        ArrayList <String> list = new ArrayList<String>();
        for (Entity uri : g.getNodes()){
            list.add(uri.getNode().getLabel());
        }
        Collections.sort(list);
        FileWriter f = new FileWriter(new File(file));
        for (String s : list){
            f.write(s);
            f.write(NL);
        }
        f.flush();
        f.close();
    }
    
    void test2(Graph g) throws EngineException {
/**
 * 
Query Time: 3.941
Enum from Time: 2.726
Enum std  Time: 1.05
 */

        QueryProcess exec = QueryProcess.create(g);
        String s = "select (count(*) as ?c) from kg:rule where { ?x ?p ?y "
                + "bind(kg:provenance(?p) as ?pr)"
                + "bind(kg:id(?pr) as ?i)"
                + "}";
        Date dd1 = new Date();
        Mappings map = exec.query(s);
        Query q = map.getQuery();
        Date dd2 = new Date();
        System.out.println(map);
        System.out.println("Query Time: " + (dd2.getTime() - dd1.getTime()) / (1000.0));
        
        Memory mem = new Memory(exec.getMatcher(), exec.getEvaluator());
        mem.init(q);
        
         dd1 = new Date();
        for (Entity e : exec.getProducer().getEdges(null, q.getFrom(), q.getBody().get(0).getEdge(), mem)) {
            
        }
         dd2 = new Date();
        System.out.println("Enum 1 Time: " + (dd2.getTime() - dd1.getTime()) / (1000.0));
        
         dd1 = new Date();
        for (Entity e : exec.getProducer().getEdges(null, new ArrayList<Node>(), q.getBody().get(0).getEdge(), mem)) {
            
        }
         dd2 = new Date();
        System.out.println("Enum 2 Time: " + (dd2.getTime() - dd1.getTime()) / (1000.0));
    }
 
    void test(GraphStore gs) throws EngineException, LoadException {
        
        Graph g = Graph.create();
//        Load ld = Load.create(g);
//        ld.load(RDF.RDF, Load.TURTLE_FORMAT);
//        
//        
//        gs.setNamedGraph(RDF.RDF, g);
        
        String q = "select  * (kg:timestamp(?e) as ?i) (kg:index(?x) as ?j) "
                + "from kg:rule\n"
                + "where {\n"
                + //" ?x rdfs:subClassOf ?y"
                "   ?x rdf:type :: ?e ?y  "
                + "  bind (kg:provenance(?e) as ?g) "
                + "graph ?g { ?q sp:where @([ sp:predicate owl:equivalentClass ])  }\n"
                + //"graph ?g { ?q sp:templates ([sp:predicate rdfs:subClassOf]) }\n" +
                "}\n"
                + "limit 10";
        
        q = "select  * "
                //+ "from kg:rule\n"
                + "where {\n"
             
                + "graph kg:d1 { [a kg:Graph ; ?p ?y ]}"
                + "graph kg:d2 { [a kg:Graph ; ?p ?z ]}"
                + "filter(?y != ?z)"
             
                + "}";
        
        
        
         q
                = "prefix f: <http://example.com/owl/families/>"
                + "select *  "
                //+ "from kg:rule "
                + "where {"
               // + "bind(kg:query() as ?q)"
                 + "graph kg:describe { ?x ?p ?y}"
               
                + "}";
        
        
       //System.out.println(((Query) gs.getQueryNode().getObject()).getAST());


        QueryProcess exec = QueryProcess.create(gs);

        Mappings map = exec.query(q);
        
        System.out.println(map);
        System.out.println(((Query) gs.getContext().getQueryNode().getObject()).getAST());
        System.out.println(map.getQuery());

    }
    
    void clean(Graph g) throws EngineException {
        QueryProcess exec = QueryProcess.create(g);
        QueryLoad ql = QueryLoad.create();
        String u2 = ql.read(data + "owlrule/query/ui2.rq");
        String u3 = ql.read(data + "owlrule/query/ui3.rq");
        String u4 = ql.read(data + "owlrule/query/ui4.rq");
        String av = ql.read(data + "owlrule/query/allsome.rq");
        //System.out.println(u2);
        exec.query(u2);
        //System.out.println(u3);
        exec.query(u3);
        //System.out.println(u4);
        exec.query(u4);
        //System.out.println(av);
        exec.query(av);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    //
    public void testQQ() throws EngineException, LoadException {
        String query = "select * where {?x ?p ?y}";
        String qvalidate = "template {st:apply-templates-with('/home/corby/AData/template/spintypecheck/template')} where {}";
        SPINProcess sp = SPINProcess.create();
        Graph qg = sp.toSpinGraph(query);
        qg.init();
        Graph gg = GraphStore.create();
        Load ld = Load.create(gg);
        ld.loadWE(data + "comma/model.rdf");
        gg.setNamedGraph(NSManager.STL + "query", qg);
        //System.out.println(qg.display());
        QueryProcess exec = QueryProcess.create(gg, true);
        //exec.add(qg);
        Mappings map = exec.query(qvalidate);
        System.out.println(map.getTemplateStringResult());
    }

    public void testSortFilter() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.loadWE(data + "comma/model.rdf");
        String q = "select * where {"
                + "?x ?p ?y ?z ?q ?t "
                // + "optional {?y ?q ?z} "
                + "filter(?x = ?z)"
                + "}";
        Mappings map = exec.query(q);
        System.out.println(map.getQuery());
    }

   

    public void testerhgodhf() throws EngineException, IOException {
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);
        Load ld = Load.create(gs);
        //ld.setLimit(10);
        try {
            ld.loadWE(data + "template/owl/data/tmp.ttl");
            //ld.loadWE(data + "template/owl/data/test.ttl");
        } catch (LoadException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

        Transformer t = Transformer.create(gs, Transformer.TURTLE);
        System.out.println("res: " + t.transform());
    }

    public void testSize() {
        boolean[][] test = null;
        try {
            int n = 100;
            test = new boolean[n][n];
            System.out.println(test.length);
        } catch (OutOfMemoryError E) {
            System.out.println("error");
        }
    }

    public void testUNION() throws EngineException, IOException, LoadException {
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);
        Load ld = Load.create(gs);
        ld.loadWE(data + "template/owl/data/primer.owl");

        String q = "select * where {"
                + "?s ?p ?o "
                + "{?s owl:sameAs ?s2} union {?p owl:sameAs ?p2}"
                + ""
                + "}"
                + "values ?s {<http://example.com/owl/families/John>}";

        Mappings map = exec.query(q);
        System.out.println(map);

    }

    public void testOWLRLxjkcbhfkjsqdf() throws EngineException, IOException, LoadException {
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);
        Load ld = Load.create(gs);
        try {
            ld.loadWE(data + "template/owl/data/primer.owl");
        } catch (LoadException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }
        ld.loadWE(data + "owlrule/owlrl.rul");
        RuleEngine re = ld.getRuleEngine();
        Date d1 = new Date();
        re.setProfile(re.OWL_RL_FULL);
        re.process();

        String q = "prefix f: <http://example.com/owl/families/>"
                + "select * "
                + "where {"
                + "graph kg:rule {"
                + "?x ?p ?y "
                + "filter (isURI(?x) && strstarts(?x, f:) "
                + "    && isURI(?y) && strstarts(?y, f:))"
                + "}"
                + "filter not exists {graph ?g {?x ?p ?y filter(?g != kg:rule)}}"
                + "}"
                + "order by ?x ?p ?y";
        Mappings map = exec.query(q);
        System.out.println(map);
        System.out.println(map.size());


    }

    /**
     * size: 123 max size: 1832 free size: 112
     */
    public void trace() {
        // Get current size of heap in bytes
        long heapSize = Runtime.getRuntime().totalMemory();
        System.out.println("size: " + heapSize / 1000000);

        // Get maximum size of heap in bytes. The heap cannot grow beyond this size.
        // Any attempt will result in an OutOfMemoryException.
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        System.out.println("max size: " + heapMaxSize / 1000000);

        // Get amount of free memory within the heap in bytes. This size will increase
        // after garbage collection and decrease as new objects are created.
        long heapFreeSize = Runtime.getRuntime().freeMemory();
        System.out.println("free size: " + heapFreeSize / 1000000);
    }

    public void testMove1() throws EngineException {
        Graph g = Graph.create();
       //g.setCompareIndex(true);
        QueryProcess exec = QueryProcess.create(g);
exec.setPlanProfile(Query.STD_PLAN);
        String init =
                "insert data {"
                + "graph <g1> {"
                + "<John> rdfs:label 'John' ."
                + "<James> rdfs:seeAlso <Jack> . "
                + "}"
                + "graph rdf: {"
                + "<John> rdfs:label 'John', 'Jim' ."
                + "<James> rdfs:seeAlso <Jack>, <Jim> . "
                + "}"
                + "graph <g3> {"
                + "<John> rdfs:label 'John' ."
                + "<James> rdfs:seeAlso <Jack> . "
                + "}"
                + "}";

        exec.query(init);

        String u1 = "move rdf: to default";

        exec.query(u1);
        
        System.out.println(g.display());
        
        String q1 = "select * from kg:default where  {?x ?p ?y}";
        String q2 = "select * from rdf: where  {?x ?p ?y}";

        Mappings m1 = exec.query(q1);
        Mappings m2 = exec.query(q2);
        assertEquals(4, m1.size());
        assertEquals(0, m2.size());

         String u2 = "move <g3> to <g0>";

         exec.query(u2);
        
         String q3 = "select * from <g0> where  {?x ?p ?y}";
         String q4 = "select * from <g3> where  {?x ?p ?y}";
         
        Mappings m3 = exec.query(q3);
        Mappings m4 = exec.query(q4);
        
        assertEquals(2, m3.size());
        assertEquals(0, m4.size());
    }
 
        
    
    
    public void testkey() throws EngineException {
        //System.out.println(EdgeIndex.byKey);
        GraphStore gs = GraphStore.create();
        //Graph.setValueTable(true);
        //Graph.setCompareKey(true);
        Graph.setCompareIndex(true);
        //Graph.setDistinctDatatype(true);
        //System.out.println(EdgeIndex.byKey);
        QueryProcess exec = QueryProcess.create(gs);

        String init = "insert data {"
                + "<John> rdf:value 1 , 1.0, '1.0'^^xsd:double "
                + "<Jim>  rdf:value  '1.0'^^xsd:float ,   'Jim', 'Jim'^^xsd:string "
                + "}";

        String q = "select debug * where {"
                + "?x ?p ?y "
                + "?z ?q ?y "
                + "filter(?x != ?z) "
               //+ "filter(datatype(?y) =  xsd:double)"
                + "}";

        exec.query(init);
        
        System.out.println(gs.display());
        Mappings map = exec.query(q);

        System.out.println(map);

        for (Entity e : gs.getAllNodes()) {
            System.out.println(e.getNode().getIndex() + " " + e);
        }

    }

    public void testgkgkgkj() {
        IDatatype u1 = DatatypeMap.newResource("http://www.inria.fr/schema/ontology/John");
        IDatatype u2 = DatatypeMap.newResource("http://www.inria.fr/schema/ontology/Jim");
        Date d1 = new Date();
        int i1 = 500000;
        int i2 = 100000;

        for (int i = 0; i < 200000000; i++) {
            //int res = u1.compareTo(u2);
            u1.getIndex();
            u2.getIndex();
            u1.getIndex();
            u2.getIndex();
            u1.getIndex();
            u2.getIndex();
            if (i1 < i2) {
            } else if (i1 == i1) {
            } else {
            }
        }
        Date d2 = new Date();
        System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / (1000.0));
    }

    
    
    public void testDN(){
        IDatatype dt1 = DatatypeMap.createResource(RDFS.SUBCLASSOF);
        IDatatype dt2 = DatatypeMap.createResource(RDFS.SUBCLASSOF);
        
        Node n1 = NodeImpl.create(dt1);
        Node n2 = NodeImpl.create(dt2);
        
        Date d1, d2;
        d1 = new Date();
        for (int i = 0; i < 100000000; i++){
            dt1.compare(dt2);
        }
        d2 = new Date();
        System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / (1000.0));
        d1 = new Date();
        for (int i = 0; i < 100000000; i++){
            n1.compare(n2);
        }
        d2 = new Date();
        System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / (1000.0));
    }
    
        
      
    
        


    public void testtypepath() throws EngineException, LoadException{
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);
        
        Load ld = Load.create(gs);
        System.out.println("Load: ");
        //ld.loadWE(data + "template/owl/data/fma3.2.owl");    //   21.713
        ld.loadWE(data + "template/owl/data/primer.owl");    //   21.713
        
        
        String init = FOAF_PREF +
                "insert data {"
                + "foaf:John a foaf:Person "
                + "foaf:James a foaf:Human "
                + "foaf:Person rdfs:subClassOf foaf:Human "
                + "}";
        
        System.out.println("Init: ");
        gs.init();
        
        String q = FOAF_PREF +
                "select (count(*) as ?c) "
                +" where {"                
                + "?x owl:allValuesFrom ?y . \n" +
"?x owl:onProperty ?p . \n" +
"?u rdf:type/rdfs:subClassOf* ?x . \n" +
"?u ?p ?v  "
                + "}";
        
        String qq = FOAF_PREF +
                "select * "
                +" where {"                
                + " {select distinct ?x ?y where {"
                + "?x owl:allValuesFrom ?y }}. \n" +
"?x owl:onProperty+ ?p . \n" 
//"?u a ?x . \n" +
//"?u ?p ?v  "
                + "}"
                + "pragma {"
                + "kg:path kg:list true ; "
                + "kg:store false ;"
                + "kg:type true"
                + ""
                + "}";
        
        qq = "prefix f: <http://example.com/owl/families/>"
                + "select * where {"
                + "?x rdfs:subClassOf ?t "
                + "filter(?t = f:Person)"
                + "?t1 rdfs:subClassOf+ ?t"
                + ""
                + "}";
        
        Date d1, d2;
//         exec.query(q);
//        
//        System.out.println("Q1: ");
//        Date d1 = new Date();
//        Mappings map = exec.query(q);
//        Date d2 = new Date();
//        System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / (1000.0));
//
//        System.out.println(map.size());
//       
//       
//       
//        System.out.println("Q2: ");
//         d1 = new Date();
//         exec.setListPath(true);
//         map = exec.query(q);
//         d2 = new Date();
//        System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / (1000.0));
//
//        System.out.println(map);
        
        
        
         System.out.println("Q3: ");
         d1 = new Date();
         exec.setListPath(true);
//         exec.setPathType(true);
         exec.setStorePath(false);
         Mappings map = exec.query(qq);
         d2 = new Date();
        System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / (1000.0));

        System.out.println(map);
        
        
    }
    


//    void test(Graph g){
//        Closure clos = new Closure(g, new Distinct());
//        clos.init(g.getPropertyNode(RDFS.SUBCLASSOF));
//        clos.closure(0, 0);
//    }
    public void testOWLRL1() throws EngineException, IOException, LoadException {
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);
        Load ld = Load.create(gs);
        //ld.setLimit(10);
        try {
            ld.loadWE(data + "template/owl/data/primer.owl");
            //ld.loadWE(data + "template/owl/data/hao.owl");
        } catch (LoadException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

//        QueryLoad ql = QueryLoad.create();
//        String unify = ql.getResource("/query/unify.rq");
//        String clean = ql.getResource("/query/clean.rq");
//        System.out.println(unify);
//        System.out.println(clean);
//
//        // tell Transformer to cache st:hash transformation result
//        exec.getEvaluator().setMode(Evaluator.CACHE_MODE);
//        System.out.println("Unify");
//        Date d1 = new Date();
//        // replace duplicate OWL expressions by one of them
//        Mappings m1 = exec.query(unify);
////        // remove duplicate expressions
////        Mappings m2 = exec.query(clean);
//        Date d2 = new Date();
//////        System.out.println(m1);
//////        System.out.println(m2);
//        System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / (1000.0));
////
////
//        String q3 = " select (count(*) as ?c) "
//                + " where {"
//                + " ?x sp:isReplacedBy ?r  "
//                // + "filter(?x != ?y)"
//                + "}";
//        Mappings mm = exec.query(q3);
//        System.out.println("Replaced: " + mm);
//
//        System.out.println("Clean");
//        d1 = new Date();
//        Mappings m2 = exec.query(clean);
//        d2 = new Date();
//        System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / (1000.0));
////
////        
        System.out.println("OWL RL");
        ld.loadWE(data + "owlrule/owlrllite.rul");
        RuleEngine re = ld.getRuleEngine();
        Date d1 = new Date();
        re.setProfile(re.OWL_RL);
        re.process();
        Date d2 = new Date();
        System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / (1000.0));

        String q2 = "prefix f: <http://example.com/owl/families/>"
                + "select (count(*) as ?c) "
                + "from kg:rule "
                + "where {"
                + "?x ?p ?y "
                + "filter (isURI(?x) && strstarts(?x, f:) && isURI(?y) && strstarts(?y, f:))"
                + "}" // + "order by ?x ?p ?y"
                ;



        String q4 = " template {"
                + "st:apply-templates-with(st:turtle,  ?x) "
                + "} "
                + " where {"
                + " graph kg:rule { ?x a ?t filter isBlank(?t) }"
                + "}";


        Mappings map = exec.query(q2);
//        System.out.println(map);
        System.out.println("Entailment: " + map);

    }

    public void testOWLRL2() throws EngineException, LoadException {
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);
        Load ld = Load.create(gs);
        //ld.setLimit(10);
        try {
            ld.loadWE(data + "template/owl/data/primer.owl");
            ld.loadWE(data + "template/owl/data/primer-data.ttl");
        } catch (LoadException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

        //462 bnode match: 19585

        ld.loadWE(data + "owlrule/owlrllite.rul");
        //ld.load(data + "owlrule/test.rul");
        RuleEngine re = ld.getRuleEngine();
        re.setProfile(re.OWL_RL);
        //re.setDebug(true);
        Date d1 = new Date();
        re.process();
        Date d2 = new Date();
        System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / (1000.0));

        String q = "prefix f: <http://example.com/owl/families/>"
                + "select * "
                + "from kg:rule "
                + "where {"
                + "?x a ?t "
                + "filter (isURI(?t) && strstarts(?t, f:))"
                + "}";

        String q2 = "prefix f: <http://example.com/owl/families/>"
                + "select * "
                + "from kg:rule "
                + "where {"
                + "?x ?p ?y "
                + "filter (isURI(?x) && strstarts(?x, f:) && isURI(?y) && strstarts(?y, f:))"
                + "}"
                + "order by ?x ?p ?y";

        String q3 = "prefix f: <http://example.com/owl/families/>"
                + "template {"
                + " st:apply-templates-with(st:turtle, ?t)  "
                + "} "
                //+ "from kg:rule "
                + "where {"
                + "f:James a ?t, f:Parent"
                + "}";

        String q4 = "prefix f: <http://example.com/owl/families/>"
                + "prefix sp: <http://spinrdf.org/spin#>"
                + "template {"
                + " st:apply-templates-with(st:turtle, ?x)"
                + "} "
                + "where {"
                + "?x a sp:ConstraintViolation"
                + "}";

        String q5 = "select (count(*) as ?res) where {"
                + "?x a ?t filter isBlank(?t)"
                + "}";

        // 462

        Mappings map = exec.query(q2);
        if (map.getTemplateResult() != null) {
            System.out.println(map.getTemplateStringResult());
        } else {
            System.out.println("result: " + map);
        }
//        
        String cc = re.getConstraintViolation();
        if (cc != null) {
            System.out.println("Constraint: \n" + cc);
        }


//        try {
//            String qq = QueryLoad.create().getResource("/query/constraint.rq");
//            Mappings m = exec.query(qq);
//            System.out.println(m.getTemplateStringResult());
//        } catch (IOException ex) {
//            java.util.logging.Logger.getLogger(RuleEngine.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (EngineException ex) {
//            java.util.logging.Logger.getLogger(RuleEngine.class.getName()).log(Level.SEVERE, null, ex);
//        }    

        //System.out.println(map.size());
        QueryLoad ql = QueryLoad.create();
        //ql.write("/home/corby/AData/work/f1.txt", map.toString());
        MatcherImpl m = (MatcherImpl) re.getQueryProcess().getMatcher();
        System.out.println("bnode match: " + m.getMatchBNode().getCount());

        //       Transformer t = Transformer.create(gs, Transformer.TURTLE);

//        MatchBNode.TreeNode tree = m.getMatchBNode().getTree(false);
//
//        for (IDatatype dt : tree.keySet()) {
//            IDatatype val = tree.get(dt);
//            if (!tree.containsKey(val)) {
//                System.out.println(dt + " " + val);
//            }
//            if (dt.same(val)) {
//                System.out.println(dt + " = " + val);
//            }
//        }
//
//
//        System.out.println(m.getMatchBNode().getTree(true).size());
//        System.out.println(m.getMatchBNode().getTree(false).size());

    }

    public void testmatch() throws EngineException , LoadException{
        GraphStore gs = GraphStore.create();
        GraphStoreInit.create(gs).init();
        QueryProcess exec = QueryProcess.create(gs);
        Load ld = Load.create(gs);
        //ld.setLimit(10);
        try {
            ld.loadWE(data + "work/testowl.ttl");
        } catch (LoadException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

        String q = "prefix ex: <http://example.org/>"
                + "select (if (isBlank(?t), kg:pprint(?t), ?t) as ?tt) where {"
                + "ex:John a ?t "
                //+ "?x rdf:first ?t "
                + "}"
                //+ "pragma {kg:query kg:match true}"
                + "";


        String q2 = "prefix ex: <http://example.org/>"
                + "select ?x ?t where {"
                + "?x a [ "
                + "a owl:Restriction ; "
                + "owl:onProperty ex:hasParent ; "
                + "owl:someValuesFrom ?t] "
                + "}"
                //+ "pragma {kg:query kg:match true}"
                + "";

        //34721 34572

        ld.loadWE(data + "owlrule/owlrllite.rul");
        RuleEngine re = ld.getRuleEngine();

        re.setSpeedUp(true);
        //re.getQueryProcess().setMatchBlank(true);

        re.process();

        Mappings map = exec.query(q);
        System.out.println(map);
        System.out.println(map.size());

    }

    public void testSubsume() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init = FOAF_PREF
                + "insert data {"
                + "graph <g1> { foaf:John foaf:name 'John' ; foaf:knows foaf:Jack } "
                + "graph <g2> { foaf:Jack foaf:name 'Jack' ; foaf:knows foaf:Jim } "
                + "}";

        String q = FOAF_PREF
                + "select * where {"
                + "?x foaf:name ?n ."
                + "{?x foaf:knows ?y} "
                + "union { ?z foaf:name ?n } "
                + "}";

        exec.query(init);
        Mappings map = exec.query(q);

        System.out.println(map);
        System.out.println(map.getQuery());
    }

    public void testRuleOpt() throws LoadException, EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.loadWE(data + "comma/comma.rdfs");
        ld.loadWE(data + "comma/model.rdf");
//        ld.loadWE(data + "comma/data");
//        ld.loadWE(data + "comma/data2");

        try {
            ld.loadWE(data + "rule/rdfs.rul");

        } catch (LoadException e) {
            e.printStackTrace();
        }
        System.out.println("Start Rules");
        RuleEngine re = ld.getRuleEngine();
        re.setOptimize(true);// 86690 ** Time : 11.477
        re.setConstructResult(true);
        re.setTrace(true);
        System.out.println("Graph: " + g.size());
        Date d1 = new Date();
        re.process();
        Date d2 = new Date();
        System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / (1000.0));


        String q = "select * where {"
                + "?x a ?t "
                + "?t rdfs:subClassOf ?c"
                + "}";

        Mappings map = exec.query(q);

        System.out.println(map);

        //assertEquals(44630, g.size());

    }

    public void testExists() throws EngineException {

        Graph g1 = Graph.create();
        QueryProcess exec = QueryProcess.create(g1);
        Graph g2 = Graph.create();
        QueryProcess exec2 = QueryProcess.create(g2);
        String init1 = "insert data { "
                + "<John> rdfs:label 'John' "
                + "<James> rdfs:label 'James'"
                + "}";

        String init2 = "insert data { "
                + "<Jim> rdfs:label 'Jim' "
                + "}";


        String q = "select * "
                + "(exists {"
                + "{select (exists {?p ?p ?y} as ?rr) where {}} "
                + "} as ?res) "
                + "where {"
                + "?x ?q ?y "
                + ""
                + "}";

        exec.query(init1);
        exec2.query(init2);

        exec.add(g2);

        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?res");
        System.out.println(map);
        System.out.println(map.size());

        SPINProcess sp = SPINProcess.create();
        System.out.println(sp.toSpin(q));

    }

    public void testQS() {

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init = "insert data { "
                + "<John> rdfs:label 'John' "
                + "<James> rdfs:label 'James'"
                + "}";

        String query =
                "select  (exists {?x rdfs:label ?l} as ?res)  where {"
                + "select (exists {?x rdfs:label ?l} as ?res) "
                + "where {"
                + "?x rdfs:label ?l"
                + "}"
                + "order by (exists {?x rdfs:label ?l})"
                + "group by (exists {?x rdfs:label ?l})"
                + "having (exists {?x rdfs:label ?l})"
                + "} "
                + "order by (exists {?x rdfs:label ?l})"
                + "group by (exists {?x rdfs:label ?l})"
                + "having (exists {?x rdfs:label ?l})";

        try {
            exec.query(init);
            Mappings map = exec.query(query);

            System.out.println(map);
            System.out.println(map.size());
            //System.out.println(map.getQuery());

        } catch (EngineException e) {
        }
    }

    public void testGT() throws LoadException, EngineException {
        Graph g = Graph.create(true);
        Load ld = Load.create(g);
        ld.loadWE(RDF.RDF, Load.TURTLE_FORMAT);
        ld.loadWE(RDFS.RDFS, Load.TURTLE_FORMAT);

        //ld.load(data + "template/spin/data");

        String t1 = "template { "
                + " st:apply-templates-with-nograph(st:turtle, kg:entailment) "
                + "} where {"
                //+ "graph ?g {} "
                + "}";


        String q = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "construct {?x ?p ?photo}"
                + "where {\n"
                + "service <http://dbpedia.org/sparql> {\n"
                + "select *\n"
                + "where {\n"
                + "?x ?p ?photo\n"
                + "} limit 10\n"
                + "}}";

        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
//            String str = map.getTemplateStringResult(); 
//            System.out.println(str);
//            assertEquals(6892, str.length());
        System.out.println(map);

    }

    public void testSTLOWL() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.loadWE(data + "template/owl/data/primer.owl");

        Transformer t = Transformer.create(g, Transformer.TURTLE);
        t.definePrefix("f", "http://example.com/owl/families/");
        //t.setTrace(true);
        System.out.println(t);

    }

    public void testChatbot() throws EngineException, LoadException {

//        ASTQuery.setTemplateAggregate(Processor.AGGAND);
//        ASTQuery.setTemplateConcat(Processor.STL_AND);

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.loadWE(data + "work/chatbot/data");

        Transformer t = Transformer.create(g, "/home/corby/AData/work/chatbot/template");

        //Transformer t = Transformer.create(g, Transformer.TURTLE);

        System.out.println(t);

        String q = "select * where {\n"
                + "?in rdfs:label ?ll "
                + "   optional { ?in rdfs:label ?l }\n"
                + "}";

        // Mappings map = exec.query(q);
        //System.out.println(map);

//        for (Query q : t.getQueryEngine().getQueries()){
//            System.out.println(q.getTemplateGroup());
//        }

    }

    public void testAND() throws EngineException, LoadException {

//        ASTQuery.setTemplateAggregate(Processor.AGGAND);
//        ASTQuery.setTemplateConcat(Processor.STL_AND);

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.loadWE(data + "comma/comma.rdfs");
        ld.loadWE(data + "comma/model.rdf");

        Transformer t = Transformer.create(g, "/home/corby/AData/work/typecheck/template");


        System.out.println(t);

//        for (Query q : t.getQueryEngine().getQueries()){
//            System.out.println(q.getTemplateGroup());
//        }

    }

    public void testSTL() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.loadWE(data + "template/spin/data");

        Transformer t = Transformer.create(g, Transformer.SPIN);
        //t.setTrace(true);
        t.definePrefix("foaf", "http://xmlns.com/foaf/0.1/");
        System.out.println(t);




        String temp =
                "prefix ex: <http://example.org/>"
                + "prefix tt: <http://inria.org/test/>"
                + "prefix ns: <http://inria.org/schema/>"
                + "template {"
                + "'prefix ' ?p ': <' ?n '>' "
                + "}"
                + "where {"
                + "bind(unnest(st:prefix()) as (?p, ?n))"
                + "}";
        Mappings map = exec.query(temp);
        //System.out.println(map.getTemplateStringResult());
        //System.out.println(map.getQuery().getAST());
    }

    void fff() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        Mappings map = exec.query("select ... where ");
        for (Mapping m : map) {
            m.getEdges();
            m.getQueryEdges();

        }
    }

    public void testLoad22() throws EngineException, LoadException {
        Graph g = Graph.create();
        Load load = Load.create(g);
        // load.load(NSManager.RDF, Load.TURTLE_FORMAT);
        QueryProcess exec = QueryProcess.create(g);

        String init = "insert data {"
                + "<A> rdfs:subClassOf <B> "
                + "<B> rdfs:subClassOf <C> "
                + "<C> rdfs:subClassOf <D> "
                + ""
                + "}";

        String temp = "template {"
                + "st:apply-templates-with('/home/corby/AData/work/template/depth')"
                + "}"
                + "where {}";

        exec.query(init);

        Mappings map = exec.query(temp);
        System.out.println("res: ");
        System.out.println(map.getTemplateStringResult());

        Transformer t = Transformer.create(g, "/home/corby/AData/work/template/depth");
        t.setTrace(true);
        System.out.println(t.transform());

    }

    public void testReplace() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select "
                + "(replace('abcd', 'b', 'Z') as ?r1)"
                + "(replace('abab', 'B', 'Z','i') as ?r2)"
                + "(replace('abab', 'B.', 'Z','i') as ?r3)"
                + "(replace ('banana', 'ANA', '*', 'i') as ?r4)"
                + "where {}";


        Mappings map = exec.query(q);

        System.out.println(map);
    }

    public void testGCC() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init = "insert data {"
                + "<John> rdf:value 'test'@fr, 'titi'@fr . "
                + "<Jack> rdf:value 'test'@fr,'titi'@en . "
                + "<Jim>  rdf:value2 'test'@fr, 'titi' . "
                + "}";

        String q = "select * "
                + " "
                + "  where {"
                + "?x ?p ?v "
                + "bind(exists {?x rdf:value ?v filter(?x != ?y) ?y rdf:value ?v} as ?b)"
                + "}"
                + "group by ?x "
                + "having (exists {?x rdf:value ?v filter(?x != ?y) ?y rdf:value ?v} ) ";
        // + "having (?b )";

        exec.query(init);


        Mappings map = exec.query(q, Dataset.create());
        System.out.println(map.size());
        System.out.println(map);
        System.out.println(map.getQuery().isCorrect());

//      IDatatype dt0 = (IDatatype) map.get(0).getValue("?g");
//      assertEquals(true, dt0.getDatatypeURI().equals(NSManager.XSD+"string"));
//      
//      IDatatype dt1 = (IDatatype) map.get(1).getValue("?g");
//      assertEquals(true, dt1.getDatatypeURI().equals(NSManager.XSD+"string"));
//      
//      IDatatype dt2 = (IDatatype) map.get(2).getValue("?g");
//      assertEquals(true, dt2.getLang().equals("fr"));

    }

    public void testGB() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init = "insert data {"
                + "<John> rdf:value 1, 2, 3 . "
                + "<Jack> rdf:value 6, 3, 4 . "
                + "<Jim> rdf:value 3, 5, 7 . "
                + "}";

        String q = "template { "
                + "st:number() ': ' "
                + " ?x ' ' "
                + "group  { st:number() ':' ?v }"
                + "}"
                + "where {"
                + "?x rdf:value ?v"
                + "} group by ?x "
                + "order by desc(?x) "
                + "limit 2 "
                + "offset 1";

        exec.query(init);


        Mappings map = exec.query(q);
        System.out.println(map.getTemplateStringResult());
        System.out.println(map.getQuery().getAST());


    }

    //@Test
    public void myTestSDK() throws EngineException , LoadException{
        QueryLoad ql = QueryLoad.create();
        String q = ql.read(data + "sdk/sdkst1.rq");
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.loadWE(data + "sdk/sdk.rdf");
        QueryProcess exec = QueryProcess.create(g);

        Date d1 = new Date();
        Mappings map = exec.query(q);
        Date d2 = new Date();
        System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / (1000.0));

        int max = 10;
        d1 = new Date();
        for (int i = 0; i < max; i++) {
            map = exec.query(q);
        }
        d2 = new Date();
        System.out.println(map.getTemplateResult().getLabel());
        System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / (max * 1000.0));
        assertEquals(1, map.size());
    }

    //@Test
    public void testRule2() throws LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        //init(g);
        // ** Time : 2.01 Rule: 71139 vs 5.5

//        init2(g);
        ld.loadWE(data + "template/owl/data/hao.owl");//** Time : 28.295 g: 179935
        //179935 ** Time : 27.824  VS Graph: 179935 ** Time : 61.946
        // distinct: 25.411
        // transitive : 16.7 !!!
        // reordered : 8.866
        // java code : 7.942
        //ld.loadWE(data + "comma/comma.rdfs");

        try {
//          load.loadWE(data + "engine/ontology/onto.rdfs");
//          load.loadWE(data + "engine/data/test.rdf");
            //ld.loadWE(data + "owlrule/tmp.rul");
            ld.loadWE(data + "owlrule/owlrllite.rul");
            // ld.loadWE(data + "work/maxime/ug-rules.rul");

            //ld.loadWE(data + "owlrule/owlrl.rul");
            // 222238 23.657 vs 36
            //ld.loadWE(data + "owlrule/rdfs.rul");

        } catch (LoadException e) {
            e.printStackTrace();
        }
        System.out.println("Start Rules");
        RuleEngine re = ld.getRuleEngine();
        re.setOptimize(true);// 86690 ** Time : 11.477
        re.setConstructResult(true);
        re.setTrace(true);
        System.out.println("Graph: " + g.size());
        //System.out.println(g.getIndex());
        Date d1 = new Date();
        re.process();
        Date d2 = new Date();
        //System.out.println(g.getIndex());
        double tt = (d2.getTime() - d1.getTime()) / (1000.0);
        System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / (1000.0));


        System.out.println("Rule: " + g.size());
        System.out.println(re.getResultListener());


        d1 = new Date();
        re.clean();
        d2 = new Date();
        System.out.println("** Clean : " + (d2.getTime() - d1.getTime()) / (1000.0));
// 86690
//18.502
//11.672     
        //System.out.println(g.display());


    }

    public void testBindd() throws EngineException {
        String init = "insert data {"
                + "<John> rdfs:label 10, 20"
                + "}";

        String q = "select debug ?c where{"
                + "?x rdfs:label ?l "
                + "bind(?l + 1 as ?c)"
                + "}";

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        System.out.println(map);
    }

    public void testTT() throws EngineException , LoadException{
        String t = "template {"
                + "st:call-template-with('/home/corby/AData/testst', st:depth )"
                + "}"
                + "where {}";
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.loadWE(data + "comma/comma.rdfs");
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(t);
        System.out.println("res:" + map.getTemplateResult());

    }

    public void testTN() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init =
                "insert data {"
                + "<John> rdfs:label 'John' ."
                + "<James> rdfs:seeAlso <Jack> . "
                + "}";
        exec.query(init);

        String query =
                "template {"
                + "kg:write('/home/corby/AData/work/tmp.html', "
                + "st:apply-templates-with('/home/corby/AData/template/table/template'))"
                + "}"
                + "where {}";



        Mappings map = exec.query(query);

        System.out.println(map.getTemplateResult().getLabel());
        System.out.println(map);
    }

    public void testJoinDistinct() {
        String init =
                "insert data {"
                + "<John> rdfs:label 'John', 'Jack' "
                + "}";

        String query =
                "select debug distinct ?x where {"
                + "{?x rdfs:label ?a} "
                + "{ ?y rdfs:label ?b } "
                + "filter(?x = ?y)"
                + "}";



        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        // exec.setJoin(true);

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            assertEquals("Result", 1, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    public void testGE() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init = "insert data {<John> rdfs:label 'John'}";

        String q = "select * where {"
                + "?x ?p ?y "
                + "filter(?z < 12) "
                + "?y ?q ?z "
                + "filter(?x != ?z)"
                + ""
                + ""
                + "}";

        exec.query(init);
        Mappings map = exec.query(q);
        System.out.println(map);
    }

    public void testLoaddf() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init = "load rdf:";
        try {
            exec.query(init);
        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(g);
    }

    public void myTestJoin() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init = "insert data {"
                + "<John> rdfs:label 'John' "
                + "<Jack> rdfs:label 'Jack' "
                + "}";

        String q = "select * where {"
                + "?x ?p ?y "
                + "?z ?q ?t"
                + "}"
                + "values (?y ?t) { "
                + "( 'John' 'Jack' ) "
                + "( 'Jack' 'John' ) "
                + "}";

        exec.query(init);
        Mappings map = exec.query(q);
        System.out.println(map);

    }

    public void testBase() {
        NSManager nsm = NSManager.create();

        nsm.setBase("http://example.org/test.html");
        nsm.setBase("foo/");
        nsm.definePrefix(":", "bar#");

        String str = nsm.toNamespaceB(":Joe");

        assertEquals("http://example.org/foo/bar#Joe", str);

        IDatatype dt = DatatypeMap.newInstance(true);
        Transformer pp = Transformer.create(Graph.create());
        System.out.println(pp.turtle(dt));

    }

    

    // @Test
    public void testSystem2() throws EngineException {
        GraphStore gs = GraphStore.create();
        GraphStoreInit.create(gs).init();
        QueryProcess exec = QueryProcess.create(gs);

        String init = "insert data { graph kg:system { "
                + "kg:kgram kg:listen true "
                + "kg:store sp:query true "
                + "}}";

        String q = "select * where  {?x  st:name 'toto' }";

        String query = "select ?res where {"
                + "graph kg:query {"
                + "select (st:apply-templates-with(st:spin, ?q) as ?res) where {"
                + "?q a sp:Select"
                + "}"
                + "}"
                + "}";

        exec.query(init);
        exec.query(q);

        Mappings map = exec.query(query);

        IDatatype dt = (IDatatype) map.getValue("?res");
        System.out.println(dt);




    }

    public void testtemp() {

        String init = FOAF_PREF
                + "insert data {"
                + "foaf:John foaf:knows foaf:James "
                + "foaf:James foaf:knows foaf:John "
                + "}";

        String q = "select (kg:pprint-with() as ?r) where {}";

        try {
            Graph g = Graph.create();

            QueryProcess exec = QueryProcess.create(g);

            Mappings map = exec.query(q);
            //PPrinter.setExplainDefault(true);

            Transformer pp = Transformer.create(g, data + "work/test/");
            pp.setDebug(true);
            System.out.println(pp);


        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    public void test10() throws LoadException {

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select  *  where {"
                + "bind (unnest(kg:sparql("
                + "'prefix c: <http://www.inria.fr/acacia/comma#> "
                + "select ?x ?doc where {"
                + "?x rdf:type c:Person; c:hasCreated ?doc}')) "
                + "as (?a, ?b, ?c))"
                + "} ";

        String q = "select debug * where {"
                + "{ select distinct ?p where {"
                + "   ?s ?p ?v"
                + " } order by ?p }"
                + " { ?in ?p ?o } union { bind(kg:null as ?o) }"
                + "}"
                + "values ?in { <http://www.inria.fr/hery.rakotoarisoa> }";


        try {
            Graph g = Graph.create(true);
            Load ld = Load.create(g);
            ld.loadWE(data + "comma/data2/f12.rdf");
            QueryProcess exec = QueryProcess.create(g);

            Mappings map = exec.query(q);
            System.out.println(map);

            System.out.println(map.size());
        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    //@Test
    public void testDescr() {

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        //ld.load(data + "rdfa");



        String init =
                "prefix ex: <http://test/> "
                + "prefix foaf: <http://foaf/> "
                + "describe ?z  where {"
                + "?x ?p ?y filter exists { ?x ?p ?z}"
                + "}";


        try {
            Mappings map = exec.query(init);
            ASTQuery ast = exec.getAST(map);
            assertEquals(0, ast.getConstruct().size());
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void testsuhydfgiuashfia() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init = "@base <http://sdhfkjs.fr/test.html> "
                + "insert data {"
                + "<> rdf:value (<foo>)"
                + "}"
                + "";

        String q = "@base <http://sdhfkjs.fr/test.html> "
                + "select debug * where {?y rdf:value (?x)}";
        exec.query(init);
        Mappings map = exec.query(q);
        System.out.println(map);
    }

    public void test37() {
        Graph g = Graph.create(true);
        QueryProcess exec = QueryProcess.create(g);

        String init = "insert data {<John> <name> 'John'}";
        try {
            exec.query(init);

            g.init();

//			RDFFormat f = RDFFormat.create(g);
//			System.out.println(f);

            System.out.println(g.display());
            assertEquals("Result", 3, g.size());

//            String query = "select * where {?p rdf:type rdf:Property}";
//
//            Mappings res = exec.query(query);
////			System.out.println("** Res: " );
////			System.out.println(res);
//            assertEquals("Result", 2, res.size());
//
//
//            String update = "delete {?x ?p ?y} where {?x ?p ?y}";
//            exec.query(update);
//
//
//            String qq = "select * where {?x ?p ?y}";
//            res = exec.query(qq);
//            assertEquals("Result", 0, res.size());

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void testEE() throws LoadException, EngineException {
        Graph g = Graph.create(true);

        QueryProcess exec = QueryProcess.create(g);

        String init = FOAF_PREF
                + "insert data {"
                + "foaf:knows rdfs:domain foaf:Person ; rdfs:range foaf:Person ."
                + "<John> foaf:knows <Jim> "
                + "<Jim> foaf:knows <James> "
                + "<Jack> foaf:knows <Jim> "
                + "<James> a foaf:Person"
                + "}";

        exec.query(init);
        g.setDebug(true);
        g.init();

        System.out.println(g.display());

    }

    public void testLoad() throws LoadException, EngineException {
        Graph g = Graph.create(true);
        Load ld = Load.create(g);

        QueryProcess exec = QueryProcess.create(g);

        String q1 = "select * where {graph ?g {?x a ?t}}";
        String qrem = "clear all";
        String q3 = "select * where {graph ?g {?x ?p ?t filter(?p = rdf:type)}}";

        ld.loadWE(data + "math/data/fun.ttl");

        Mappings map = exec.query(q1);

        //System.out.println(map);
        System.out.println(map.size());

        int res = map.size();

        exec.query(qrem);

        ld.loadWE(data + "math/data/fun.ttl");

        map = exec.query(q1);

        //System.out.println(map);
        assertEquals(res, map.size());

//        map = exec.query(q3);
//        
//        System.out.println(map);
//        System.out.println(map.size());

//        Iterable<Entity> it = g.getEdges(RDF.TYPE);
//        int i = 0;
//        for (Entity e : it){
//            System.out.println(i++   + " " + e);
//        }
//        
    }

    public void testOptional() throws EngineException, LoadException {

        String init = FOAF_PREF
                + "insert data {"
                + "<James> rdfs:label 'James' "
                + "<Jim> rdfs:label 'Jim' ; foaf:knows <John> "
                + "<John> foaf:knows <James>, <Jim> "
                //+ "; rdfs:label 'John' "
                + ""
                + "}";

        String q = FOAF_PREF
                + " select distinct  ?x where {"
                //+ "graph ?g {"
                + "?x foaf:knows ?y, ?z   optional "
                + " {?y rdfs:label ?l} filter(! bound(?l))"
                ///  + "} "
                //+ "filter(?g = <g1>)"
                + "} "
                // + "pragma {kg:kgram kg:test true}"
                + "";

        q = "prefix c: <http://www.inria.fr/acacia/comma#> "
                + "select debug ?x ?z where {"
                + "?x ?p ?y "
                + ". ?z ?q ?t ."
                + "filter(?x > ?z) "
                + "filter(?x = ?z) "
                + ""
                + "}";


        //QueryProcess.setJoin(false);

        Graph g = Graph.create(true);
        Load ld = Load.create(g);


        init(g);

        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Node node = g.getResource("John");
        System.out.println(node);
//        Node var = NodeImpl.createVariable("?x");
//        Mapping m = Mapping.create(var, node);
//        Mappings map = null;
//        Date d1 = new Date();
//        int max = 1;
//        System.out.println("start");
//        for (int i = 0; i < max; i++) {
//            System.out.println(i);
//            map = exec.query(q);
//        }
//        Date d2 = new Date();
//        System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / (max * 1000.0));
//
//        System.out.println(map.getQuery());
//        System.out.println(map);
//        System.out.println(map.size());

    }

    public void test45sqdhfsq() throws EngineException {

        String init = "insert data {"
                + "<John> rdfs:label 'John' "
                + "<Jim> rdfs:label 'Jim' "
                + ""
                + "}";



        String q = " select  distinct ?x   where {"
                + "?x ?p ?y ?t ?q ?z"
                + ""
                + "} limit 1 "
                // + "pragma {kg:kgram kg:test true}"
                + "";
        //Query.testJoin = true;
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = null;
        Date d1 = new Date();
        int max = 1;
        System.out.println("start");
        for (int i = 0; i < max; i++) {
            System.out.println(i);
            map = exec.query(q);
        }
        Date d2 = new Date();

        System.out.println(map.getQuery());
        //System.out.println(map.getQuery().getNodes());

        System.out.println(map);
        System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / (max * 1000.0));

    }

    //@Test
    public void testSQL() throws EngineException {
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);

        String q = "select  (sql('db', 'login', 'passwd', 'select from where') as (?a, ?b)) where {"
                //+ "bind(1 as ?x)"
                + "}";

        Mappings map = exec.query(q);

        System.out.println(map.getQuery().getSelectFun());
        System.out.println(map.getSelect());
        System.out.println(map.size());
    }

    //@Test
    public void testPPPPP() throws EngineException {
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);

        String init = FOAF_PREF
                + "insert data {"
                + "<John> foaf:knows <Jim> "
                + "<Jim> foaf:knows <James>"
                + "<John> foaf:knows <Jack>  "
                + "<Jack> foaf:knows <James>  "
                + "<Jim> foaf:name 'Jim' "
                + "}";

        String q = FOAF_PREF
                + "select debug ?z where {"
                + "{?x foaf:knows ?y   . bind(?y as ?z) }  union "
                + "{?x foaf:name ?name . bind(?name as ?z)}"
                + "}";

        exec.query(init);

        Mappings map = exec.query(q);

        System.out.println(map);


    }

    //@Test
    public void testrec() throws EngineException {
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);

        String init = FOAF_PREF
                + "insert data {"
                + "<John> foaf:knows (1 2 3) "
                + "}";

        String q =
                "template {kg:templateWith('/home/corby/AData/work/fact.rq', kg:rec, 4)}"
                + "where {}";

        String qq = FOAF_PREF
                + "template {kg:templateWith('/home/corby/AData/work/rec.rq', kg:rec, ?y)}"
                + "where {?x foaf:knows ?y}";

        exec.query(init);

        Mappings map = exec.query(q);

        System.out.println(map.getTemplateResult());


    }

    public void testjsdhshqds() throws EngineException {
        GraphStore gs = GraphStore.create();
        GraphStoreInit.create(gs).init();
        QueryProcess exec = QueryProcess.create(gs);

        String init = "load rdf: ;  load rdfs:";

        String q = "select * where {"
                + "{?x ?p ?y}"
                + "{?z ?q ?t ?t ?r ?w ?w ?rr ?z}"
                + "}";

        exec.query(init);

        Mappings map = map = exec.query(q);;
        int max = 10;
        Date d1 = new Date();
        for (int i = 0; i < max; i++) {
            System.out.println(i);
            map = exec.query(q);
        }
        Date d2 = new Date();

        System.out.println(map.getQuery());
        System.out.println(map.size());
        System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / (max * 1000.0));

    }

    public void testJJ() throws EngineException {
        GraphStore gs = GraphStore.create();
        GraphStoreInit.create(gs).init();
        QueryProcess exec = QueryProcess.create(gs);

        String q = "select * where {"
                + "{?x ?p ?y graph ?g { ?x ?q ?z }"
                + "{?a ?q ?z filter(?x != ?z) } minus {?t ?q ?z} optional {?t ?r ?v}}"
                + "}";

        Mappings map = exec.query(q);
        System.out.println(map.getQuery());

    }

    public void testSort() throws EngineException {
        GraphStore gs = GraphStore.create();
        GraphStoreInit.create(gs).init();
        QueryProcess exec = QueryProcess.create(gs);

        String q = "select debug "
                //               + "(not exists{filter(?x < ?y) . ?x ?p ?y } as ?res) "
                + "where {"
                + "?x ?p ?y { graph ?g { ?z ?q ?t  filter(?p != rdf:value)}} filter(?g < 12) "
                + "}";

        Mappings map = exec.query(q);

        System.out.println(map.getQuery());
        System.out.println(map);
        System.out.println(map.size());

    }

    //@Test
    public void testServ() throws EngineException {
        GraphStore gs = GraphStore.create();
        GraphStoreInit.create(gs).init();
        QueryProcess exec = QueryProcess.create(gs);

        String q = "select debug * where {"
                + "values  ?s1 { <http://fr.dbpedia.org/sparql>  } "
                + "service ?s1 { ?x rdfs:label 'Auguste'@fr } "
                + "values  ?s2 { <http://fr.dbpedia.org/sparql> } "
                + "service ?s2 { select * where {?x ?q ?Z} limit 10 } "
                + "}"
                + "pragma {kg:service kg:slice 10}";

        String q1 = "select debug * where {"
                + "service ?s1 { ?x rdfs:label 'Auguste'@fr } "
                + "service ?s2 { select * where {?x ?q ?Z} limit 10 } "
                + "}"
                + "values  (?s1 ?s2){ (<http://fr.dbpedia.org/sparql>  <http://fr.dbpedia.org/sparql> )} "
                + "pragma {kg:service kg:slice 10}";


        String q2 = "select debug * where {"
                + "values  ?s1 { <http://fr.dbpedia.org/sparql>  } "
                + "service ?s1 { ?x rdfs:label 'Auguste'@fr } "
                + "optional {?s1 ?p ?v} "
                + "service <http://fr.dbpedia.org/sparql> { select * where {?x ?q ?Z} limit 10 } "
                + "}"
                + "pragma {kg:service kg:slice 10}";


        String q3 = "select debug * where {"
                + "{service <http://fr.dbpedia.org/sparql> { ?x <http://dbpedia.org/ontology/deathPlace> ?y } }"
                + "union"
                + "{service <http://fr.dbpedia.org/sparql> {?z <http://dbpedia.org/ontology/deathPlace> ?t } }"
                + "}";



        Mappings map = exec.query(q3);

        System.out.println(map.getQuery());
        System.out.println(map);
        System.out.println(map.size());


    }

    public void testList33() throws EngineException {
        GraphStore gs = GraphStore.create();
        GraphStoreInit.create(gs).init();
        QueryProcess exec = QueryProcess.create(gs);

        String init = "insert data {"
                + "[] rdf:value (1 2 3)"
                + "}";

        String q =
                "select ?e (count(?mid) as ?c) where {"
                + "?x rdf:value ?head "
                + "?head rdf:rest* ?mid "
                + "?mid  rdf:rest* ?node "
                + "?node rdf:first ?e "
                + "}"
                + "group by ?node ?e "
                + "order by ?c";

        exec.query(init);

        Mappings map = exec.query(q);


        System.out.println(map);

    }

    public void testDT() throws EngineException {
        GraphStore gs = GraphStore.create();
        GraphStoreInit.create(gs).init();
        QueryProcess exec = QueryProcess.create(gs);

        String init =
                "insert data { "
                + "[ rdf:value '2013-11-11'^^xsd:gYear "
                + ", '2013-11-11'^^xsd:gMonth "
                + ", '2013-11-11'^^xsd:gDay "
                + ", 'ar'^^xsd:int "
                + ", 'toto'^^xsd:double "
                + ""
                + "]}";

        String q = "select (datatype(?y) as ?res)  where {?x ?p ?y}";

        exec.query(init);

        Mappings map = exec.query(q);
        System.out.println(map);

        assertEquals(5, map.size());
        assertEquals(false, gs.getProxy().typeCheck());


    }

    public void test45() {

        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix c: <http://test/> "
                + "insert data {"
                + "tuple(c:name <John>  'John' 1)"
                + "tuple(c:name <John>  'John' 2)"
                + "}";

        String query =
                "prefix c: <http://test/> "
                + "select * where {"
                + "graph ?g { tuple(c:name ?x ?n ?v) }"
                + "}";

        String del = "prefix c: <http://test/> "
                + "insert {tuple(c:name ?x ?n )}"
                + "where {tuple(c:name ?x ?n ?v)}";

        try {
            exec.query(init);
            System.out.println(graph.display());
            Mappings map = exec.query(del);
            System.out.println(map);
            System.out.println(graph.display());

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testSystem() throws EngineException {
        GraphStore gs = GraphStore.create();
        GraphStoreInit.create(gs).init();
        QueryProcess exec = QueryProcess.create(gs);

        String init = "insert data { graph kg:system { "
                + "kg:kgram kg:listen true "
                + "kg:store sp:query true "
                + "}}";

        String q = "select * where  {?x ?p ?y}";

        String query = "select ?res where {"
                + "graph kg:query {"
                + "select (kg:pprint(?q) as ?res) where {"
                + "?q a sp:Select"
                + "}"
                + "}"
                + "}";

        exec.query(init);
        exec.query(q);

        Mappings map = exec.query(query);

        IDatatype dt = (IDatatype) map.getValue("?res");
        assertEquals(true, dt.getLabel().contains("?x ?p ?y"));


    }

    public void translate() {
        TemplatePrinter p = TemplatePrinter.create(root + "pprint/asttemplate", root + "pprint/turtle.rul");
        try {
            p.process();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LoadException e) {
            e.printStackTrace();//    public static void main(String[] args){
//        new TestUnit().testSPIN3();
//    }
        }
    }

    //@Test
    public void testQVV() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        QueryLoad ql = QueryLoad.create();
        Load ld = Load.create(g);

        String q = ql.read(data + "work/listmatch.rq");

        System.out.println(q);

        SPINProcess sp = SPINProcess.create();

        String spin = sp.toSparql(q);

        System.out.println(spin);

        ql.write(data + "work/listmatch.ttl", spin);

        ld.loadWE(data + "work/listmatch.ttl");


        String qq = ql.read(data + "work/querylistmatch.rq");
        System.out.println(qq);

        exec.setVisitor(ExpandList.create());
        Mappings map = exec.query(qq);


        System.out.println(map);
        System.out.println(map.size());

        System.out.println(map.getQuery().getAST());

    }

    //@Test
    public void testQV() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryProcess exec = QueryProcess.create(g);

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<John> a foaf:Person ;"
                + " foaf:knows ((<Jack> <John>) <Jim>)"
                + "}";

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select * (kg:pprint(?x) as ?res)  where {"
                + "?x foaf:knows @ (<Jim> (?x) ) "
                + "}"
                + "pragma {"
                //                + "kg:list kg:expand true "
                //                + "kg:path kg:expand true"
                + "}"
                + "";
        try {
            exec.query(init);
            //exec.setVisitor(ExpandList.create());
            Mappings map = exec.query(query);
            System.out.println(map.getQuery().getAST());
            System.out.println(map);
            System.out.println("size: " + map.size());




        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testPPrintghjfgh() throws LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g);

        ld.loadWE(root + "pprint/data/");

        NSManager nsm = NSManager.create();
        nsm.definePrefix("ex", "http://www.example.org/");
        nsm.definePrefix("ast", "http://www.inria.fr/2012/ast#");

        Date d1 = new Date();

        TemplateFormat tf = TemplateFormat.create(g);
        tf.setPPrinter(root + "pprint/asttemplate");
        tf.setNSM(nsm);
        String str = tf.toString();

        Date d2 = new Date();
        System.out.println(str);

        assertEquals("Results", 3055, str.length());
        System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / 1000.0);

//        str = nsm.toString() + "\n" + str;
//
//        InputStream io = new ByteArrayInputStream(str.getBytes());
//        Graph gg = Graph.create();
//        Load ll = Load.create(gg);
//        try {
//            ll.load(io, "test.ttl");
//            System.out.println(g.size() + " " + (gg.size() + 1));
//
//            assertEquals("Results", g.size(), gg.size());
//        } catch (LoadException e) {
//            e.printStackTrace();
//        }

    }

    public void testbug() throws EngineException {
        QueryLoad ql = QueryLoad.create();

        String str = ql.read("/home/corby/NetBeansProjects/kgram/trunk/kgtool/target/test-classes/data/w3c-sparql11/sparql11-test-suite/bind/bind10.rq");
        System.out.println(str);
        ASTQuery ast = ASTQuery.create(str);
        ParserSparql1.create(ast).parse();
        SPIN sp = SPIN.create();
        sp.visit(ast);

        System.out.println(sp.getBuffer());
        System.out.println(ast);

        SPINProcess spp = SPINProcess.create();
        System.out.println(spp.toSpinSparql(str));

    }

    public void testOumy() throws EngineException, LoadException {

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.loadWE(data + "work/owlrl.rul");
        QueryLoad ql = QueryLoad.create();

        RuleEngine re = ld.getRuleEngine();

        int i = 0;

        for (Rule r : re.getRules()) {
            System.out.println(i++);
            ASTQuery ast = (ASTQuery) r.getQuery().getAST();

            SPINProcess sp = SPINProcess.create();
            String spin = sp.toSpin(ast);
            String sparql = sp.toSparql(spin);
//            System.out.println(ast);
//            System.out.println(spin);
//            System.out.println(sparql);
            exec.compile(sparql);

            //ql.write(data + "work/owlrl/f" + i + ".ttl", spin);
        }

    }

    //@Test
    public void testLoc() throws EngineException, LoadException {

        String init = FOAF_PREF
                + "insert data { "
                + "[ foaf:knows <Jim> ] . "
                + "<Jim> foaf:knows <James> "
                + "<Jim> rdfs:label 'Jim' "
                + " "
                + "}";

        if (null instanceof Object) {
            System.out.println("yes");
        } else {
            System.out.println("no");
        }
        GraphStore gs = GraphStore.create();
//        Graph gg = g.createNamedGraph(Graph.SYSTEM);
//        gg.addListener(new GraphListenerImpl(g));
        GraphStoreInit.create(gs).init();
        Graph gg = gs.getNamedGraph(Graph.SYSTEM);
        Transformer pp = Transformer.create(gg, Transformer.TURTLE);
        System.out.println(pp);
        Load ql = Load.create(gs);
        QueryProcess exec = QueryProcess.create(gs);

        String query =
                "insert { graph kg:system { ?x ?p ?y } }"
                + " where {"
                + "?x ?p ?y "
                + "graph ?g {  }"
                + "}";

        String q = FOAF_PREF
                + "select debug *"
                + "where {"
                //+ "graph ?g { ?x ?p ?y  }"
                + "graph kg:system { "
                + "?a kg:version+ ?b "
                + "filter ("
                + "?a != ?a || "
                + "if (! (exists { ?a kg:date+ ?d } = false),  true, false)"
                + ")"
                + "filter not exists { ?x foaf:knows ?y }"
                + "}"
                + "}";

        String qq = FOAF_PREF
                + "select * where {"
                + "?x foaf:knows+ :: $path ?y "
                + "graph $path { filter exists { ?a foaf:knows ?b }"
                // + "?a foaf:knows+ ?b "
                + "}"
                + "}";

//        Mappings map = exec.query(query);
//        System.out.println(map);
        System.out.println("init");
        exec.query(init);
        // System.out.println(gg);
        Mappings map = exec.query(q);
        System.out.println(map);
        System.out.println(map.size());
    }

    public void testProv() throws EngineException, LoadException {

        String init = FOAF_PREF
                + "insert data { <John> foaf:knows <Jim>, <James> }";
        Graph g = Graph.create();
        Load ql = Load.create(g);
        ql.loadWE(data + "work/owlrl");
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);

        for (Entity ent : g.getEdges()) {
            EdgeImpl edge = (EdgeImpl) ent;
            edge.add(g.getNode(DatatypeMap.newDate(), true, false));
            ent.setProvenance(g);

        }

        String query = FOAF_PREF
                + "select *  where {"
                + "tuple(?p ?x ?y ?t ?prov)"
                //+ "tuple(foaf:knows <John>  ?v ?prov) "
                //+ "graph ?prov { <John>  ?p ?v }"
                + "}"
                // + "group by ?prov"
                + "";

        Mappings map = exec.query(query);
        System.out.println(map);
        // assertEquals("result", 2, map.size());


        for (Mapping m : map) {
            for (Entity ent : m.getEdges()) {
                // assertEquals("result", true, ent.getProvenance() != null);
            }
        }

    }

    public void testlist() throws EngineException {
        StringBuilder sb = new StringBuilder(FOAF_PREF);
        sb.append("insert data {\n");
        sb.append("<John> foaf:list (\n");
        for (int i = 0; i < 10; i++) {
            sb.append(i + " ");
        }
        sb.append(")}");

        System.out.println(sb);

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.setListPath(true);
        exec.query(sb.toString());
        QueryLoad ql = QueryLoad.create();

        //String query = ql.read(data + "work/list.rq");
        String query = ql.read(data + "work/prov.rq");
        Mappings map = null;
        Date d1 = new Date();
        int n = 1;
        for (int j = 0; j < n; j++) {
            System.out.println(j);
            map = exec.query(query);
        }
        Date d2 = new Date();

        System.out.println(map);
        System.out.println(map.size());
        System.out.println("time: " + (d2.getTime() - d1.getTime()) / (n * 1000.0));

        for (Mapping m : map) {
            for (Entity ent : m.getEdges()) {
                Node node = (Node) ent.getProvenance();
                System.out.println(node.getObject());
            }
        }
    }

    public void testSTTLXML() throws EngineException, LoadException {
        Graph g = Graph.create();
        String init = "insert data {"
                + "[] rdf:value "
                + "'<doc>"
                + "<phrase><subject>s</subject><verb>p</verb><object>o</object></phrase>"
                + "<phrase><subject>s</subject><verb>p2</verb><object>o</object></phrase>"
                + "</doc>'^^rdf:XMLLiteral"
                + ""
                + "}";

        String query =
                "template {"
                + "?s ' ' ?p ' ' ?o"
                + "}"
                + "where {"
                + "?x rdf:value ?xml "
                + "bind(xpath(?xml, '/doc/phrase') as ?sent)"
                + "bind(xpath(?sent, 'subject/text()') as ?s) "
                + "bind(xpath(?sent, 'verb/text()') as ?p) "
                + "bind(xpath(?sent, 'object/text()') as ?o) "
                + "}";

        QueryProcess exec = QueryProcess.create(g);

        exec.query(init);

        Mappings map = exec.query(query);

        System.out.println(map.size());
        System.out.println(map.getTemplateResult().getLabel());

    }

    public void testBlank2() throws LoadException, EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init = FOAF_PREF
                + "insert data {"
                + "<John> a foaf:Person ; rdfs:seeAlso _:b1 , _:b2"
                + "<Jack> rdfs:seeAlso _:b1 "
                + "_:b1 a foaf:Person ; rdfs:label 'test' , 'tyty'"
                + "_:b2 a foaf:Person ; rdfs:label 'toto' , 'tete'"
                + "}";

        exec.query(init);

        Transformer pp = Transformer.create(g, Transformer.TURTLE);
        String str = pp.transform();
        System.out.println(str);


    }

    public void testSPIN22() throws LoadException {
        Graph g = Graph.create(true);
        Load ld = Load.create(g);
        ld.loadWE(data + "template/spin/data");

        Transformer pp = Transformer.create(g, Transformer.SPIN);
        pp.definePrefix("foaf", "http://xmlns.com/foaf/0.1/");

        String str = pp.transform();
        System.out.println(str);


    }

    //@Test
    public void testSTTL() throws EngineException, LoadException, FileNotFoundException, IOException {
        Graph g = Graph.create(true);
        Load ld = Load.create(g);
        System.out.println("Load");
        ld.loadWE(data + "work/tmp.ttl");
        //ld.load(data + "template/owl/data/primer.owl"); 
        //ld.load(data + "template/owl/data/galen.ttl");  // 2.0928
        //ld.load(data + "template/owl/data/hao.owl");

        QueryProcess exec = QueryProcess.create(g);


        String ppowl = data + "template/owl/template/uri.rq";
        String temp =
                SQL_PREF
                + "prefix f: <http://example.com/owl/families/> "
                + "template  { ?c ; separator = '\\n\\n'}"
                + "where {"
                + "?c a owl:Class "
                + "filter(isURI(?c)) "
                // + "filter(?c = f:Person)"                
                + "}"
                + "order by ?c "
                + "";

        temp =
                SQL_PREF
                + "prefix f: <http://example.com/owl/families/> "
                + "prefix e: <http://example.org/factkb#>"
                + "prefix obo: <http://purl.obolibrary.org/obo/> "
                + "prefix oo: <http://www.geneontology.org/formats/oboInOwl#>"
                + "prefix a: <http://api.hymao.org/api/ref/>"
                //+ "template  { st:apply-templates-with('/home/corby/AData/work/test') }"
                + "template  { kg:write('/home/corby/AData/work/tmp.ttl',"
                + "st:apply-templates-with(st:trig)) "
                + "}"
                + "where {"
                + " "
                + "}";



//temp =    
//            "prefix f: <http://example.com/owl/families/> "
//            + "template "
//        + " [st:turtle] "
//        + "{"
//+ "?x ' ' ?p ' ' ?y "
//        + "; separator = '\\n'" 
//+ "}"
//+ "where {"
//        + "?x ?p ?y "
//+ "}" ; 
//    

//1636514
//885568 552146
        /**
         * new: size in char: 4691 nb answer: 1
         *
         *
         * size in char: 3138 nbt: 2314 nb answer: 32
         */
        //PPrinter.setOptimizeDefault(false);
//         PPrinter.define(PPrinter.OWL, true);
        //PPrinter.setExplainDefault(true);
        //PPrinter.define(ppowl, true);
        Mappings map = exec.query(temp);
        System.out.println(map.getTemplateResult().getLabel());
        System.out.println(map.getQuery().getAST());

//        if (map.getTemplateResult() != null){
//           // System.out.println(map.getTemplateResult().getLabel());
//           QueryLoad ql = QueryLoad.create();
//           ql.write( data + "work/tmp.txt", map.getTemplateResult().getLabel());
//            System.out.println("size in char: " + map.getTemplateResult().getLabel().length());
//        }
        // 8765
        if (true) {
            Date d1 = new Date();
            int n = 0;
            for (int i = 0; i < n; i++) {
                System.out.println(i);
                map = exec.query(temp);
            }
            Date d2 = new Date();
            //System.out.println(map.getTemplateResult().getLabel());
            System.out.println(map.size());
            System.out.println("time: " + (d2.getTime() - d1.getTime()) / (n * 1000.0));
        }

        //System.out.println("res: \n" + map.getTemplateResult().getLabel());

        Transformer pp = Transformer.create(Transformer.OWL);

        pp.definePrefix("f", "http://example.com/owl/families/");
        //System.out.println("res: " + pp.transform(data + "template/owl/data/primer.owl"));
        //pp.transform(new FileInputStream(data + "template/owl/data/hao.owl"), System.out, Load.RDFXML_FORMAT);

//        Query q = map.getQuery();
//        Transformer p = (Transformer) q.getPP();
//        System.out.println("nbt: " + p.nbTemplates());
//        System.out.println("nb answer: " + map.size());
//        System.out.println("pp size: " + map.getTemplateResult().getLabel().length());
//        System.out.println("graph size: " + g.size());
        // 22807 tps
        // 208228 tps
        // 126712
//        QueryLoad ql = QueryLoad.create();
//        String str = ql.read(data + "work/oumy.rq");
//        
//        ld.loadWE(data + "work/owlrl.brul");
        /**
         *
         * time: 0.348 nbt: 9050
         *         
* time: 0.0864 nbt: 787
         *
         *
         *
         *
         * time: 0.2672 nbt: 8765 size: 38
         *         
* time: 0.2448 nbt: 6562 size: 38
         */
    }

    public void tttt() throws EngineException, LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        System.out.println("Load");
        ld.loadWE(data + "template/owl/data/primer.owl");
        QueryProcess exec = QueryProcess.create(g);

        String temp2 =
                "prefix f: <http://example.com/owl/families/>"
                // + "template { kg:pprintWith('/home/corby/AData/pprint/owl-save/template') }"
                + "template { kg:pprintWith(pp:owl) }"
                + "where {}";

        String temp =
                "prefix f: <http://example.com/owl/families/>"
                + "template debug { ?c ; separator = '\\n\\n'}"
                + "where {"
                + "?c a ?class "
                + "filter(isURI(?c))"
                //    + "filter(?c = f:Person)"
                + "}"
                + "values ?class { owl:Class }";

        Mappings map = exec.query(temp);




        System.out.println(map.getTemplateResult().getLabel());
        System.out.println(map.size());
        // 5296
    }

    public void ttt() throws EngineException , LoadException{
        Graph g = Graph.create();
        Load ld = Load.create(g);
        System.out.println("Load");
        ld.loadWE(data + "spin/data");
        ld.loadWE(data + "sql/data");

        QueryLoad ql = QueryLoad.create();
        String temp = ql.read(data + "sql/query/mixte.txt");

        QueryProcess exec = QueryProcess.create(g);
        System.out.println("First");
        Mappings map = exec.query(temp);



        System.out.println("Start");
        Date d1 = new Date();
        int n = 10;
        for (int i = 0; i < n; i++) {
            System.out.println(i);
            map = exec.query(temp);
        }
        Date d2 = new Date();
        System.out.println(map.getTemplateResult().getLabel());
        System.out.println(map.size());
        System.out.println((d2.getTime() - d1.getTime()) / (n * 1000.0));
        // 0.51
    }

    public void tt() throws EngineException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryLoad ql = QueryLoad.create();
        String q = ql.read(data + "work/test.rq");
        SPINProcess sp = SPINProcess.create();
        Graph gg = sp.toSpinGraph(q);

        String temp = SPIN_PREF + FOAF_PREF
                + "template { ?q }"
                + "where { ?q a sp:Select }";
        //PPrinter.define(NSManager.SPIN, PPrinter.TURTLE);
        QueryProcess exec = QueryProcess.create(gg);
        Mappings map = exec.query(temp);

        System.out.println(map.getTemplateResult().getLabel());
    }

    public void testSpinQueryGraph2() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryLoad ql = QueryLoad.create();
        String q = ql.read(data + "work/test.rq");
        String t = ql.read(data + "work/target.rq");
        String t2 = ql.read(data + "work/target.rq");

        SPINProcess sp = SPINProcess.create();
        SPINProcess sp2 = SPINProcess.create();

        try {
            //System.out.println(sp.toSpin(q));
            Graph qg = sp.toSpinGraph(q);

            Graph tg = Graph.create();
            sp2.toSpinGraph(t, tg);
            sp2.toSpinGraph(q, tg);

            QueryProcess exec = QueryProcess.create(tg);

            QueryGraph qq = QueryGraph.create(qg);
            QueryGraphVisitorImpl vis = QueryGraphVisitorImpl.create();
            // replace string name by variable
            vis.addPredicate(NSManager.SPIN + "varName");
            qq.setVisitor(vis);

            //exec.setDebug(true);
            Mappings map = exec.query(qq);

            System.out.println(map);

            System.out.println(map.size());

            // root of query AST
            Node node = qg.getRoot();
            // System.out.println(node);

            Transformer pp = Transformer.create(tg, Transformer.SPIN);
            pp.setNSM(sp.getNSM());

            int i = 1;
            for (Mapping m : map) {
                Node res = m.getNode(node);
                if (res != null) {
                    IDatatype dt = pp.process(res);
                    if (dt != null) {
                        System.out.println(i++);
                        System.out.println(dt.getLabel());
                    }
                }
            }

            assertEquals("result", 2, map.size());

        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testAtest() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryProcess exec = QueryProcess.create(g);

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<John> foaf:name 'John' ; foaf:age 18 "
                + "<Jim> foaf:name 'Jim' ; foaf:knows <John>"
                + "}";

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + "?x ?p ?y"
                + "}";
        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map.getQuery().getAST());
            System.out.println(map);
            System.out.println("size: " + map.size());

        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testGG() throws LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        System.out.println("load");
        ld.loadWE("/home/corby/Work/dbpedia.ttl");
        QueryProcess exec = QueryProcess.create(g);
        System.out.println("query");

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<John> foaf:name 'John' ; foaf:age 18 "
                + "<Jim> foaf:name 'Jim' ; foaf:knows <John>"
                + "}";

        String query = "prefix dbpedia-owl: <http://dbpedia.org/ontology/> \n"
                + "prefix geo:	<http://www.w3.org/2003/01/geo/wgs84_pos#>\n"
                + "select distinct ?label  ?codepostal (datatype(?codepostal) as ?dt)  where {\n"
                + "?s dbpedia-owl:region <http://fr.dbpedia.org/resource/Auvergne> .\n"
                + "?s rdfs:label ?label .\n"
                + "?s dbpedia-owl:postalCode ?codepostal .\n"
                + "?s dbpedia-owl:populationTotal ?pop .\n"
                + "?s geo:lat ?lat .\n"
                + "?s geo:long ?lon .\n"
                + "}";
        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map.getQuery().getAST());
            //System.out.println(map);
            ResultFormat f = ResultFormat.create(map);
            System.out.println("size: " + map.size());
            System.out.println(f);

        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //@Test
    public void testToSpin() throws EngineException, IOException {
        String sparql =
                "prefix sp: <http://spinrdf.org/sp#>"
                + "select * where {"
                + "?x sp:test [ sp:test1 ?v1 ; sp:test2 ?v2] bind(concat(?v1, ?v2) as ?o)"
                + "}";

        QueryLoad ql = QueryLoad.create();
        InputStream stream = Graph.class.getResourceAsStream("/update/path.rq");
        String str = ql.read(stream);

        SPINProcess sp = SPINProcess.create();

        String spin = sp.toSpin(str);

        System.out.println(spin);

    }

    //@Test
    public void testJoin2() {
        Graph g = Graph.create();
        Load ld = Load.create(g);

        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data   {"
                + "graph <g1> {"
                + "<http://fr.dbpedia.org/resource/Auguste>  foaf:knows <James> ; "
                + "foaf:service <http://fr.dbpedia.org/sparql> "
                + "}"
                + "graph <g1> {"
                + "<Jim> foaf:knows <James>"
                + "<Jim> foaf:name 'Jim' "
                + "}"
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select debug * where {"
                + "?x foaf:knows ?y "
                + //  "; foaf:service ?s " +
                "service ?s {"
                + "select * where {"
                + "?x rdfs:label ?n "
                + "filter(regex(?n, '^August'))"
                + "} limit 2"
                + "}"
                + "service <http://fr.dbpedia.org/sparql>{"
                + "select * where {"
                + "?x rdfs:label ?n "
                + "}"
                + "}"
                + "}"
                + "values ?s { <http://fr.dbpedia.org/sparql> <http://dbpedia.org/sparql>} "
                + //"pragma {kg:kgram kg:detail true}" +
                "";

        String qq = "select * where {"
                + "?x ?p ?y "
                + "?z ?q ?t ."
                + "filter(?x != ?z)"
                + ""
                + "}"
                + "pragma {kg:kgram kg:test true}";

        QueryProcess exec = QueryProcess.create(g);
        //exec.setSlice(30);
        exec.setDebug(true);

        try {
            //exec.query(init);

            Mappings map = exec.query(qq);
            System.out.println(map);

            //assertEquals("Result", 7, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }



    }

    public void testQM() {
        Graph g = Graph.create();
        QueryManager man = QueryManager.create(g);
        QueryProcess exec = QueryProcess.create(g);
        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<John> foaf:name 'John' ; foaf:age 18 "
                + "<Jim> foaf:name 'Jim' ; foaf:knows <John>"
                + "}";

        String query = "prefix sp: <http://spinrdf.org/sp#>"
                + "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + "?x foaf:name ?n "
                + "?x foaf:knows ?p "
                + "minus { ?x foaf:age ?a } "
                + "<James> foaf:fake ?f "
                + "?f a foaf:Person "
                + "?f sp:elements ?e "
                + "?f sp:test ?t "
                + "filter(?b >= 20)"
                + "}";

        query = "select * where {"
                + "?x rdf:test* ?y"
                + "}";

        try {
            exec.query(init);

            Mappings map = man.query(query);
            System.out.println(map.getQuery().getAST());
            System.out.println(map);
            System.out.println("size: " + map.size());
            //assertEquals("result", 1, map.size());
        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
            assertEquals("result", true, ex);
        }

    }

    public void test55() {


        String o1 = "prefix foaf: <http://foaf.org/>"
                + "insert data {"
                + "foaf:Human rdfs:subClassOf foaf:Person "
                + "}";

        String o2 = "prefix foaf: <http://foaf.org/>"
                + "insert data {"
                + "foaf:Man rdfs:subClassOf foaf:Person "
                + "}";

        String init1 = "prefix foaf: <http://foaf.org/>"
                + "insert data {"
                + "<John> a foaf:Human"
                + "}";

        String init2 = "prefix foaf: <http://foaf.org/>"
                + "insert data {"
                + "<Jack> a foaf:Man"
                + "}";




        String query = "prefix foaf: <http://foaf.org/>"
                + "select * where {"
                + "?x a foaf:Person"
                + "}";

        Graph o = Graph.create(true);
        Graph g1 = Graph.create(true);
        Graph g2 = Graph.create(true);

        QueryProcess exec1 = QueryProcess.create(g1);
        QueryProcess exec2 = QueryProcess.create(g2);

        QueryProcess exec = QueryProcess.create(g1, true);
        exec.add(g2);


        try {
            exec1.query(o1);
            exec1.query(init1);

            exec2.query(o2);
            exec2.query(init2);

//			exec.query(o1);
//			exec.query(o2);

            Mappings map = exec.query(query);
            System.out.println(map);

            String str = "123";
            System.out.println(str.toLowerCase().contains(str.toLowerCase()));

            assertEquals("Result", 2, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }
    }

    public void test63() {
//        Graph.setValueTable(true);
//        Graph.setCompareKey(true);
        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                + "insert data {"
                + "<A> foaf:date 20 "
                + "} ;"
                + "insert data {"
                + "<B> foaf:date 20 "
                + "} ";


        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                + "select * where {"
                + "?x foaf:date ?date"
                + "}"
                + "order by desc(?date)";


        String test = "prefix : <http://example.org/> "
                + "select ?z  where "
                + "{?s ?p ?o . "
                + "bind ((?o + 10) as ?z) "
                + "}";

        try {
            SPINProcess sp = SPINProcess.create();
            sp.setDebug(true);
            String s = sp.toSpinSparql(init);
            //System.out.println(s);
            Mappings map = exec.query(init);
//            System.out.println(map.getQuery().getAST());
//            System.out.println(graph.display());
//            for (Entity e : graph.getEdges()) {
//                System.out.println(e.getNode(1));
//            }
            sp.setDebug(true);
            String str = sp.toSpinSparql(test);
            Dataset ds = Dataset.create();
            ds.addFrom(Constant.create(Entailment.DEFAULT));
            //ds.addNamed(Constant.create("empty"));
            exec.setDebug(true);
            map = exec.sparql(str, ds);
            System.out.println(map);
            System.out.println(map.getQuery().getAST());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    ;
           

     
    public void test14() throws LoadException {

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select  *  where {"
                + "?x rdf:type c:Person; c:hasCreated ?doc "
                + "?doc rdf:type/rdfs:subClassOf* c:Document "
                + "c:Document rdfs:label ?l ;"
                + "rdfs:comment ?c"
                + "}";

        try {

            Graph g = Graph.create(true);
            Load ld = Load.create(g);
            //ld.setBuild(new MyBuild(g));

            init(g);

            QueryProcess exec = QueryProcess.create(g);

            QueryProcess.definePrefix("c", "http://www.inria.fr/acacia/comma#");

            Mappings map = exec.query(query);
            assertEquals("Result", 68, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }





    }

//        public static void main(String[] args){
//        new TestUnit().testDistType();
//    }
    public void iiii() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init =
                "@prefix ast: <http://www.inria.fr/2012/ast#> "
                + "insert data {"
                + "[a ast:SelectQuery ; "
                + "ast:select ( '*' "
                + "[ast:var [a ast:Var ; ast:name '?xx'] ; "
                + " ast:exp [ast:fun 'self' ; ast:body ( [a ast:Var ; ast:name '?x'])]"
                + "]"
                + ") ;"
                + "ast:where ("
                + "[ast:subject  [a ast:Var ; ast:name '?x'] ;"
                + " ast:property [a ast:Var ; ast:name '?p'] ;"
                + " ast:object   [a ast:Var ; ast:name '?y']]"
                + ")"
                + "]"
                + "}";


        try {
            exec.query(init);

            Transformer pp = Transformer.create(g);
            IDatatype dt = pp.process();
            System.out.println(dt.getLabel());

            Mappings map = exec.query(g);
            System.out.println(map);


            assertEquals("Result", 1, map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            assertEquals("Result", true, false);
            e.printStackTrace();
        }

    }

    public void testSPIN4() {
        Graph g = Graph.create(true);

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<John> a foaf:Person ; foaf:knows <James> "
                + "<Jim> a foaf:Person "
                + "}";

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select ?x (count(?y) as ?c) where {"
                + " { ?x a foaf:Test } union { ?x a foaf:Person ; foaf:pp* :: $path ?z }"
                + "optional { ?x foaf:knows ?y } "
                + "minus { ?x a foaf:Test } "
                + "filter(bound(?x) && ?x != 12)"
                + "}"
                + "group by ?x "
                + "having (?c >= 0)";
        try {

            SPINProcess sp = SPINProcess.create();
            QueryProcess exec = QueryProcess.create(g);
            sp.setDebug(true);
            Mappings m = exec.query(init);
            String str = sp.toSpinSparql(query);
            Mappings map = exec.query(sp.toSparql(sp.toSpin(query)));
            System.out.println(map);
            System.out.println(map.getQuery().getAST());

            assertEquals("result", 2, map.size());

        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testSPIN() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryProcess exec = QueryProcess.create(g);

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "}";

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select  "
                + "?x  (?x + 2 * isURI(?y) as ?z)"
                + "(count(distinct ?x) as ?c) "
                + "(group_concat(?x ; separator=';') as ?g) "
                + "from <http://example.org/gg1> "
                + "from named <http://example.org/gg2>"
                + "where {"
                + "service <http://example.org> {"
                + "values (?x ?y) { (12 13) (14 15) }  "
                + "{?x foaf:name 'John' } union { ?x foaf:name 'Jack'}  "
                + "graph ?g { ?x (foaf:knows/foaf:knows+)* ?z } "
                + "optional { ?x foaf:age (12)  minus { ?x foaf:age 222 }} "
                + "minus { ?x foaf:age 333 }"
                + "}"
                + "filter exists {?x ?p ?y}"
                + "}"
                + "group by ?y "
                + "order by desc(?x) ?x "
                + "having(?x != 0 && count() > 1)"
                + "values ?x { 23 undef 24}";

        String update = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                //                "load foaf:graph into graph foaf:gg ;"
                + "insert data {<John> foaf:knows <James>} ;"
                + "delete {?x ?p ?y}"
                + "insert {?z ?q ?t}"
                + "where {"
                + "?a foaf:knows ?b "
                + "filter(?a not in (22, 23))"
                + "}";

        String ask = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "ask { <John> foaf:name ?n  }";

        String desc = "describe ?x <John>";
        try {
            Query qq = exec.compile(query);
            System.out.println(qq.getAST());

            ASTQuery ast = exec.getAST(qq);

            SPIN sp = SPIN.create();
            sp.visit(ast);

            System.out.println("spin:\n" + sp);

            String str = sp.toString();

            ld.load(new ByteArrayInputStream(str.getBytes("UTF-8")), "spin.ttl");

            TemplateFormat tf = TemplateFormat.create(g, "/home/corby/AData/spin/template");
            tf.setNSM(ast.getNSM());
            System.out.println(tf);


        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        } catch (LoadException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testRes() {
        String fileName = "/junit/spin.rul";
        InputStream resource = this.getClass().getResourceAsStream(fileName);
        System.out.println("stream: " + resource);
        BufferedReader read = new BufferedReader(new InputStreamReader(resource));
        StringBuffer buf = new StringBuffer();
        try {
            while (true) {
                String str = read.readLine();
                if (str == null) {
                    read.close();
                    break;
                }
                buf.append(str);
                buf.append("\n");

            }
        } catch (IOException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("** res: \n" + buf);

    }

    public void testXML() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryProcess exec = QueryProcess.create(g);

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "[ foaf:member <John> , <Jack>] ."
                + ""
                + "<John> foaf:author 'b1', 'b2' "
                + "<Jack> foaf:author 'b4', 'b3' "
                + "}";

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "template {kg:pprintWith(<" + data + "result>)}"
                + "where {}";

        String temp = QueryLoad.create().read(data + "test/template.rq");
        String tt = QueryLoad.create().read(data + "test/tmp.rq");


        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map.getTemplateResult().getLabel());
            System.out.println("size: " + map.size());

            XMLResult r = XMLResult.create(exec.getProducer());

            Mappings m = r.parseString(map.getTemplateResult().getLabel());

            System.out.println(m.getValue("?p"));

        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testPPP() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryProcess exec = QueryProcess.create(g);

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "[ foaf:member <John> , <Jack>] ."
                + ""
                + "<John> foaf:author 'b1', 'b2' "
                + "<Jack> foaf:author 'b4', 'b3' "
                + "}";

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + ""
                + "}";

        String temp = QueryLoad.create().read(data + "test/template.rq");
        String tt = QueryLoad.create().read(data + "test/tmp.rq");


        try {
            exec.query(init);
            Mappings map = exec.query(temp);
            System.out.println(map.getTemplateResult().getLabel());
            System.out.println("size: " + map.size());

        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testList3() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryProcess exec = QueryProcess.create(g);

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "[ rdf:value (1 1 1 4 1 5) ]"
                + "}";

        String update = QueryLoad.create().read(data + "test/list.rq");

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + "?x ?p ?e "
                + "}";
        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println("size: " + map.size());

            exec.query(update);

            map = exec.query(query);
            System.out.println(map);
            System.out.println("size: " + map.size());

        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testMytest() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryProcess exec = QueryProcess.create(g);

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "[] foaf:arg1 1 ; foaf:arg11 3 ; foaf:arg2 2 "
                + "}";

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select * "
                + "(xsd:integer(substr(?p, 1 + strlen(foaf:arg))) as ?o)"
                + "where {"
                + "?x ?p ?v"
                + "}"
                //+ "order by ?p "
                + "order by xsd:integer(substr(?p, 1 + strlen(foaf:arg)))"
                + "";
        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println("size: " + map.size());

        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    public void testDataset() {
        Graph g = Graph.create(true);
        Load ld = Load.create(g);
        Dataset ds = Dataset.create();
        ds.setUpdate(true);
        ds.addFrom("http://inria.fr/g2");
        ds.addNamed("http://inria.fr/g1");

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "graph <http://inria.fr/g1> {"
                + "<John> foaf:name 'John' ; a foaf:Person"
                + "}"
                + "graph <http://inria.fr/g2> {"
                + "<Jim> foaf:name 'Jim' ; a foaf:Person"
                + "}"
                + "graph <http://inria.fr/o> {"
                + "foaf:Person rdfs:subClassOf foaf:Human"
                + "}"
                + "}";

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select * "
                + "from named <http://inria.fr/g2>"
                + "where {"
                + "    {?x rdf:type foaf:Person ; ?p ?y}"
                + "}";

        String query2 = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select * "
                + "from <http://inria.fr/g2>"
                + "where {"
                + " graph ?g   {?x rdf:type foaf:Person ; ?p ?y}"
                + "}";

        String update = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "delete  where {?x ?p ?y}";

        try {
            QueryProcess sparql = QueryProcess.create(g);
            sparql.query(init);

            Mappings map = sparql.sparql(query, ds);
            assertEquals("result", 0, map.size());

            QueryProcess exec = QueryProcess.create(g);
            Mappings map2 = exec.query(query, ds);
            assertEquals("result", 2, map2.size());

            map = sparql.sparql(query2, ds);
            assertEquals("result", 0, map.size());

            map2 = exec.query(query2, ds);
            assertEquals("result", 2, map2.size());

        } catch (EngineException ex) {
            System.out.println(ex);

        }



    }

    public void testsrjfhgk() {
        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<John>  foaf:name 'http://www.inria.fr' "
                + "<Jack>  foaf:name 'http://www.inria.fr' "
                + "<James> foaf:name <http://www.inria.fr> "
                + "<Jim>   foaf:name <http://www.inria.fr> "
                + "<John>  foaf:name 'http://www.inria.fr'@en "
                + "<Jack>  foaf:name 'http://www.inria.fr'@en "
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select  * where {"
                + "?x foaf:name ?a "
                + "?y foaf:name ?a "
                + "filter(?x < ?y) "
                + "}";

        Graph g = Graph.create();
        Graph.setValueTable(true);
        Graph.setCompareKey(true);
        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            for (Mapping m : map) {
                IDatatype dt = (IDatatype) m.getValue("?a");
                System.out.println(dt);
                System.out.println(dt.getClass().getName());
            }
            System.out.println(map);
            assertEquals("Result", 3, map.size());


        } catch (EngineException e) {
            assertEquals("Result", 2, e);
        }

    }

    public void testNSM() throws LoadException {
        NSManager nsm = NSManager.create();

        nsm.setBase("http://example.org/x/");

        nsm.definePrefix("", "#");

        System.out.println(nsm.toNamespaceB(":x"));

        System.out.println(nsm.toNamespaceB("x"));

        //DatatypeMap.setLiteralAsString(false);

        IDatatype dt = DatatypeMap.newStringBuilder("test");

        System.out.println(dt.toString());




        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.loadWE(data + "sql/data/q1.ttl");

        Transformer pp = Transformer.create(g, data + "sql/template");

        System.out.println(pp.process());

    }

    public void testAtest1() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/>"
                + "insert data {"
                + "<John> foaf:knows <Jack> "
                + "<John> foaf:knows <Jim> "
                + "<Jack> foaf:name 'Jack' "
                + ""
                + "}";

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/>"
                + "select * where {"
                + "?x foaf:knows ?y"
                + "}";

        String update = "prefix foaf:    <http://xmlns.com/foaf/0.1/>"
                + "delete  {?x foaf:knows ?y}"
                + "insert {?x rdfs:seeAlso ?y}"
                + "where {?x foaf:knows ?y}";

        String delete = "prefix foaf:    <http://xmlns.com/foaf/0.1/>"
                + "delete "
                + "where {?x foaf:knows ?y}";

        GListener gl = GListener.create();
        g.addListener(gl);

        try {
            exec.query(init);
            Mappings map = exec.query(query);
//            System.out.println(map);
//            System.out.println("size: " + map.size());

            map = exec.query(update);

            // map = exec.query(delete);


            Graph gg = Graph.create();
            gg.index();
            for (Operation o : gl.getOperations()) {
                for (Entity ent : o.getDelete()) {
                    gg.delete(ent);
                }
                for (Entity ent : o.getInsert()) {
                    gg.insert(ent);
                }
            }

            System.out.println(gg.display());


            String q = "select (max(count(*)) as ?c) where {"
                    + "?x ?p ?y "
                    + "} group by ?x";

            Mappings m = exec.query(q);
            System.out.println(m);

//            System.out.println(map);
//            XMLFormat f = XMLFormat.create(map);
//            System.out.println(f);
//            System.out.println("size: " + map.size());
//            System.out.println(g.display());

        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testAG() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        //g.setSkolem(true);

        // construct graph setSkolem(true)
        exec.setSkolem(true);

        String q = "prefix foaf: <http://foaf.org/>"
                + "select  * "
                + "('a-01-16'^^foaf:date as ?d)"
                + "('a-01-16'^^foaf:date as ?dd)"
                + "(datatype(?d) as ?dt) (?d = ?dd as ?bool)"
                + "('-10-01-16'^^xsd:date as ?d2)"
                // + "(kg:skolem(bnode()) as ?bb)"
                + "where {"
                + ""
                + "?a ?p ?b "
                //+ "filter(kg:isSkolem(?b))"
                // check that blank is not already in another graph than gNode

                + "}";


        String q2 = "prefix foaf: <http://foaf.org/>"
                + "construct { ?x ?p ?b ; rdfs:seeAlso ?b }"
                + "where {"
                + "{select * ((bnode()) as ?x) where {"
                + ""
                + "?a ?p ?b "
                //+ "filter(kg:isSkolem(?a))"
                + "}}"
                + "}";


        String q3 = "prefix foaf: <http://foaf.org/>"
                + "construct { [ ?p ?b ; rdfs:seeAlso ?b ] }"
                + "where {"
                + ""
                + "?a ?p ?b "
                // + "filter(kg:isSkolem(?a))"

                + "}";



        String q5 = "prefix foaf: <http://foaf.org/>"
                + "construct { ?a ?p ?b ; rdfs:seeAlso ?b }"
                + "where {"
                + "?a ?p ?b "
                + "}";

        String q6 = "prefix foaf: <http://foaf.org/>"
                + "select * "
                + "where {"
                + "{?a ?p ?b} union {?b ?p ?a} "
                + "}";


        String init = "prefix foaf: <http://foaf.org/>"
                + "insert data { "
                + "[foaf:name 'John' ; foaf:knows [ foaf:name 'James' ] ] "
                + ""
                + "}";


        try {
            exec.query(init);
            Mappings map = exec.query(q2);
            //System.out.println(map);
            System.out.println(exec.getGraph(map).display());

            map = exec.query(q2);
            //System.out.println(map);
            System.out.println(exec.getGraph(map).display());




//            exec.skolem(map);
//            
////            Node qn = map.getQueryNode("?b");
////            Node n = map.getNode("?b");
////            System.out.println(n);
////            
////            Mapping m = Mapping.create(qn, n);
////            
////            map = exec.query(q6, m);
//            System.out.println(map);
//            System.out.println(map.size());


//            map = exec.query(q6);
//            System.out.println(map);
//            System.out.println(exec.skolem(map));
//            
//
//            System.out.println(map.size());
//            ResultFormat f = ResultFormat.create(map);
//            System.out.println(f);
//            
//            map = exec.query(q5);
//            System.out.println(map);
//            System.out.println(map.size());
//             f = ResultFormat.create(map);
//            System.out.println(f);


//            CSVFormat csv = CSVFormat.create(map);
//            System.out.println(csv);
//            TSVFormat tsv = TSVFormat.create(map);
//            System.out.println(tsv);
        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }



    }

    /**
     *
     *
     * hash: 766488133 hash: 211543962 hash: 1839367010 hash: 169456094
     */
    public void testValidate() {

        Graph g = Graph.create(true);
        Load ld = Load.create(g);

        try {
            ld.loadWE(root + "comma/model.rdf");
            ld.loadWE(root + "comma/comma.rdfs");

        } catch (LoadException e1) {
            e1.printStackTrace();
        }

        String q =
                "template  { kg:pprintWith(<" + data + "validate/template> ) }"
                + "where {  }";

        QueryLoad qr = QueryLoad.create();
        String qq = qr.read(data + "validate/instance.rq");

        QueryProcess exec = QueryProcess.create(g);

        try {
            ASTQuery.setTemplateAggregate("agg_and");
            Mappings map = exec.query(q);
            Node node = map.getTemplateResult();

            System.out.println("** Test: \n" + node.getLabel());

            map = exec.query(qq);
            System.out.println(map);
            System.out.println(map.size());

            assertEquals("result", "false", node.getLabel());

        } catch (EngineException e) {
            e.printStackTrace();
        }


    }

    public void testVR() {
        ValueResolverImpl vr = new ValueResolverImpl();
        HashMap h = new HashMap();
        Date d1 = new Date();
        String value = "http://www.inria.fr/schema/name/";
        String key = vr.getKey(value);
        System.out.println(key);
        System.out.println(key.length());
        System.out.println(value.length());

        h.put(key, value);
        h.put(value, value);
        for (int i = 0; i < 1000000; i++) {
            key = vr.getKey(value);
            // vr.hashByte(value);
            //h.put(key, value);
            h.get(key);
            // System.out.println(str);
        }
        Date d2 = new Date();
        System.out.println((d2.getTime() - d1.getTime()) / 1000.0);

    }

    public void testValues() {
        String init =
                "insert data {"
                + "<John> foaf:age '21'^^xsd:double "
                + "<Jack> foaf:age 21.0 "
                + "}";

        String query =
                "select  * where {"
                + "?x foaf:age ?a "
                + "?y foaf:age ?b "
                + "filter(?a = ?b && ?x != ?y) "
                + "}";

        String query2 =
                "select  * where {"
                + "{select (21.0 as ?a) where {}}"
                + "?x foaf:age ?a "
                + "}";

        String query3 =
                "select  * where {"
                + "?x foaf:age ?a "
                + "}"
                + "values ?a { 21 21.0}";

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.definePrefix("foaf", "http://xmlns.com/foaf/0.1/");

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            assertEquals("Result", 2, map.size());

            map = exec.query(query2);
            System.out.println(map);
            assertEquals("Result", 1, map.size());

            map = exec.query(query3);
            System.out.println(map);
            assertEquals("Result", 1, map.size());

        } catch (EngineException e) {
            assertEquals("Result", 2, e);
        }

    }

    public void testValues2() {
        String init =
                "insert data {"
                + "<Jim>  foaf:age 21.0 "
                + "<John> foaf:age '21'^^xsd:double "
                + "<Jack> foaf:age 22 "
                + "}";

        String query =
                "select  * where {"
                + "?x foaf:age ?a "
                + "?y foaf:age ?b "
                + "filter(?a = ?b && ?x != ?y) "
                + "}";

        String query2 =
                "select debug * where {"
                // + "{select (23.0 as ?a) where {}}"
                + "?x foaf:age ?a "
                + "}"
                + "group by ?a "
                + "having (min(?x) = <Jim>)"
                + "limit 1";

        String query3 =
                "select  * where {"
                + "?x foaf:age ?a "
                + "}"
                + "values ?a { 21 21.0}";

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.definePrefix("foaf", "http://xmlns.com/foaf/0.1/");

        try {
            exec.query(init);
//            Mappings map = exec.query(query);
//            System.out.println(map);
//            assertEquals("Result", 2, map.size());

            Mappings map = exec.query(query2);
            System.out.println(map);
            System.out.println(map.getQuery().getAST());

            assertEquals("Result", 1, map.size());

//            map = exec.query(query3);
//            System.out.println(map);
//            assertEquals("Result", 1, map.size());

        } catch (EngineException e) {
            assertEquals("Result", 2, e);
        }

    }

    public void testtest() {
        // System.out.println(graph.getValueResolver().getCount());
        System.out.println(graph.getValueResolver().size());
    }

    public void TestOnto() {
        Graph g = Graph.create(true);
        QueryProcess exec = QueryProcess.create(g);

        String init =
                "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "insert data {"
                + "c:Human rdfs:subClassOf c:Animal "
                + "c:Man   rdfs:subClassOf c:Human "
                + "c:Woman rdfs:subClassOf c:Human "
                + ""
                + "<John> a c:Man "
                + "<Tigrou> a c:Cat "
                + "<Mary> a c:Woman "
                + "<James> a c:Human "
                + "}";

        String query =
                "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select * where {"
                + "?x a c:Human, ?t"
                + "}";
        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println(map.size());

            System.out.println("Size: " + graph.getValueResolver().size());
            //System.out.println("Size: " + graph.getValueResolver().getCount());

        } catch (EngineException ex) {
            Logger.getLogger(TestQuery1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void testValues3() {
        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<John>  foaf:name 'http://www.inria.fr' "
                + "<Jack>  foaf:name 'http://www.inria.fr' "
                + "<James> foaf:name <http://www.inria.fr> "
                + "<Jim>   foaf:name <http://www.inria.fr> "
                + "<John>  foaf:name 'http://www.inria.fr'@en "
                + "<Jack>  foaf:name 'http://www.inria.fr'@en "
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select  * where {"
                + "?x foaf:name ?a "
                + "?y foaf:name ?a "
                + "filter(?x < ?y) "
                + "}";

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            assertEquals("Result", 3, map.size());


        } catch (EngineException e) {
            assertEquals("Result", 2, e);
        }

    }

    public void testIDX() {

        Graph g = Graph.create();
        Load ld = Load.create(g);

        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<John> foaf:age '1'^^xsd:integer "
                + "<Jack> foaf:age '1'^^xsd:long "
                + "}";

        String q =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + "?x foaf:age ?a "
                + "?y foaf:age ?a"
                + "}";

        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            System.out.println(g.display());
            Mappings map = exec.query(q);
            System.out.println(map);

        } catch (EngineException e) {
            e.printStackTrace();
        }


    }

    public void test18() {
        String query = "select ?a ?b where {"
                + "c:Person rdfs:subClassOf+ :: $path ?c "
                + "graph $path {?a ?p ?b}"
                + "}"
                + "order by ?a ?b ";

        QueryProcess exec = QueryProcess.create(graph);

        try {
            Mappings map = exec.query(query);
            System.out.println(map);
            assertEquals("Result", 28, map.size());
        } catch (EngineException e) {
            assertEquals("Result", 28, e);
        }

    }

    //@Test
    public void testMath() {

        Graph g = Graph.create();
        Load ld = Load.create(g);

        try {
            ld.loadWE(root + "math/data");
        } catch (LoadException e1) {
            e1.printStackTrace();
        }

        String q =
                "prefix m: <http://ns.inria.fr/2013/math#>"
                + "template  { kg:pprintWith(?p) }"
                + "where { ?p a m:PrettyPrinter }";

        q =
                "prefix m: <http://ns.inria.fr/2013/math#>"
                + "template  { kg:pprintWith('/home/corby/AData/math/latextemplate', ?e) }"
                + "where     { ?e a m:Integral }";

        Option.isOptional = false;
        QueryProcess exec = QueryProcess.create(g);

        try {
            Mappings map = exec.query(q);
            Node node = map.getTemplateResult();

            System.out.println(node.getLabel());

            assertEquals("result", true, node.getLabel().length() > 10);

        } catch (EngineException e) {
            e.printStackTrace();
        }


    }

    public void testDB() {
        QueryLoad ql = QueryLoad.create();
        String q = ql.read(data + "dbpedia/query/text.txt");
        System.out.println(q);
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        try {
            Mappings map = exec.query(q);
            System.out.println(map);
            TripleFormat f = TripleFormat.create(map);
            System.out.println(f);
            System.out.println(map.size());
        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void test2graph() throws LoadException {
        Graph go = Graph.create(true);
        Load load = Load.create(go);
        load.loadWE("http://www-sop.inria.fr/edelweiss/software/corese/v2_4_0/data/human_2007_09_11.rdfs");

        Graph g = Graph.create(true);
        Load ld = Load.create(g);
        ld.loadWE("http://www-sop.inria.fr/edelweiss/software/corese/v2_4_0/data/human_2007_09_11.rdf");

        QueryProcess exec = QueryProcess.create(go);
        exec.add(g);

        String q =
                "PREFIX h: <http://www.inria.fr/2007/09/11/humans.rdfs#>"
                + "SELECT *   WHERE{"
                + " graph ?g {?x rdf:type h:Male}"
                + "}";

        try {
            Mappings map = exec.query(q);
            System.out.println(map);
            System.out.println(map.size());
        } catch (EngineException e) {
            e.printStackTrace();
        }
    }

    public void testBug() throws LoadException {
        Graph g = Graph.create(true);
        QueryProcess exec = QueryProcess.create(g);
        Load load = Load.create(g);
        load.loadWE(data + "kgraph/rdf.rdf", RDF.RDF);
        load.loadWE(data + "kgraph/rdfs.rdf", RDFS.RDFS);
        load.loadWE(data + "comma/comma.rdfs");
        load.loadWE(data + "comma/commatest.rdfs");
        load.loadWE(data + "comma/model.rdf");
        load.loadWE(data + "comma/testrdf.rdf");
        load.loadWE(data + "comma/data");
        load.loadWE(data + "comma/data2");

        String q = "select * where {?x ?p ?y optional{?y rdf:type ?class} filter (! bound(?class) && ! isLiteral(?y))}";

        try {
            Mappings map = exec.query(q);
            System.out.println(map);
        } catch (EngineException e) {
            e.printStackTrace();
        }


    }

    public void testPerf() throws LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.setLimit(2000000);
        Date d1 = new Date();

//		ld.load(root + "alban/drugbank_dump.ttl");
//		ld.load(root + "alban/article_categories_en.ttl");

        ld.loadWE(root + "alban2/");


        Date d2 = new Date();
        System.out.println("Time : " + (d2.getTime() - d1.getTime()) / 1000.0);


        System.out.println(g.size());
        System.out.println(g);
        System.out.println(g.getIndex());

        String sparqlQuery = "SELECT ?predicate ?object WHERE {"
                + "{    <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> ?predicate ?object . }"
                + " UNION    "
                + "{    <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> <http://www.w3.org/2002/07/owl#sameAs> ?caff ."
                + "     ?caff ?predicate ?object . } "
                + "}";

        String edgeSelect =
                "SELECT * WHERE { "
                + //"?x rdfs:label ?object . } limit 1";
                "<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> ?predicate ?object . }";
        // "<http://dbpedia.org/resource/Category:%22Weird_Al%22_Yankovic_albums> ?predicate ?object . }";

        String distinct =
                "SELECT distinct ?predicate  WHERE { "
                + "<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> ?predicate ?object . }";


        String edgeConstruct =
                "construct  { <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> <http://www.inria.fr/acacia/corese#Property> ?object } "
                + "where { <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> <http://www.inria.fr/acacia/corese#Property> ?object .}";
        String edgeConstruct2 =
                "construct  { <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> ?predicate ?object } "
                + "where { <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> ?predicate ?object .}";

        try {
            d1 = new Date();
            Mappings map1 = exec.query(distinct);

            d2 = new Date();

            System.out.println(map1);
            System.out.println("Time : " + (d2.getTime() - d1.getTime()) / 1000.0);

            d1 = new Date();
            StopWatch sw = new StopWatch();
            Mappings map2 = null;
            for (int i = 0; i < 10; i++) {
                System.out.println(i);
                sw.reset();
                sw.start();
                map2 = exec.query(edgeSelect);
                System.out.println(sw.getTime());
            }
            d2 = new Date();

            System.out.println(map2);
            System.out.println("Time : " + (d2.getTime() - d1.getTime()) / 1000.0);



            System.out.println("nb prop: " + map1.size());







        } catch (EngineException e) {
            e.printStackTrace();
        }

        TemplateFormat tf = TemplateFormat.create(g, Transformer.PPRINTER);
        //System.out.println(tf);

    }

    public void testPPAgg() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init = "prefix ex: <http://www.example.org/>"
                + "insert data {"
                + "ex:Jack a ex:Man ; ex:name 'Jack' "
                + "ex:Jim a ex:Man  "
                + "ex:John a ex:Man ; ex:name 'John' "
                + "}";

        String t1 = "prefix ex: <http://www.example.org/>"
                + "template {group_concat(if (bound(?n), ?n, '') ; separator = ';')} "
                + "where {?in a ex:Man optional {?in ex:name ?n}}";

        String q = "prefix ex: <http://www.example.org/>"
                + "select (group_concat(?n ; separator = ';') as ?out) "
                + "where {?in a ex:Man optional {?in ex:name ?n}}";


        try {
            exec.query(init);
            Transformer f = Transformer.create(g);
            f.defTemplate(t1);
            f.trace();

            System.out.println(f);


            Mappings map = exec.query(q);
            System.out.println(map);
        } catch (EngineException e) {
            e.printStackTrace();
        }


    }

    public void testPPrint2() throws LoadException {
        Graph g = Graph.create(true);
        Load ld = Load.create(g);
        //g.init();

        ld.loadWE(root + "pprint/data");
        //ld.load(cos + "ontology/carto.owl");

        NSManager nsm = NSManager.create();
        nsm.definePrefix("ex", "http://www.example.org/");
        nsm.definePrefix("ast", "http://www.inria.fr/2012/ast#");

        Date d1 = new Date();

        TemplateFormat tf = TemplateFormat.create(g);
        tf.setPPrinter(root + "pprint/asttemplate");
        tf.setNSM(nsm);
        String str = tf.toString();

        Date d2 = new Date();

        int length = str.length();

        str = nsm.toString() + str;

        System.out.println(str);
        System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / 1000.0);
        System.out.println(str.length());



    }

    public void testPPrint() throws LoadException {
        Graph g = Graph.create(true);
        Load ld = Load.create(g);

        ld.loadWE(root + "pprint/data/");

        NSManager nsm = NSManager.create();
        nsm.definePrefix("ex", "http://www.example.org/");
        nsm.definePrefix("ast", "http://www.inria.fr/2012/ast#");

        Date d1 = new Date();

        TemplateFormat tf = TemplateFormat.create(g);
        tf.setPPrinter(root + "pprint/asttemplate");
        tf.setNSM(nsm);
        String str = tf.toString();

        Date d2 = new Date();
        System.out.println(str);

        assertEquals("Results", 3058, str.length());
        System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / 1000.0);
    }

    public void testBib() throws LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        Load ld = Load.create(g);
        ld.loadWE(root + "bib/data/glc-all-utf.rdf");

        QueryLoad ql = QueryLoad.create();

        String q = ql.read(root + "bib/query/title.rq");





        try {
            Mappings map = exec.query(q);
            IDatatype dt = (IDatatype) map.getValue("?tt");

            System.out.println(dt.getLabel());

//			try {
//				FileWriter f = new FileWriter(root + "bib/corpus.txt");
//				f.write(dt.getLabel());
//				f.flush();
//				f.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

            //System.out.println(map);
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testPPCount() {
        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);
        String query = "select * where {"
                + "service <test> {select (count(*) as ?c) where {}}"
                + "}";

        try {
            Query q = exec.compile(query);
            System.out.println(q.getAST());
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testAlban2() throws LoadException {

        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph);


        Load ld = Load.create(graph);

        QueryLoad ql = QueryLoad.create();
        String q = ql.read(root + "test/inference-atest.rq");


        ld.loadWE(root + "test/FIELD2.rdf");

        try {
            Mappings map = exec.query(q);
            System.out.println(map);
            System.out.println(map.size());
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    public void testExam() throws LoadException {

        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "insert data {"
                + "graph <g1> { <a> <p> <b>, <c> "
                + "<b> <p> <a> "
                + "<c> <p> <a>}"
                + "}";

        String query = "ask {"
                + "filter (not exists {"
                + " {?x ?p ?y "
                + " filter not exists { ?y ?p ?x}"
                + "}"
                + "})"
                + "}";

        query = "select * where {?x ?p ?y}";

        Load ld = Load.create(graph);
        ld.setRenameBlankNode(false);

        ld.loadWE(root + "test/luc.ttl");


        try {
            //exec.query(init);

            Mappings map = exec.query(query);

            System.out.println(map.size());
            System.out.println(map);

//			Node n = graph.getResource("a");
//			Node p = graph.getResource("p");
//			
//			graph.getEdges(n);
//			
//			for (Entity ent : graph.getEdges(p, n, 0)){
//				System.out.println(ent);
//			}
//			
//			System.out.println(graph.getEdge(p, n, 0));


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testReverseList() {

        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph);

        String init = "insert data {"
                + "[rdf:value (1 2 3)]"
                + "}";

        String query =
                "delete {"
                + "?y rdf:rest ?x ?x rdf:first ?e	"
                + "?xx rdf:first ?ee "
                + "}"
                + "insert {graph <g1> {"
                + "?x rdf:rest ?y ; rdf:first ?e ."
                + "?xx rdf:first ?ee ; rdf:rest rdf:nil "
                + "}}"
                + "where {"
                + "{?y rdf:rest ?x ?x rdf:first ?e}"
                + "union"
                + "{?xx rdf:first ?ee minus {?yy rdf:rest ?xx}}"
                + "}";

        query = "select ?e where {"
                + "rdf:nil (^rdf:rest)*/rdf:first ?e"
                + "}";



        try {
            exec.query(init);

            Mappings map = exec.query(query);

            System.out.println(map);
            System.out.println(map.size());



            TripleFormat f = TripleFormat.create(graph);
            f.with(Entailment.ENTAIL);
            System.out.println(f);

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    public void testGC() {

        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix ex: <http://example.org/> "
                + "insert data {"
                + "[ex:name 'John' , 'Jim']"
                + "[ex:name 'John' , 'Jim']"
                + "}"
                + "";

        String query = "prefix ex: <http://example.org/> "
                + "select (group_concat(distinct self(?n1), ?n2 ;  separator='; ') as ?t) where {"
                + "?x ex:name ?n1 "
                + "?y ex:name ?n2 "
                + "filter(?x != ?y)"
                + ""
                + "}";

        try {
            exec.query(init);

            Mappings map = exec.query(query);

            System.out.println(map);

            IDatatype dt = (IDatatype) map.getValue("?t");
            System.out.println(dt.getLabel().length());
            assertEquals("Results", 42, dt.getLabel().length());

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testQueryGraph() {

        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix : <http://example.org/> "
                + ""
                + "insert data {"
                + ":a :p :b, :c ."
                + ":b :q :d "
                + ":c :q :d "
                + ":d :p :e "
                + ":e :q :f "
                + ""
                + "} ";

        String cons =
                "prefix : <http://example.org/> "
                + ""
                + "construct {?x :p []}"
                + "where {?x :p ?y}";

        String init2 =
                "prefix : <http://example.org/> "
                + ""
                + "insert data {"
                + ":a :p [] ."
                + "}";


        try {
            // create a graph
            exec.query(init);

            // create a copy where triple objects (values) are Blank Nodes (aka Variables)
            // consider the copy as a Query Graph and execute it
            Mappings map = exec.queryGraph(cons);

            assertEquals("Results", 4, map.size());

            Graph g2 = Graph.create();
            QueryProcess exec2 = QueryProcess.create(g2);
            exec2.query(init2);

            QueryGraph qg = QueryGraph.create(g2);
            QGV vis = new QGV();
            qg.setVisitor(vis);
            //qg.setConstruct(true);
            map = exec.query(qg);

            //Graph res = exec.getGraph(map);
            System.out.println(map.toString(true));
            System.out.println(map.size());

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    class QGV implements QueryGraphVisitor {

        public Graph visit(Graph g) {
            return g;
        }

        public ASTQuery visit(ASTQuery ast) {
            return ast;
        }

        public Entity visit(Entity ent) {
            return ent;
        }

        public Query visit(Query q) {
            //q.setLimit(1);
            return q;
        }
    }

    public void test47() {

        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix i: <http://www.inria.fr/test/> "
                + ""
                + "insert data {"
                + "<doc> i:contain "
                + "'<doc>"
                + "<person><name>John</name><lname>K</lname></person>"
                + "<person><name>James</name><lname>C</lname></person>"
                + "</doc>'^^rdf:XMLLiteral   "
                + "}";

        String query = ""
                + "prefix i: <http://www.inria.fr/test/> "
                + "select (concat(?n, '.', ?ll) as ?name) where {"
                + "?x i:contain ?xml "
                + "{select  (xpath(?xml, '/doc/person') as ?p) where {}}"
                + "{select  (xpath(?p, 'name/text()')  as ?n)  where {}}"
                + "{select  (xpath(?p, 'lname/text()') as ?l)  (concat(?l, ' 123') as ?ll ) where {}}"
                + "}";


        try {
            Mappings map = exec.query(init);
            map = exec.query(query);
            System.out.println(map);
            assertEquals("Result", 2, map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testCompile() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String query =
                "select * where {"
                + "graph ?g {?x ?p ?y "
                + "{select * where {"
                + "?a (rdf:type@[a rdfs:Resource]) ?b  "
                + "{values ?a {<John>}}"
                + "}"
                + "order by ?a "
                + "group by ?b "
                + "having (?a > ?b) "
                + "}"
                + "?a (rdf:type@[a rdfs:Resource]) ?b"
                + ""
                + "}"
                + "}";

        try {
            Mappings map = exec.query(query);
            Query q = map.getQuery();

            assertEquals("Result", 16, q.nbNodes());

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testIndex() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        //exec.addEventListener(EvalListener.create());

        String init =
                "prefix ex: <http://www.example.org/>"
                + "insert data {"
                + "ex:a ex:p1 '2'^^xsd:integer "
                + "ex:a ex:p1 '2'^^xsd:long "
                + //				"ex:a ex:p2 '2'^^xsd:integer "  +
                //				"ex:a ex:p2 '2'^^xsd:long "  +
                //				
                //				"ex:a ex:p3 '2'^^xsd:float " +
                //				"ex:a ex:p3 '2'^^xsd:double " +
                //				
                //				
                //				"ex:a ex:p 'toto' " +
                //				"ex:a ex:p 'toto'^^xsd:string " +
                "}"
                + ""
                + "";

        String q =
                "prefix ex: <http://www.example.org/>"
                + " select * where {"
                + "?x ex:p1* ?y "
                + //"filter(?y = 2)" +
                "}";

//		q = 
//				"prefix ex: <http://www.example.org/>" +				
//				" select * where {" +
//				"?x ex:p ?y " +
//				"}";

        try {
            exec.query(init);

            Mappings map = exec.query(q);

            System.out.println(g.display());



            System.out.println(map);
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testPP() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init =
                "@prefix ast: <http://www.inria.fr/2012/ast#> "
                + "insert data {"
                + "[a ast:SelectQuery ; "
                + "ast:select ( '*' "
                + "[ast:var [a ast:Var ; ast:name '?xx'] ; "
                + " ast:exp [ast:fun 'self' ; ast:body ( [a ast:Var ; ast:name '?x'])]"
                + "]"
                + ") ;"
                + "ast:where ("
                + "[ast:subject  [a ast:Var ; ast:name '?x'] ;"
                + " ast:property [a ast:Var ; ast:name '?p'] ;"
                + " ast:object   [a ast:Var ; ast:name '?y']]"
                + ")"
                + "]"
                + "}";

        String q = "prefix ast: <http://www.inria.fr/2012/ast#> "
                + "select * where {?in ast:select ?s}";

        try {
            exec.query(init);

            Transformer pp = Transformer.create(g);
            //pp.setDebug(true);
            IDatatype dt = pp.process();
            System.out.println(dt.getLabel());

            Mappings map = exec.query(dt.getLabel());
            System.out.println(map);

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testFF() {

        System.out.println(String.format("%g", new Double(1.23456789e6).doubleValue()));
        System.out.println(String.format("%f", new Double(1.23456789e6).doubleValue()));
        System.out.println(String.format("%e", new Double(1.23456789e6).doubleValue()));

        System.out.println(String.format("%g", new Double(1.54e6).doubleValue()));
        System.out.println(String.format("%g", new Double(1e6).doubleValue()));
        System.out.println(String.format("%.1g", new Double(1e6).doubleValue()));
        System.out.println(String.format("%f", new Double(1e6).doubleValue()));
        System.out.println(new Double(1e6).doubleValue());

    }

    public void testAST() throws LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g);

        ld.loadWE(root + "pprint/pprint.ttl");

        QueryLoad ql = QueryLoad.create();
        String q = ql.read(root + "pprint/pprint.rq");
//		String q1 = ql.read(root + "test/pprint1.rq");
//		String q2 = ql.read(root + "test/pprint2.rq");

        //System.out.println(q);

        //System.out.println(g.display());

        QueryProcess exec = QueryProcess.create(g);

        try {

            Mappings map1 = exec.query(q + "values ?pp {ast:construct}");
            Mappings map2 = exec.query(q + "values ?pp {ast:where}");

            IDatatype cst = (IDatatype) map1.getValue("?res");
            IDatatype whr = (IDatatype) map2.getValue("?res");

            System.out.println("construct {" + cst.getLabel() + "}");
            System.out.println("where {" + whr.getLabel() + "}");



        } catch (EngineException e) {
            e.printStackTrace();
        }


    }

    public void testJoin2xcgf() {
        Graph g = Graph.create();
        Load ld = Load.create(g);

        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data   {"
                + "graph <g1> {"
                + "<John> foaf:name 'John' "
                + "<http://fr.dbpedia.org/resource/Auguste>  foaf:knows <James>"
                + "<http://fr.dbpedia.org/resource/Augustus> foaf:knows <Jim>"
                + "<http://fr.dbpedia.org/resource/Augustin> foaf:knows <Jim>"
                + "<http://fr.dbpedia.org/resource/Augusgus> foaf:knows <Jim>"
                + "}"
                + "graph <g1> {"
                + "<Jim> foaf:knows <James>"
                + "<Jim> foaf:name 'Jim' "
                + "}"
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select debug * where {"
                + "service <http://fr.dbpedia.org/sparql> {"
                + "select * where {"
                + "?x rdfs:label ?n "
                + "filter(regex(?n, '^Augustin'))"
                + "}  "
                + "limit 20"
                + ""
                + "}"
                // + "?x foaf:knows ?y "
                + "service <http://fr.dbpedia.org/sparql> {"
                + "select * where {"
                + "?y rdfs:label ?n "
                + "filter(regex(?n, '^August'))"
                + "}  "
                + "limit 20"
                + "}"
                + "}"
                + "pragma {kg:kgram kg:detail true}";

        String qq = "select * where {"
                + "service <http://fr.dbpedia.org/sparql> {"
                + "select * where {"
                + "?y rdfs:label ?n "
                + "filter(regex(?n, '^August'))"
                //                + "filter ((((((((((((((((?n = 'Augustin'@fr) || "
                //                + "(?n = 'Augustins'@fr)) || "
                //                + "(?n = \"Augustins de l'Assomption\"@fr)) || "
                //                + "(?n = 'Augustin français'@fr)) || "
                //                + "(?n = 'Augustin Bonrepaux'@fr)) || "
                //                + "(?n = 'Augustin Berque'@fr)) || "
                //                + "(?n = 'Augustin Matata Ponyo'@fr)) || "
                //                + "(?n = 'Augustin Challamel'@fr)) || "
                //                + "(?n = 'Augustin Bernard'@fr)) || "
                //                + "(?n = 'Augustin d’Hippone'@fr)) || "
                //                + "(?n = 'Augustinus'@fr)) || "
                //               // + "(?n = 'Augustin d\'Hippone'@fr)) || "
                //                + "(?n = 'Augustin Fabre'@fr)) || (?n = 'Augustin, roi du kung-fu'@fr)) || (?n = 'Augustin Louis Cauchy'@fr)) || (?n = 'Augustin Fresnel'@fr)) || (?n = 'Augustin Chauvet'@fr)) " 
                + "}  "
                //+ "limit 20"
                + "}}";

        QueryProcess exec = QueryProcess.create(g);
        // exec.setSlice(30);
        //exec.setDebug(true);

        try {
            exec.query(init);

            Mappings map = exec.query(query);
            System.out.println(map);

            assertEquals("Result", 26, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }



    }

    public void testUpdateSyntax() {
        Graph g = Graph.create();
        String init =
                "prefix ex: <http://www.example.org/test/> "
                + "insert data {"
                + "ex:test1 ex:name 'John'"
                + "} ;"
                + "prefix ex: <http://www.example.org/> "
                + "insert data {"
                + "ex:test2 ex:name 'Jack'"
                + "} ;"
                + "prefix ex: <http://www.example.org/test/> "
                + "create graph  ex: ; "
                + "delete  {ex:test2 ex:name 'Jack'} "
                + "insert {?x a ex:Person} where {?x ex:name ?n}"
                + "";

        String q = "select * where {?x ?p ?y}";

        QueryProcess exec = QueryProcess.create(g);
        exec.setDebug(true);
        try {
            Mappings map = exec.query(init);
            ASTQuery ast = exec.getAST(map);

            System.out.println(ast);
            System.out.println(map);
            System.out.println(map.size());

            map = exec.query(q);
            System.out.println(map);



        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void test15() throws LoadException {

        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.loadWE(data + "comma/comma.rdfs");

        String q1 =
                "select  *  where {"
                + "?x rdfs:subClassOf ?sup"
                + "}";

        String q2 =
                "select (kg:similarity() as ?sim) (max(kg:depth(?x)) as ?max)  where {"
                + "?x rdfs:subClassOf ?sup"
                + "}";
        try {

            QueryProcess exec = QueryProcess.create(g);
//			System.out.println("q1");
            exec.query(q1);
//			System.out.println("q2");
//			System.out.println(g.getClassDistance());
//			Mappings map = exec.query(q2);
//			Node n = map.getNode("?max");
//			System.out.println(n);
//			System.out.println(map);
//			System.out.println(map.size());

//			IDatatype dt = (IDatatype) n.getValue();
//			assertEquals("Result", 13, dt.intValue()); 
            Node node = g.getResource("http://www.inria.fr/acacia/comma#Person");
            Node type = g.getPropertyNode(RDF.TYPE);
            for (Entity e : g.getEdges(type, node, 0)) {
                System.out.println(e.getNode(1));
            }

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    /**
     * Rule engine with QueryExec on two graphs
     */
    public void test6() throws LoadException {
        QuerySolver.definePrefix("c", "http://www.inria.fr/acacia/comma#");

        Graph g1 = Graph.create(true);
        Graph g2 = Graph.create(true);

        Load load1 = Load.create(g1);
        Load load2 = Load.create(g2);

        load1.loadWE(data + "engine/ontology/test.rdfs");
        load2.loadWE(data + "engine/data/test.rdf");

        QueryProcess exec = QueryProcess.create(g1);
        exec.add(g2);
        RuleEngine re = RuleEngine.create(g2, exec);
        //re.setOptimize(true);

        load2.setEngine(re);

        try {
            load2.loadWE(data + "engine/rule/test2.brul");
            load2.load(new FileInputStream(data + "engine/rule/meta.brul"), "meta.brul");
        } catch (LoadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }






        String query =
                "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select     * where {"
                + "?x c:hasGrandParent c:Pierre "
                + "}";





        re.process();

        try {
            Mappings map = exec.query(query);
            assertEquals("Result", 4, map.size());
            System.out.println(map);
        } catch (EngineException e) {
            assertEquals("Result", 4, e);
        }

    }

    public static void main22(String[] args) {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);


        Load ld = Load.create(g);

        //ld.load(data + "comma/comma.rdfs");
//		ld.load(data + "comma/model.rdf");
//		ld.load(data + "comma/data");


        String rule =
                "rule construct {?x rdfs:subClassOf ?y }"
                + "where {?x rdfs:subClassOf ?y }";

        String q =
                "select * where {"
                + " graph ?g {?x ?p ?y}"
                + "}";

        String q2 = ""
                + "insert data  {"
                + "graph <g1> { _:b rdfs:label 'John'}"
                + "graph <g2> { _:b rdfs:label 'Jack'}} ;"
                + "";

        String q3 =
                "INSERT  { GRAPH :g1  { _:b :p :o } } WHERE {};"
                + "INSERT  { GRAPH :g2  { _:b :p :o } } WHERE {}";

        try {
            exec.query(rule);
            exec.query(q3);
            Mappings map = exec.query(q);

            System.out.println(map);
            System.out.println(ResultFormat.create(map));

            System.out.println(map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testWF2() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init =
                "prefix c: <http://example.org/>"
                + "insert data {"
                + "<John> c:name 'John' ; rdf:value (1 2 3)"
                + "c:name rdfs:domain c:Person "
                + "c:Person rdfs:subClassOf c:Human "
                + "}";


        String query =
                "prefix c: <http://example.org/>"
                + "select  *  where {"
                + "graph ?g {?x rdf:type ?c}"
                + "}"
                + "pragma{kg:kgram rdfs:entailment true}";


        String query2 = "drop graph kg:entailment "
                + "pragma{kg:kgram rdfs:entailment false}";

        String query3 =
                "prefix c: <http://example.org/>"
                + "select  *  where {"
                + "graph ?g {?x rdf:type ?c}"
                + "}";

        try {

            g.getWorkflow().setDebug(true);

            exec.query(init);
            Mappings map = exec.query(query);


            System.out.println(map);
            System.out.println(map.size());

            System.out.println("query2");

            exec.query(query2);

            System.out.println("query3");

            map = exec.query(query3);

            System.out.println(map);
            System.out.println(map.size());

            //g.getEntailment().setActivate(true);
            //g.process();

            map = exec.query(query);

            System.out.println(map);
            System.out.println(map.size());


        } catch (EngineException e) {
            e.printStackTrace();
        }



    }

    public void testWFQE() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        QueryEngine qe = QueryEngine.create(g);
        g.addEngine(qe);

        String init =
                "prefix c: <http://example.org/>"
                + "insert data {"
                + "[ c:hasParent [] ]"
                + "}";


        String update =
                "prefix c: <http://example.org/>"
                + "insert {?y c:hasChild ?x}"
                + "where { ?x c:hasParent ?y}";

        qe.addQuery(update);
//		qe.setDebug(true);
//		g.getWorkflow().setDebug(true);

        String query = "select * where {?x ?p ?y}";

        try {
            //System.out.println("init");
            exec.query(init);
            //System.out.println("query");

            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println(map.size());

            assertEquals("Result", 2, map.size());


        } catch (EngineException e) {
            assertEquals("Result", true, false);
        }



    }

    public void testWF() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        //g.setClearEntailment(true);
        String init =
                "prefix c: <http://example.org/>"
                + "insert data {"
                + "<John> c:name 'John' ; rdf:value (1 2 3)"
                + "c:name rdfs:domain c:Person "
                + "c:Person rdfs:subClassOf c:Human "
                + "}";

        String drop =
                "prefix c: <http://example.org/>"
                + "delete data {"
                + "c:name rdfs:domain c:Person "
                + "c:Person rdfs:subClassOf c:Human}"
                + "pragma {kg:kgram kg:detail true}";

        String query =
                "prefix c: <http://example.org/>"
                + "select  *  where {"
                + "?x rdf:type c:Human ; c:name ?n ;"
                + "rdf:value @(1 2)"
                + "}";

        query =
                "prefix c: <http://example.org/>"
                + "select  *  where {"
                + "graph ?g {?x rdf:type ?c}"
                + "}";

        String rule =
                "prefix c: <http://example.org/>"
                + "construct {?x a c:Human}"
                + "where {?x c:name ?n}";

        String upd =
                "prefix c: <http://example.org/>"
                + "insert data {<Jack> a c:Human}";

        RuleEngine re = RuleEngine.create(g);
        g.addEngine(re);

        QueryEngine qe = QueryEngine.create(g);
        try {
            qe.defQuery(upd);
        } catch (EngineException e2) {
            e2.printStackTrace();
        }
        g.addEngine(qe);


        g.getWorkflow().setDebug(true);
        try {
            re.defRule(rule);
        } catch (EngineException e1) {
            e1.printStackTrace();
        }

        try {
            exec.query(init);
            Mappings m = exec.query(drop);
            System.out.println(XMLFormat.create(m));
            g.remove();
            g.getWorkflow().setActivate(false);
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println(map.size());

            g.getWorkflow().setActivate(true);
            g.process();
            map = exec.query(query);
            System.out.println(map);
            System.out.println(map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    /**
     * 26.263 25.246
     *
     *
     */
    public void test() throws LoadException {
        Date d1 = new Date();
        for (int i = 0; i < 10; i++) {
            testRule();
        }
        Date d2 = new Date();
        System.out.println((d2.getTime() - d1.getTime()) / 1000.0);

    }

    public void testRule() throws LoadException {

        Graph g = Graph.create(true);
        //g.addListener(GraphListenerImpl.create(10000));
        QueryProcess exec = QueryProcess.create(g);



        Load ld = Load.create(g);

        ld.loadWE(data + "comma/comma.rdfs");
        ld.loadWE(data + "comma/model.rdf");
        ld.loadWE(data + "comma/data");
        //ld.load(data + "comma/data2");

        //ld.load(data + "comma/test.rul");
        RuleEngine re = ld.getRuleEngine();
        ld.getQueryEngine();

        RuleEngine re2 = RuleEngine.create(g);
        ld.setEngine(re2);

        Entailment ent = Entailment.create(g);

        String loadrule = "load  <" + data + "comma/test.rul>  ";

        String rule = "select * where {graph kg:rule {?x ?p ?y}} ";

        String entail = "select * where {graph kg:entailment {?x ?p ?y}} ";

        String drop = "drop graph kg:rule ;"
                + "drop graph kg:entailment";

        /**
         *
         * ** Rule: 26 * Entail: 1604
         *
         */
        try {
//			Mappings map = exec.query(entail);
//			System.out.println(map.size());

            g.index();
            g.setDebug(true);


            //g.addEngine(ent);
//			g.addEngine(re);
//			g.addEngine(re2);

            g.process();
            exec.query(loadrule);

            Mappings map = exec.query(rule);
            System.out.println("** Rule: " + map.size());

            map = exec.query(entail);
            System.out.println("** Entail: " + map.size());

            System.out.println("** Graph: " + g.size());


            map = exec.query(drop);

            map = exec.query(rule);
            System.out.println("** Rule: " + map.size());

            exec.query(loadrule);

            map = exec.query(rule);
            System.out.println("** Rule: " + map.size());


        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    public void testLoop() {

        Graph g = Graph.create(true);
        QueryProcess exec = QueryProcess.create(g);
        //g.init();

        String init =
                "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "load <file://" + data + "comma/model.rdf> ";

        String update =
                "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "delete {?x c:FirstName ?n}"
                + "where  {?x c:FirstName ?n}";

        update =
                "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "rule construct {?x c:hasFather []} "
                + "where  {?x c:FirstName ?n} ";

        String query = "select * where {?x ?p ?y}";

        try {
            exec.query(init);

            //exec.setDebug(true);
            System.out.println("** U: 1");

            boolean go = true;
            Mappings map = exec.query(update);

            System.out.println(map);

            System.out.println("** Insert: " + map.nbInsert());
            System.out.println("** Delete: " + map.nbDelete());

            System.out.println("** Insert: " + map.getInsert());
            System.out.println("** Delete: " + map.getDelete());

            System.out.println("** Total: " + map.nbUpdate());

            if (map.nbUpdate() == 0) {
                go = false;
            }

            map = exec.query(query);

            //System.out.println(g.display());

//			System.out.println(g.getIndex());
//
//			for (Entity ent : map.getInsert()){
//				System.out.println(ent);
//				System.out.println(g.delete(ent));
//			}
//			
//			System.out.println(g.getIndex());


            //while (exec.query(update).nbUpdate()>0){}



        } catch (EngineException e) {
            e.printStackTrace();
        }


    }

    public void testGL() {

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        //g.init();

        String init =
                "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "load <file://" + data + "comma/comma.rdfs> "
                + "pragma {"
                + //"[" +
                //	"kg:when   [a kg:Insert ; kg:graph kg:entailment ; kg:triple(?x rdf:type c:Person)] ;"+
                //	"kg:action [a kg:Log    ; kg:file '/tmp' ] ;" +
                //	"kg:action [a kg:Broadcast ; kg:target(<server1> <server2>) ]" +
                //"]" +
                ""
                + "["
                + "kg:when [a kg:Query  ; kg:load [a kg:RuleBase]] ;"
                + "kg:then [a kg:Action ; kg:run  [a kg:RuleBase]]"
                + "]"
                + //"[kg:when  [a kg:Insert ; "+
                //  "a kg:greaterThan ; kg:args(kg:size 10000)] ;"+
                //  "kg:action [a kg:Reject]]"+
                "}";


        String ins =
                "insert {?y ?p ?x} where {?x ?p ?y}"
                + "pragma {"
                + "graph kg:listen {"
                + "[ kg:size 3000;"
                + "kg:insert true]"
                + "}"
                + "}";

        String query = "prefix ext: <function://junit.TestUnit>"
                + "select (ext:size(kg:graph()) as ?size) where {"
                + "optional {"
                + "[ex:relation( ex:subject ex:object ex:arg)]"
                + "}"
                + "}";

        try {
//			exec.addPragma(Pragma.LISTEN, Pragma.SIZE, 1000);
//			exec.addPragma(Pragma.LISTEN, Pragma.INSERT, true);

            exec.query(init);
            Mappings map = exec.query(query);

            //g.addListener(GraphListenerImpl.create(5000));

            //exec.query(ins);

            System.out.println(g.size());
            System.out.println(map);
            //System.out.println(g.getListeners().size());

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public IDatatype size(Object o) {
        Graph g = (Graph) o;
        return DatatypeMap.newInstance(g.size());
    }

    public void testMatch() throws LoadException {

        Graph g = Graph.create();
        //g.addListener(GraphListenerImpl.create(10000));
        QueryProcess exec = QueryProcess.create(g);



        Load ld = Load.create(g);

        ld.loadWE(data + "comma/comma.rdfs");
        ld.loadWE(data + "comma/data");
        ld.loadWE(data + "comma/data2");

        String cons =
                "construct {?x ?p ?z}"
                + "where {"
                + "?p a rdf:Property "
                + "{select * (bnode(?y) as ?z) where {"
                + "?x ?p ?y "
                + "} limit 1}"
                + "}";

        try {
            Mappings map = exec.query(cons);
            Graph res = exec.getGraph(map);
            System.out.println("** Query: " + res.size());
            System.out.println("** Target: " + g.size());
            System.out.println(res.display());

            Date d1 = new Date();
            QueryGraph qg = QueryGraph.create(res);

            map = exec.query(qg);
            Date d2 = new Date();
            //System.out.println(map.toString(true));

            System.out.println("** Result: " + map.size());
            System.out.println("** Time: " + (d2.getTime() - d1.getTime()) / 1000.0);

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void testList4() {

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init = "@prefix : <http://test.fr/> "
                + "insert data {"
                + ":xxx rdf:value (3 2 3 3)"
                + "}";

        String update =
                "prefix : <http://test.fr/> "
                + "delete {"
                + "?e rdf:rest ?n ?n rdf:first ?val ?n rdf:rest ?rst"
                + "?x rdf:value ?nn ?nn rdf:first ?val . ?nn rdf:rest ?rr"
                + "}"
                + "insert {"
                + "?e rdf:rest ?rst"
                + "?x rdf:value ?rr "
                + "}"
                + "where {"
                + "{?x rdf:value ?l . ?l rdf:rest* ?e . ?e rdf:rest ?n"
                + " ?n rdf:first ?val . ?n rdf:rest ?rst }"
                + "union"
                + "{?x rdf:value ?nn ?nn rdf:first ?val . ?nn rdf:rest ?rr }"
                + "values ?val {3}"
                + "}";

        String query =
                "prefix : <http://test.fr/> "
                + "select * where {?x ?p ?y}";

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            exec.query(update);
            map = exec.query(query);
            System.out.println(map);


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void testNS() throws LoadException {

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init = "@prefix : <http://test.fr/> "
                + "insert data {:a :p :b}";

        String query =
                "prefix : <http://example/> "
                + "construct {?x ?p ?y} where {?x ?p ?y}";

        Load ld = Load.create(g);
        ld.loadWE(root + "test/crdt.ttl");

        try {
            //exec.query(init);
            Mappings map = exec.query(query);
            Graph gg = exec.getGraph(map);
            RDFFormat f = RDFFormat.create(map);
            f.write(root + "test/tmp.rdf");
            System.out.println(f);

            Graph g2 = Graph.create();
            Load l2 = Load.create(g2);
            l2.loadWE(root + "test/tmp.rdf");
            System.out.println(g2.display());
            System.out.println(g2.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }



    }

    public void test61() {
        DatatypeMap.setLiteralAsString(false);
        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                + "prefix p: <http://fr.dbpedia.org/property/>"
                + "select  * where {"
                + "service <http://fr.dbpedia.org/sparql> {"
                + "<http://fr.dbpedia.org/resource/Auguste> p:succ+ ?y ."
                + "?y rdfs:label ?l}"
                + "}"
                + "pragma {kg:path kg:expand 12}";




        try {
            Mappings map = exec.query(query);
            ResultFormat f = ResultFormat.create(map);
            System.out.println(map);
            System.out.println(f);
            assertEquals("Result", 12, map.size());



        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void testExpand() {

        String query =
                "prefix ex: <http://example.org/>"
                + "select  * where {"
                + //"?x rdf:type/rdfs:subClassOf* ?c" +
                //"?x ex:p0 / (!(ex:p1 | ex:p2))*  ?y" +
                "?x rdf:rest*/rdf:first ?y"
                + //"?x ^rdf:first/(^(! rdf:gogo))+ ?y" +
                "}";
        //"pragma {kg:path kg:expand 3}";

        String init =
                "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:a ex:p0 ex:b "
                + "ex:b ex:p0 ex:c "
                + "ex:c ex:p1 ex:d "
                + ""
                + "ex:list rdf:value (1 2 3) "
                + ""
                + "ex:Human rdfs:subClassOf ex:Animal "
                + "ex:Animal rdfs:subClassOf ex:Living "
                + "ex:John a ex:Human"
                + "}";



        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.addVisitor(ExpandPath.create(3));

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            exec.getGraph(map);

//			ASTQuery ast = exec.getAST(map);			
//			ExpandPath rew = ExpandPath.create(3);
//			System.out.println(rew.rewrite(ast.getBody()));

            System.out.println(exec.getAST(map));
            System.out.println(map.getQuery());
            System.out.println(map);
            System.out.println(ResultFormat.create(map));
            System.out.println(map.size());
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testSyntax() throws LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init = "insert data {<a> a rdfs:Resource}";

        String cons = "construct {?x rdf:value ?y} where {?x a rdfs:Resource} values ?y {10}";

        String query = "select * where {?x ?p ?y}";

        Load ld = Load.create(g);

        ld.loadWE(root + "test/deco.rl");

        try {
            exec.query(init);

            RuleEngine re = ld.getRuleEngine();
            re.process();

            Mappings map = exec.query(query);
            System.out.println(map);

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Total: 0.6 Without XML parse: 0.4
     *
     * 0.553
     */
    public void testServicesdf() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = QueryLoad.create().read(root + "alu/nico.rq");

        try {
            int size = 1;
            Date d1 = new Date();
            for (int i = 0; i < size; i++) {
                //exec.setDebug(true);
                Mappings map = exec.query(q);
                if (i == 0) {
                    System.out.println(map);
                }
            }
            Date d2 = new Date();
            System.out.println(((d2.getTime() - d1.getTime()) / 1000.0) / size);
            //System.out.println(map.size());
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void testSAP() throws LoadException {
        Graph g = Graph.create(true);
        QueryProcess exec = QueryProcess.create(g);

        Load ld = Load.create(g);
        ld.loadWE(root + "sap/q1-light.ttl");
        ld.loadWE(root + "sap/q2-light.ttl");
        ld.loadWE(root + "sap/sqlOnto.ttl");

        String q = QueryLoad.create().read(root + "sap/q1.rq");

        try {
            Date d1 = new Date();
            Mappings map = exec.query(q);
            Date d2 = new Date();
            //System.out.println(map);
            System.out.println(map.size());
            System.out.println((d2.getTime() - d1.getTime()) / 1000.0);
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testCountPath() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init = ""
                + "prefix ex: <http://www.inria.fr/acacia/comma#>"
                + "insert data {"
                + "ex:a ex:p ex:b ex:b ex:p ex:c "
                + "ex:a ex:p ex:d ex:d ex:p ex:c "
                + ""
                + "} ";

        String query = ""
                + "prefix ex: <http://www.inria.fr/acacia/comma#> "
                + "select * where {"
                + "ex:a ex:p+ ?x"
                + "}"
                + "pragma {kg:path kg:count false}";

        try {
            //exec.setCountPath(true);
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println(map.size());




        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    public void testBUG() throws LoadException {

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        Load ld = Load.create(g);
        ld.loadWE(root + "test/q1.ttl");

        QueryLoad ql = QueryLoad.create();
        String q = ql.read(root + "test/q1.rq");
        System.out.println(q);

        try {
            Mappings map = exec.query(q);
            System.out.println(exec.getGraph().display());

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void testDelete() {

        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph, true);

        String init =
                "prefix ex: <http://example.org/> "
                + ""
                + "insert data {"
                + "ex:John a ex:Man "
                + "ex:Jill a ex:Woman "
                + "ex:John a ex:Human "
                + "} ";


        String update =
                "prefix ex: <http://example.org/> "
                + "delete where {"
                + "ex:John ?p ?y"
                + "}"
                + ""
                + "";

        String query =
                "prefix ex: <http://example.org/> "
                + "select *  where  {"
                + "{?x rdf:type* ?c}"
                + "}";


        try {

            exec.query(init);
            exec.query(update);
            Mappings map = exec.query(query);

            System.out.println(map);
            System.out.println(map.size());
            System.out.println(graph);
            System.out.println(graph.getResource("http://example.org/John"));

            //assertEquals("Results", 9, map.size());



        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testType() {

        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph, true);

        String init =
                "prefix ex: <http://example.org/> "
                + ""
                + "insert data {"
                + "ex:Human rdfs:subClassOf ex:Animal "
                + "ex:Man   rdfs:subClassOf ex:Human "
                + "ex:Woman rdfs:subClassOf ex:Human "
                + "graph ex:g1 { "
                + "ex:John a ex:Man "
                + "ex:Jill a ex:Woman "
                + "}"
                + "ex:John a ex:Human "
                + "ex:John a ex:Man "
                + "} ";

        String query =
                "prefix ex: <http://example.org/> "
                + "select *  where  {"
                + //"graph ?g " +
                "{?x a ex:Human}"
                + "}";


        try {

            exec.query(init);
            Mappings map = exec.query(query);

            System.out.println(map);
            System.out.println(map.size());

            //assertEquals("Results", 9, map.size());



        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testOption() {

        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix : <http://example.org/> "
                + ""
                + "insert data {"
                + ":a :p :b, :c ."
                + ":b :p :d, :a "
                + ":c :p :d "
                + ""
                + ":e :p :b, :c ."
                + ""
                + "} ";

        String query =
                "prefix : <http://example.org/> "
                + "select *  where  {"
                + "?x ((:p/:p) ?)  :d "
                + "}";


        try {

            exec.query(init);
            Mappings map = exec.query(query);

            System.out.println(map);
            System.out.println(map.size());
            System.out.println(map.getQuery().getAST());

            //assertEquals("Results", 9, map.size());



        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Create a Query graph from an RDF Graph Execute the query Use case: find
     * similar Graphs (cf Corentin)
     */
    public void test52() {

        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix : <http://example.org/> "
                + "insert data {"
                + "graph <q1> {"
                + "[a :Query ; :query <q1> ; "
                + ":where [:subject [] ; :property [] ; :object 10 ]]"
                + "}"
                + "graph <q2> {"
                + "[a :Query ; :query <q2> ; "
                + ":where [:subject [] ; :property [] ; :object 20 ]]"
                + "}"
                + ""
                + "} ";

        String onto =
                "prefix : <http://example.org/> "
                + "insert data {"
                + ":Query rdfs:subClassOf :Action"
                + "}";

        // extract a subgraph 
        // replace literal with bnode
        String cons =
                "prefix : <http://example.org/> "
                + "construct { graph ?g { ?x ?p ?o }}"
                + "where {"
                + "select * "
                + "(if (?p = :query || isLiteral(?y), bnode(), ?y) as ?o) "
                + "where {"
                + "graph ?g { [:object ?v] . filter(?v >= 10) ?x ?p ?y }"
                + "}"
                + "}";

        // rewrite subClass as superClass
        String rew =
                "prefix : <http://example.org/> "
                + "delete {graph ?g {?x a ?c}}"
                + "insert {graph ?g {?x a ?c2}}"
                + "where  {"
                + "graph ?g {?x a ?c} "
                + "?c rdfs:subClassOf ?c2"
                + "}";

        Graph go = Graph.create();


        try {
            // Load ontology
            QueryProcess.create(go).query(onto);

            // create a graph
            exec.query(init);
            exec.query(onto);

            // create a copy where triple objects (values) are Blank Nodes (aka Variables)
            Mappings map = exec.query(cons);
//			System.out.println(map);
//			System.out.println(map.size());

            Graph g2 = exec.getGraph(map);
            //System.out.println(TripleFormat.create(g2, true));

            List<Graph> list = g2.split();

            for (Graph g : list) {
                System.out.println(TripleFormat.create(g, true));

                QueryProcess rewrite = QueryProcess.create(g);
                rewrite.add(go);
                rewrite.query(rew);


                System.out.println(TripleFormat.create(g, true));

                map = exec.query(g);
                System.out.println(map.toString(true));
                System.out.println(map.size());
            }

//			QueryProcess rewrite = QueryProcess.create(g2);
//			rewrite.query(rew);
//			System.out.println(TripleFormat.create(g2));


//			QueryGraph qg = QueryGraph.create(g2);
//			QGVisitor vis = new QGVisitor();
            //qg.setVisitor(vis);
            //qg.setConstruct(true);
            //map = exec.query(g2);									

            //Graph res = exec.getGraph(map);
            //assertEquals("Results", 2, res.size());

            //System.out.println(TripleFormat.create(res));
//			
//			System.out.println(map.toString(true));
//			System.out.println(map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testBNode() {
        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init = ""
                + "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                + "insert data {"
                + "<John> foaf:knows <Jim>, <James>; foaf:prout <jjj>  "
                + "<Jack> foaf:knows <Jim> ; foaf:prout <hhh>"
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                + "select ?x "
                + "(count(?y) as ?c1) (count(distinct ?z) as ?c2)"
                + "(bnode(?c1) as ?b1) (bnode(?c2) as ?b2)"
                + "where {"
                + "?x foaf:knows ?y ; foaf:prout ?z "
                + "} group by ?x";

        try {
            exec.query(init);

            Mappings map = exec.query(query);
            System.out.println(map);

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void test60() {
        DatatypeMap.setLiteralAsString(false);
        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init, query;

        init = ""
                + "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                + "insert data {"
                + "<John> foaf:knows <Jack> ; foaf:knows 'John' "
                + "<Jack> foaf:knows <Jim> ; foaf:knows 'Jack'"
                + "<John> foaf:knows <James>"
                + "<James> foaf:knows <Jim> ; foaf:knows 'James' "
                + "<Jim> foaf:knows <Jules>  "
                + "<Jim> foaf:knows 'Jim' "
                + "<James> foaf:knows <John>  "
                + "}";

        query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                + "select * where {"
                + "?x foaf:knows+ ?y "
                + "filter(isURI(?x))"
                + "}"
                + "order by ?x ?y";


//		init = 
//			"prefix foaf: <http://xmlns.com/foaf/0.1/>" +
//			"insert data {" +
//				"<John> foaf:knows (<Jack> <Jack> <Jim> <Jack>) " +
//				"" +
//				"}";				
//		
//		query = 
//			"prefix foaf: <http://xmlns.com/foaf/0.1/>" +
//			"select * where {" +
//			"?x foaf:knows/rdf:rest+/rdf:first ?y " +
//			"filter(isURI(?y))" +
//			"}";
//		

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println(map.size());



        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    /**
     * select where { ?x foaf:FamilyName 'Corby' ?x foaf:isMemberOf ?org ?x
     * foaf:FirstName ?name filter (?name = 'toto' || ?org ~ 'inria' )}
     */
//    Graph init2() {
//        String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
//        String root = "/home/corby/workspace/kgengine/src/test/resources/data/";
//
//        Graph graph = Graph.create(true);
//        graph.set(Entailment.DATATYPE_INFERENCE, true);
//
//        Load load = Load.create(graph);
//        graph.setOptimize(true);
//        System.out.println("load");
//
//        load.load(data + "kgraph/rdf.rdf", RDF.RDF);
//        load.load(data + "kgraph/rdfs.rdf", RDFS.RDFS);
//        load.load(data + "comma/comma.rdfs");
//        //load.load(data + "comma/commatest.rdfs");
//        load.load(data + "comma/model.rdf");
//        load.load(data + "comma/testrdf.rdf");
//        load.load(data + "comma/data");
//        load.load(data + "comma/data2");
//
//        try {
//            load.loadWE(root + "rule/rdfs.rul");
//            load.loadWE(root + "rule/owl.rul");
//
//            //load.loadWE(root + "rule/tmp.rul");
//
//        } catch (LoadException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        System.out.println("init");
//        long t1 = new Date().getTime();
//        graph.init();
//        long t2 = new Date().getTime();
//        System.out.println((t2 - t1) / 1000.0 + "s");
//
//        System.out.println("rule");
//
//        t1 = new Date().getTime();
////		RuleEngine re = load.getRuleEngine();
////		//int nb = re.process();
////		 t2 = new Date().getTime();
////		System.out.println("** Time: " + (t2-t1) / 1000.0 + "s");
////		System.out.println("** Size: " +graph.size());
//        //System.out.println("** Rule Entailment: " + nb);
////		
//        return graph;
//    }

    public void test7() throws ParserConfigurationException, SAXException, IOException {
        Graph g1 = Graph.create(true);
//		Load ld = Load.create(g1);
//		Date d1 = new Date();
//		ld.load(data + "commattl/copy.ttl");
//		Date d2 = new Date();
//		System.out.println("Time: " + (d2.getTime() - d1.getTime()) / 1000.0);
//		g1.init();
//		System.out.println(g1.size());

        String query =
                "prefix foaf: <http://www.inria.fr/acacia/comma#>"
                + "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                + "prefix p: <http://fr.dbpedia.org/property/>"
                + "insert {<http://fr.dbpedia.org/resource/Auguste> p:succ ?y}  where {"
                + "service <http://fr.dbpedia.org/sparql> {"
                + "<http://fr.dbpedia.org/resource/Auguste> p:succ+ ?y "
                + "}" + ""
                + "}"
                + "pragma {"
                + "kg:service kg:timeout 1000"
                + "kg:path kg:expand 5 "
                + "}";


        String del = "clear all";

        QueryProcess exec = QueryProcess.create(g1);
        //exec.addPragma(Pragma.SERVICE, Pragma.TIMEOUT, 10);
        //exec.addPragma(Pragma.PATH, Pragma.EXPAND, 5);

        try {

            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println(map.size());

            TripleFormat tf = TripleFormat.create(g1, true);
            tf.with(Entailment.DEFAULT);
            System.out.println(tf);


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void test8() {
        Graph g = Graph.create(true);
        QueryProcess exec = QueryProcess.create(g);

        String init = "insert data {"
                + "<a> foaf:knows <e> "
                + "<a> foaf:knows <c> "
                + "<a> foaf:knows <b> <b> foaf:knows <c>}";

        String query =
                //"construct {?x ?p ?y}" +
                "select * (min(?l) as ?min)"
                + "where {"
                + "{select * (pathLength($path) as ?l)  where {"
                + "?a short(foaf:knows+) :: $path ?b } }"
                + "values ?a {<a>}"
                + "graph $path {?x ?p ?y}"
                + "} "
                + //"having (?l = min(?l))" +
                "bindings ?a ?b {"
                + "(<a> <c>)"
                + "}"
                + "pragma {kg:query kg:check true}";

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            ASTQuery ast = exec.getAST(map);
            System.out.println(ast);

            ResultFormat f = ResultFormat.create(map);
            System.out.println(f);

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    public void ttest2() {
        Graph g = Graph.create(true);
        QueryProcess exec = QueryProcess.create(g);

        String init = "insert data {"
                + "graph <g1> {<John> foaf:knows <Jim>} "
                + "graph <g2> {<John> foaf:knows <James> }"
                + "}";

        String del = "delete data {"
                + "<John> foaf:knows <James> "
                + "}";


        String query =
                "select * "
                + "from kg:entailment "
                + "where {"
                + "?x ?p ?y "
                + //"?p rdf:type rdf:Property"+
                "}";


        query =
                "select * "
                + "where {"
                + "?x foaf:knows* ?y"
                + "}";


        try {
            exec.query(init);
            exec.query(del);
            Mappings map = exec.query(query);

            ResultFormat f = ResultFormat.create(map);
            System.out.println(f);

//			RDFFormat ff = RDFFormat.create(g);
//			System.out.println(ff);


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    /**
     * Random graph creation
     */
    public void testUpdate() {
        int nbnode = 100000;
        int nbedge = nbnode;


        Graph g = Graph.create(true);

        Node pred = g.addProperty("foaf:knows");

        String init = ""
                + "insert data {"
                + "foaf:knows rdfs:domain foaf:Person "
                + "foaf:knows rdfs:range  foaf:Person"
                + "}";

        Date d1 = new Date();

        for (int j = 0; j < 10; j++) {
            System.out.println(j);
            for (int i = 0; i < nbedge; i++) {
                long sd = Math.round(Math.random() * nbnode);
                long od = Math.round(Math.random() * nbnode);

                Node sub = g.addResource(Long.toString(sd));
                Node obj = g.addResource(Long.toString(od));

                g.addEdge(sub, pred, obj);
            }
        }

        System.out.println("Size: " + g.size());

        Date d2 = new Date();
        System.out.println("Create Time : " + (d2.getTime() - d1.getTime()) / 1000.0);

        g.init();

        Date d3 = new Date();


        System.out.println("Index Time : " + (d3.getTime() - d2.getTime()) / 1000.0);


        String query = "select * where {"
                + "?x foaf:knows ?y "
                + "?z foaf:knows ?x"
                + "?y foaf:knows ?z "
                + "}"
                + "limit 5";

        String update =
                "delete {"
                + "?x foaf:knows ?y "
                + "?y foaf:knows ?z "
                + "?z foaf:knows ?x"
                + "}"
                + "where {"
                + "{select * where {"
                + "?x foaf:knows ?y "
                + "?z foaf:knows ?x"
                + "?y foaf:knows ?z "
                + "}"
                + "limit 5}"
                + "}";

        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            d2 = new Date();
            g.init();
            d3 = new Date();
            System.out.println("Infer Time : " + (d3.getTime() - d2.getTime()) / 1000.0);


            exec.query(query);
            Date d4 = new Date();
            System.out.println("Query Time : " + (d4.getTime() - d3.getTime()) / 1000.0);


            Mappings map = exec.query(update);
            Date d5 = new Date();
            System.out.println("Update Time : " + (d5.getTime() - d4.getTime()) / 1000.0);

            g.init();

            Date d6 = new Date();
            System.out.println("Infer Time : " + (d6.getTime() - d5.getTime()) / 1000.0);

            System.out.println(g.size());
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Size: 1000000 Create Time : 3.102 Index Time : 4.844
     * fr.inria.acacia.corese.triple.parser.ASTQuery.setError(ASTQuery.java:372)
     * Undefined prefix: foaf:knows
     * fr.inria.acacia.corese.triple.parser.ASTQuery.setError(ASTQuery.java:372)
     * Undefined prefix: foaf:Person Infer Time : 7.345
     * fr.inria.acacia.corese.triple.parser.ASTQuery.setError(ASTQuery.java:372)
     * Undefined prefix: foaf:knows
     *
     * Query Time : 1.468 Query Time : 4.767
     *
     * Infer Time : 5.18 1099947
     */
    public void test451() {

        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix foaf: <http://test/> "
                + "insert data {"
                + "tuple(foaf:knows <John>  <James> 2)"
                + "tuple(foaf:knows <John>  <James> 1)"
                + "tuple(foaf:knows <John>  <James> 2 3)"
                + "tuple(foaf:knows <John>  <Jim>   1)"
                + "tuple(foaf:knows <John>  <James> )"
                + "tuple(foaf:knows <Jack>  <James> )"
                + "tuple(foaf:knows <Jim>  <James> )"
                + "} ;";

        String query =
                "prefix foaf: <http://test/> "
                + "prefix ext: <function://junit.TestUnit>"
                + "select * where {"
                + //"graph ?g " +
                "{ "
                + "tuple(foaf:knows ?x ?n ?v) "
                + "?x foaf:knows::?p ?y  "
                + //					  "filter(?x != ?y)" +
                "}"
                + "}";



        try {
            exec.query(init);
            graph.init();
            System.out.println("** Size: " + graph.size());

            ASTQuery ast = ASTQuery.create(query);
            ParserSparql1.create(ast).parse();

            System.out.println(ast);

            Mappings map = exec.query(query);

            System.out.println(map);

            assertEquals("Result", 2, map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static String dateToGMTString(Date dateToBeFormatted) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.FRANCE);
        return dateFormat.format(dateToBeFormatted);
    }

    public static Date GMTStringToDate(String gmtDateString) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.FRANCE);
            return dateFormat.parse(gmtDateString);
        } catch (ParseException e) {
            // manage exception
            e.printStackTrace();
        }
        return null;
    }

    public void test50() {

        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);
        String d1 = dateToGMTString(new Date());
        for (int i = 0; i < 10000000; i++) {
        }
        String d2 = dateToGMTString(new Date());

        System.out.println(d1);
        System.out.println(d2);

        try {
            IDatatype dt1 = new CoreseDate(d1);
            IDatatype dt2 = new CoreseDate(d2);
        } catch (CoreseDatatypeException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        String init =
                "prefix foaf: <http://test/> "
                + "insert data {"
                + "<John> foaf:date '" + d1 + "'^^xsd:dateTime "
                + "<Jim> foaf:date  '" + d2 + "'^^xsd:dateTime "
                + "} ;";

        String query =
                "prefix foaf: <http://test/> "
                + "select * where {"
                + "?x foaf:date ?d"
                + "}"
                + "order by desc(?d)";


        try {
            exec.query(init);
            graph.init();
            System.out.println("** Size: " + graph.size());



            Mappings map = exec.query(query);

            System.out.println(map);

            assertEquals("Result", 2, map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public Object fun(Object obj) {
        return DatatypeMap.TRUE;
    }

    public void testDate() throws LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.loadWE(root + "isicil/date.rdf");

        QueryLoad ql = QueryLoad.create();
        String query = ql.read(root + "isicil/date.rq");

        IEngine engine = new EngineFactory().newInstance();
        try {
            engine.load(root + "isicil/date.rdf");
        } catch (EngineException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }


        try {
            IResults res = engine.query(query);
            System.out.println(res);

        } catch (EngineException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }




        String query2 = "prefix dc: <http://purl.org/dc/elements/1.1/>"
                + "select * where {	"
                + "?msg dc:created ?date "
                + "}"
                + "order by (?date)";

        QueryProcess exec = QueryProcess.create(g);

        try {
            Mappings map = exec.query(query);

            ResultFormat f = ResultFormat.create(map);

            //
            //System.out.println(f);
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }



    }

    public void test65() {
        Graph g = Graph.create();

        Load ld = Load.create(g);

        try {
            ld.loadWE(root + "test/iso.ttl");
            ld.loadWE(root + "test/iso.rdf");

            ld.loadWE(root + "test/utf.ttl");
            ld.loadWE(root + "test/utf.rdf");

            ld.loadWE(root + "test/iso.rul");

        } catch (LoadException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        String query = "select * where {"
                + "?x ?p ?y . ?z ?q ?y filter(?x != ?z)"
                + "}";


        QueryProcess exec = QueryProcess.create(g);

        try {
            Mappings map = exec.query(query);
            System.out.println(map);
            assertEquals("Result", 4, map.size());

            ResultFormat f = ResultFormat.create(map);
            System.out.println(f);

            RDFFormat ff = RDFFormat.create(g);
            System.out.println(ff);

        } catch (EngineException e) {
            e.printStackTrace();
        }

        RuleEngine re = ld.getRuleEngine();
        Rule r = re.getRules().get(0);
        System.out.println(r.getQuery().getAST());

        System.out.println(System.getProperty("file.encoding"));

    }

    public void test70() {
        Graph g = Graph.create();
        Load ld = Load.create(g);

        try {
            System.out.println("** Load: alu/dbpedia_3.7.rdfs");
            ld.loadWE(root + "alu/dbpedia_3.7.rdfs");

        } catch (LoadException e1) {
            e1.printStackTrace();
        }

        String query = "select ?sim (kg:similarity(owl:Thing,owl:Thing) as ?sim) where {}";



        String query2 = "select ?depth (kg:depth(<http://schema.org/CreativeWork>) as ?depth) where {}";


        QueryProcess exec = QueryProcess.create(g);

        try {
            System.out.println("** Query 1");
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println("** Query 2");
            map = exec.query(query2);

            System.out.println(map);
            System.out.println(map.size());
            assertEquals("Result", 4, map.size());

            ResultFormat f = ResultFormat.create(map);
            System.out.println(f);

        } catch (EngineException e) {
            e.printStackTrace();
        }



    }

    public void testJoin() {
        Graph g = Graph.create(); //init(); //Graph.create();
        Load ld = Load.create(g);

        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data   {"
                + "graph <g1> {"
                + "<John> foaf:name 'John' "
                + "<http://fr.dbpedia.org/resource/Auguste>  foaf:knows <James>"
                + "<http://fr.dbpedia.org/resource/Auguste>  foaf:knows <Jack>"
                + "<http://fr.dbpedia.org/resource/Augustus> foaf:knows <Jim>"
                + "<http://fr.dbpedia.org/resource/Augustin> foaf:knows <Jim>"
                + "<http://fr.dbpedia.org/resource/Augusgus> foaf:knows <Jim>"
                + "}"
                + "graph <g1> {"
                + "<Jim> foaf:knows <James>"
                + "<Jim> foaf:name 'Jim' "
                + "}"
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select debug * where {"
                + "service <http://fr.dbpedia.org/sparql> {{"
                + "select * where {"
                + "<http://fr.dbpedia.org/resource/Auguste> <http://www.w3.org/2000/01/rdf-schema#label> ?n"
                + "?x rdfs:label ?n "
                + "} limit 20"
                + "}}"
                + "service <http://fr.dbpedia.org/sparql> {{"
                + "select * where {"
                + "?x rdfs:label ?n "
                + "}"
                + "}}"
                + "}"
                + "";


        query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select debug *  where {"
                + "?x ?p ?y "
                + "service <http://fr.dbpedia.org/sparql> {{"
                + "select * where {"
                + "?x ?p ?y "
                + "}"
                + "}}"
                + "}"
                + "";


        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.setOptimize(true);
            exec.query(init);

            Mappings map = exec.query(query);
            System.out.println(map);

            assertEquals("Result", 2, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }



    }

    public void testRelax() {
        Graph g = Graph.create(); //init();

        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "insert data {"
                + "<John> foaf:type c:Researcher "
                + "<John> foaf:knows <Jack> "
                + "<Jack> foaf:type c:Engineer "
                + "<John> foaf:knows <Jim> "
                + "<Jim> foaf:type c:Fireman "
                + "<e> foaf:type c:Event "
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select   more * (kg:similarity() as ?s) "
                + "(uuid() as ?u)"
                + "(struuid() as ?su)"
                + "where {"
                + "?x foaf:type c:Engineer "
                + "?x foaf:knows ?y "
                + "?y foaf:type c:Engineer"
                + "}"
                + "order by desc(?s) "
                + "pragma {kg:kgram kg:relax  rdf:type, foaf:type}";


        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            assertEquals("Result", 2, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }




    }

    public void testValues4() {
        Graph g = Graph.create();
        Load ld = Load.create(g);



        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<a> foaf:knows <b> "
                + "<c> foaf:knows <d> "
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + //"?x foaf:knows ?y " +
                "values ?x {<b> <c>}"
                + "}"
                + "values ?y {<b> <e>}"
                + "";


        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println(map.getQuery());
            System.out.println(exec.getAST(map));

            //assertEquals("Result", 4, map.size());

            ResultFormat f = ResultFormat.create(map);
            System.out.println(f);

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    public void testNode() {
        Graph g = Graph.create();


        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<?a> foaf:knows <?b> "
                + "<?b> foaf:knows <?c> "
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + "<?a> foaf:knows ?a ?a foaf:knows ?b "
                + "}";


        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println(map.size());

            //assertEquals("Result", 4, map.size());

            ResultFormat f = ResultFormat.create(map);
            System.out.println(f);

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    public void testBlank() {
        Graph g = Graph.create();
        Load ld = Load.create(g);


        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "graph <g1> {_:b foaf:knows <b>} "
                + "graph <g2> {_:b foaf:knows <b>} "
                + "graph <g2> {_:b1 foaf:knows <b>} "
                + "}";

        String query0 =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert {graph <g2> {?x foaf:knows ?y} }"
                + " where {"
                + "graph <g1> {?x foaf:knows ?y} "
                + //"values ?x {<a>}" +
                "}";


        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + "graph ?g {?x foaf:knows ?y} "
                + //"values ?x {<a>}" +
                "}";


        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings m = exec.query(query0);
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println(map.size());

            //assertEquals("Result", 4, map.size());

            ResultFormat f = ResultFormat.create(map);
            System.out.println(f);

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    public void testTTL() {
        Graph g = Graph.create();
        Load ld = Load.create(g);

        try {
            ld.loadWE(data + "commattl/comma.ttl");
            ld.loadWE(data + "commattl/model.ttl");

        } catch (LoadException e1) {
            e1.printStackTrace();
        }

        System.out.println(g);

        EngineFactory fac = new EngineFactory();
        IEngine engine = fac.newInstance();
        GraphEngine eng = (GraphEngine) engine;
        Graph gg = eng.getGraph();
        gg.setEntailment(false);


    }

    public void testAlban() {
        Graph g = Graph.create();
        QueryLoad ld = QueryLoad.create();

        String q = ld.read(ndata + "alban/query/q1.rq");

        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<a> foaf:knows <b> "
                + "<c> foaf:knows <d> "
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + "?x foaf:knows ?y "
                + //"values ?x {<a>}" +
                "}";


        QueryProcess exec = QueryProcess.create(g);

        try {
            System.out.println(q);
            Mappings map = exec.query(q);
            System.out.println(map);
            System.out.println(map.size());

            //assertEquals("Result", 4, map.size());

            ResultFormat f = ResultFormat.create(map);
            System.out.println(f);

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    public void testBind() {
        Graph g = Graph.create();
        Load ld = Load.create(g);

        String init = " insert data {"
                + "<John> foaf:age 2"
                + "<John> foaf:age 3"
                + "<John> foaf:age 4"
                + "}";


        String query2 =
                "select more * where {"
                + //"filter(?x = 2)" +
                "bind(3 as ?x)"
                + ""
                + "}"
                + "values ?x {1 2 3}";

        String query =
                "select * where {"
                + "select more  *  (5 as ?x) (3 as ?x)  where {"
                + "?a foaf:age ?x"
                + //"{select (3 as ?x) {}}" +
                ""
                + "}"
                + "}";


        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);

            Mappings map = exec.query(query);

            System.out.println(map);
            System.out.println(map.size());
            //System.out.println(map.get(0).getQueryNodes().length);
            System.out.println(exec.getAST(map).getSelectAllVar());
            System.out.println(map.getQuery().getSelectFun());
            System.out.println(exec.getAST(map));

            //assertEquals("Result", 4, map.size());

            ResultFormat f = ResultFormat.create(map);
            System.out.println(f);

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    public void test30() {

        NSManager nsm = NSManager.create();
        nsm.definePrefix("foaf", "http://foaf.org/");


        ASTQuery ast = ASTQuery.create();
        ast.setNSM(nsm);

        Triple t1 = Triple.create(Variable.create("?x"), ast.createQName("foaf:knows"), Variable.create("?y"));

        ast.setBody(BasicGraphPattern.create(t1));

        ast.setDescribe(Variable.create("?x"));

        String init =
                "prefix foaf: <http://foaf.org/>"
                + "insert data {<John> foaf:knows <Jim>"
                + "<John> owl:sameAs <Johnny>}";

        String query =
                "prefix foaf: <http://foaf.org/>"
                + "construct {?y foaf:knows ?x}"
                + "where {?x foaf:knows ?y}";


        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings map = exec.query(ast);
            RDFFormat f = RDFFormat.create(map);

            System.out.println(ast);
            System.out.println(map);
            System.out.println(f);
            assertEquals("Result", map.size(), 2);


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            assertEquals("Result", true, e);
        }
    }

    public void testAgg() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryLoad ql = QueryLoad.create();
        String qq = ql.read(ndata + "test/agg.rq");



        String init =
                "PREFIX ex: <http://example.org/meals#>  "
                + "insert data {"
                + "[ ex:mealPrice 1 ; "
                + " ex:mealTip 2 ;"
                + "ex:mealPrice 3 ; "
                + "ex:mealTip 4 ;"
                + " ] "
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + "?x foaf:knows ?y "
                + //"values ?x {<a>}" +
                "}";


        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings map = exec.query(qq);
            System.out.println(map);
            System.out.println(map.size());

            //assertEquals("Result", 4, map.size());

            ResultFormat f = ResultFormat.create(map);
            System.out.println(f);

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    public void testJulien() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryLoad ql = QueryLoad.create();

        String qq = ql.read(ndata + "test/dbpedia.rq");

        try {
            ld.loadWE(root + "test/iso.ttl");
            //ld.loadWE(data + "commattl/comma.ttl");

        } catch (LoadException e1) {
            e1.printStackTrace();
        }

        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<a> foaf:knows <b> "
                + "<c> foaf:knows <d> "
                + "}";

        //http://fr.dbpedia.org/property/texte	

        String query =
                "prefix dbpedia-owl: <http://dbpedia.org/ontology/> "
                + "prefix dbfr: <http://fr.dbpedia.org/> "
                + "prefix dbpedia-prop: <http://dbpedia.org/property/> "
                + "prefix dbp: <http://dbpedia.org/property/> "
                + "select debug  distinct  ?p1 "
                + "where { "
                + "service <http://dbpedia.org/sparql>    {<http://dbpedia.org/resource/Paris> ?r2 ?p1 "
                + "filter(?r2 != dbp:texte)"
                + "filter(isLiteral(?p1))"
                + // "filter(?p1 = 20)" +
                "}  . "
                + "filter( isNumeric(?p1))"
                + //               "filter( lang(?p1) = 'fr')" +
                //               
                " service <http://fr.dbpedia.org/sparql> {<http://fr.dbpedia.org/resource/Paris> ?r1 ?p1 . }  "
                + "}"
                + "order by ?p1 "
                + "pragma {"
                + "kg:service kg:slice 3 "
                + "}";;


        QueryProcess exec = QueryProcess.create(g);
        //exec.setSlice(30);

        try {
            Mappings map = exec.query(query);
            System.out.println("** Result: " + map.size());
            System.out.println(map.size());
            System.out.println(map);

            //assertEquals("Result", 4, map.size());

            ResultFormat f = ResultFormat.create(map);
            //System.out.println(f);			

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    public void test1() {

        Graph g = Graph.create();
        Load ld = Load.create(g);

        try {
            ld.loadWE(data + "comma/comma.rdfs");
            ld.loadWE(data + "comma/data");
            ld.loadWE(data + "comma/data2");
        } catch (LoadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        TripleFormat t = TripleFormat.create(g, true);
        try {
            t.write(data + "commattl/global.ttl");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void test2() {

        Graph g = Graph.create();
        Load ld = Load.create(g);

        try {
            ld.loadWE(data + "commattl/global.ttl");
        } catch (LoadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String query = "select * where {?x <p> 'ab\\ncd'}";

        QueryProcess exec = QueryProcess.create(g);

        try {
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println(exec.getAST(map));

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("** Size: " + g.size());
    }

    public void test3() {

        Graph g = Graph.create();
        Load ld = Load.create(g);

        try {
            ld.loadWE(data + "comma/data2/f125.rdf");
        } catch (LoadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String query =
                "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select * where {?x c:Title ?t}";

        QueryProcess exec = QueryProcess.create(g);

        try {
            Mappings map = exec.query(query);
            System.out.println(map);
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void testNicolas() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryLoad ql = QueryLoad.create();



        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<a> foaf:knows <b> "
                + "<c> foaf:knows <d> "
                + "}";

        //  9.095

        // 11.81


        String prop = "prefix db: <http://fr.dbpedia.org/property/>"
                + "select  ?p where {"
                + "service <http://fr.dbpedia.org/sparql> {"
                + "select distinct ?p where {"
                + "?p rdf:type rdf:Property "
                + "filter(?p != db:isbn)"
                + "filter (! regex(str(?p), 'owl'))"
                + "filter (! regex(str(?p), 'wiki'))"
                + "}  limit 100"
                + "}"
                + "}  order by ?p ";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + //"select * " +
                "insert {?x <%s> ?p1 }"
                + "where {"
                + "service <http://fr.dbpedia.org/sparql> {"
                + "select * where {?x <%s> ?p1 } limit 10"
                + "}}";

        /**
         * 219247 22.83 29 17.271
         *
         * 353422 49 45.816 49 25.154
         *
         * 49 37.099
         *
         *
         * 498615 99 60.255 99 44.21
         *
         *
         */
        QueryProcess exec = QueryProcess.create(g);

        try {
            //exec.query(init);
            Date d1 = new Date();

            int slice = 1;

            Mappings res = exec.query(prop);
            System.out.println(res);


            for (int i = 0; i < res.size(); i++) {
                Node np = res.get(i).getNode("?p");
                String name = np.getLabel();
                System.out.println(name);
                Formatter f = new Formatter();
                String qq = f.format(query, name, name).toString();
                //String qq = f.format(query, name).toString();
                //System.out.println(qq);
                Mappings map = exec.query(qq);
                //System.out.println(map);
                System.out.println(g.size());
                Date d2 = new Date();
                System.out.println(name);
                System.out.println(i + " " + (d2.getTime() - d1.getTime()) / 1000.0);
            }

            //System.out.println(res);


            /**
             * 9 12.824 9 12.073 9 12.288
             *
             */
// 28.5 pour 200 000 (vs 15.7 select *)
// 		300 000 insert : 51.007    select * : 23
        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    public void testIGN() {
        Graph g = Graph.create(true);
        Load ld = Load.create(g);

        try {
            ld.loadWE(root + "ign/ontology/ign.owl");
            //ld.loadWE(ndata + "test/onto.ttl");


        } catch (LoadException e1) {
            e1.printStackTrace();
        }

        // query generate a construct where rule for property Chain
        String init =
                "select debug  "
                + "(concat('construct {?x ',  kg:qname(?q), ' ?y} where {?x ', ?r , ' ?y}') as ?req)"
                + "(group_concat(kg:qname(?p) ; separator='/') as ?r)"
                + "where {"
                + "?q owl:propertyChainAxiom/rdf:rest*/rdf:first ?p "
                + "}"
                + "group by ?q";


        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "prefix ign: <http://www.semanticweb.org/ontologies/2012/5/Ontology1339508605479.owl#>"
                + "select * where {"
                + "?x ign:aLaTeinteDe ?t "
                + "}";

        String query2 =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "prefix t: <http://ns.inria.fr/test/>"
                + "select * where {"
                + "graph ?g {?x a t:Male ;  a ?t } "
                + "}";

        QueryProcess exec = QueryProcess.create(g);

        try {


//			OWLRule owl = OWLRule.create(g);
//			owl.process();

            Mappings map = exec.query(init);
            System.out.println(map);
            System.out.println(map.getQuery());
            System.out.println(map.size());
            System.out.println(exec.getAST(map));


        } catch (EngineException e) {
            e.printStackTrace();
        }
//		catch (LoadException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

    }
    // http://www.subshell.com/en/subshell/blog/article-Changing-from-m2eclipse-to-m2e-Eclipse-Indigo100.html
    // org.eclipse.m2e.launchconfig.classpathProvider"/>
    // org.maven.ide.eclipse.launchconfig.classpathProvider

    public void dbpedia() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryLoad ql = QueryLoad.create();

        String query = ql.read(root + "test/dbpedia.rq");

        System.out.println(query);



        QueryProcess exec = QueryProcess.create(g);
        //exec.setDebug(true);

        try {
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println(map.size());

            //assertEquals("Result", 4, map.size());

            ResultFormat f = ResultFormat.create(map);
            System.out.println(f);

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    public void start2() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryLoad ql = QueryLoad.create();

        try {
            ld.loadWE(root + "test/iso.ttl");

        } catch (LoadException e1) {
            e1.printStackTrace();
        }

        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<a> foaf:knows <b> "
                + "<c> foaf:knows <d> "
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + "?x foaf:knows ?y "
                + //"values ?x {<a>}" +
                "}";


        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println(map.size());

            //assertEquals("Result", 4, map.size());

            ResultFormat f = ResultFormat.create(map);
            System.out.println(f); 
            //test

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }
}
