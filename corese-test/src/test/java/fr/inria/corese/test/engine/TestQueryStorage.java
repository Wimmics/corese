package fr.inria.corese.test.engine;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.jena.JenaTdb1DataManagerBuilder;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;

/**
 * Test data manager
 * ldscript 
 * transformer
 * rule engine
 */
public class TestQueryStorage {

    private static final String STORAGE = "/user/corby/home/AADemoNew/storage/storagetest";
    private static final String DATASET = "/user/corby/home/AADemoNew/human/human.rdf";
    private static final String SHACL = "/user/corby/home/AADemoNew/human/shape1.ttl";
    private static final String SCHEMA = "/user/corby/home/AADemoNew/human/human.rdfs";

    static DataManager dataManager;
    private static Graph graph; 

//    public static void main(String[] args) throws LoadException, EngineException {
//        new TestQueryStorage().test();
//    }
    
    @BeforeClass
    public static void start() throws LoadException {
        init();
    }

    static void init() throws LoadException {
        DataManager man = new JenaTdb1DataManagerBuilder().storagePath(STORAGE).build();
        man.getCreateMetadataManager();
        Graph g = Graph.create();
        
        Load ld = Load.create(g, man);
        ld.parse(SCHEMA);
        ld.parse(DATASET);
        setDataManager(man);
        
        setGraph(Graph.create());
        Load load = Load.create(getGraph());
        load.parse(SCHEMA);
        load.parse(DATASET);        
    }

    @Test
    public void testshacl() throws EngineException {
        String q = "prefix i: <http://www.inria.fr/2015/humans-instances#>"
                + "select * where {"
                + "bind (xt:shaclGraph(<%s>) as ?g)"
                //+ "bind (xt:shaclGraph(<%s>, us:test2) as ?g)"
                //+ "bind (xt:shaclNode(<%s>, i:John) as ?g)"
                //+ "bind (xt:shaclNode(<%s>, i:John, us:test2) as ?g)"
                + "}";
        QueryProcess exec = QueryProcess.create(getDataManager());
        Mappings map = exec.query(String.format(q, SHACL));
        IDatatype dt = map.getValue("?g");
        Graph g = (Graph) dt.getPointerObject();
//        System.out.println(g.display());
//        System.out.println(g.size());
        assertEquals(101, g.size());
    }

    @Test
    public void testshacl1() throws EngineException {
        String q = "prefix i: <http://www.inria.fr/2015/humans-instances#>"
                + "select * where {"
                //+ "bind (xt:shaclGraph(<%s>) as ?g)"
                + "bind (xt:shaclGraph(<%s>, us:test2) as ?g)"
                //+ "bind (xt:shaclNode(<%s>, i:John) as ?g)"
                //+ "bind (xt:shaclNode(<%s>, i:John, us:test2) as ?g)"
                + "}";
        QueryProcess exec = QueryProcess.create(getDataManager());
        Mappings map = exec.query(String.format(q, SHACL));
        IDatatype dt = map.getValue("?g");
        Graph g = (Graph) dt.getPointerObject();
//        System.out.println(g.display());
//        System.out.println(g.size());
        assertEquals(83, g.size());
    }

    @Test
    public void depth() throws EngineException {
        String q2 = "select * "
                + "(xt:depth(?c1) as ?d) (min(?d) as ?min) (max(?d) as ?max)"
                + "where {"
                + "?c1 a rdfs:Class "
                + "}";

        QueryProcess exec = QueryProcess.create(getDataManager());
        Mappings map = exec.query(q2);
        IDatatype min = map.getValue("?min");
        IDatatype max = map.getValue("?max");
        assertEquals(min.intValue(), 1);
        assertEquals(max.intValue(), 3);
    }

    @Test
    public void distance() throws EngineException {
        String q = "select * "
                + "where {"
                + "?c1 a rdfs:Class . ?c2 a rdfs:Class "
                + "filter (?c1 < ?c2)"
                + "bind (xt:similarity(?c1, ?c2) as ?sim) "
                + "filter (?sim > 0.9)"
                + "}";

        QueryProcess exec = QueryProcess.create(getDataManager());
        Mappings map = exec.query(q);
        //System.out.println(map);
        assertEquals(28, map.size());
    }
    
     @Test
    public void relax() throws EngineException {
        String q = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "select more * (xt:similarity() as ?sim) "
                + "where {"
                + "?s a h:Woman ; h:name ?n ;"
                + "h:hasChild ?c ."
                + "?c a h:Woman "
               // + "filter (xt:similarity() > .96)"
                + "}"
                + "order by ?s ?c";

        QueryProcess exec = QueryProcess.create(getDataManager());//getGraph());
        //QueryProcess exec = QueryProcess.create(getGraph());
//        RuleEngine re = RuleEngine.create(getDataManager());
//        re.setProfile(RuleEngine.RDFS_RL);
//        re.process();
        Mappings map = exec.query(q);
        // System.out.println(getGraph().display());
        //System.out.println("res:\n"+map);
        assertEquals(7, map.size());
    }
    
    
     @Test
    public void degree() throws EngineException {
        String q
                = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "prefix i: <http://www.inria.fr/2015/humans-instances#>"
                + "prefix g: <file:///user/corby/home/AADemoNew/human/human.rdf>"
                + "select * (sum(xt:degree(?s)) as ?sum) where {"
                + "?s h:age ?o"
                + "}";

        QueryProcess exec = QueryProcess.create(getDataManager());
        //QueryProcess exec = QueryProcess.create(getGraph());
        Mappings map = exec.query(q);
        //System.out.println(map);
        IDatatype count = map.getValue("?sum");
        assertEquals(64, count.intValue());
    }

    @Test
    public void iterate1() throws EngineException {
        String q
                = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "prefix i: <http://www.inria.fr/2015/humans-instances#>"
                + "prefix g: <file:///user/corby/home/AADemoNew/human/human.rdf>"
                + "select * where {"
                + "bind (xt:size(us:test()) as ?count)"
                + "}"
                + "function us:test() {"
                + "let (select * (aggregate(?o) as ?list) "
                + "where {?s h:age ?o}) {"
                + "return (list)"
                + "}"
                + "}";

        QueryProcess exec = QueryProcess.create(getDataManager());
        Mappings map = exec.query(q);
        //System.out.println(map);
        IDatatype count = map.getValue("?count");
        assertEquals(8, count.intValue());
    }

    @Test
    public void iterate() throws EngineException {
        String q
                = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "prefix i: <http://www.inria.fr/2015/humans-instances#>"
                + "prefix g: <file:///user/corby/home/AADemoNew/human/human.rdf>"
                + "select * where {"
                + "bind (xt:size(us:test()) as ?count)"
                + "}"
                + "function us:test() {"
                + "maplist(xt:self, xt:edges(xt:_joker, h:age, xt:_joker, xt:list(us:, g:)))"
                + "}";

        QueryProcess exec = QueryProcess.create(getDataManager());
        Mappings map = exec.query(q);
        //System.out.println(map);
        IDatatype count = map.getValue("?count");
        assertEquals(8, count.intValue());
    }

    @Test
    public void objects() throws EngineException {
        String q
                = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "prefix i: <http://www.inria.fr/2015/humans-instances#>"
                + "prefix g: <file:///user/corby/home/AADemoNew/human/human.rdf>"
                + "select (sum(?val) as ?sum) where {"
                + "values ?val {unnest(xt:objects(h:age))} "
                + "}";

        QueryProcess exec = QueryProcess.create(getDataManager());
        Mappings map = exec.query(q);
        //System.out.println(map);
        IDatatype sum = map.getValue("?sum");
        assertEquals(409, sum.intValue());
    }
    
    /**
     * Jena data manager  default graph for sparql = union(jena default graph, named graph list)
     * In addition, default graph behaves in sparql as if it were a kg:default named graph
     * hence query on named graph list consider jena default graph object as a kg:default name graph
     * In other words it behaves like corese
     * In addition,  it can handle a native jena data base which 
     * contains a default graph object
     */
     @Test
    public void update3() throws EngineException {
        String insert
                = "insert data { "
                + "graph us:g1 {us:John foaf:knows us:Jack ."
                + "us:John foaf:knows us:Jim} "
                + "graph us:g2 {us:John foaf:knows us:Jim} "
                + "us:John foaf:knows us:James "
                + "}";
        
        
        String q1 = "select * where {graph ?g {?s foaf:knows ?o}}";
        
        String q2 = "select * "
                + "from named us:g1 from named us:g2 "
                + "where {graph ?g {?s foaf:knows ?o}}";
        
        String q21 = "select * "
                + "from named kg:default from named us:g2 "
                + "where {graph ?g {?s foaf:knows ?o}}";
        
        String q22 = "select * "
                + "where {graph us:g1 {?s foaf:knows ?o}}";
        
        String q23 = "select * "
                + "where {graph kg:default {?s foaf:knows ?o}}";
        
        String q3 = "select * where {?s foaf:knows ?o}";
        
        String q4 = "select * from us:g1 from us:g2 "
                  + "where {?s foaf:knows ?o}";
        

        QueryProcess exec = QueryProcess.create(getDataManager());
        //QueryProcess exec = QueryProcess.create(getGraph());

         exec.query(insert);
         Mappings map = exec.query(q1);
         assertEquals(4, map.size());
         trace("graph ?g:\n" + map);
         map = exec.query(q2);
         assertEquals(3, map.size());
         trace("from named graph ?g\n" + map);
         map = exec.query(q21);
         assertEquals(2, map.size());
         trace("from named kg:default us:g2 graph ?g\n" + map);
         map = exec.query(q22);
         assertEquals(2, map.size());
         trace("graph us:g1\n" + map);
         map = exec.query(q23);
         assertEquals(1, map.size());
         trace("graph kg:default\n" + map);
         map = exec.query(q3);
         assertEquals(3, map.size());
         trace("default graph:\n" + map);
         map = exec.query(q4);
         assertEquals(2, map.size());
         trace("from:\n" + map);        
//assertEquals(1, map.size());
    }
    
    void trace(String mes) {
        //System.out.println(mes);
    }
    
    @Test
    public void update2() throws EngineException {
        String insert
                = "insert data { graph us:test {us:John foaf:name 'John'} }";
        String delete
                = "delete {"
                + "graph ?g {?s foaf:name ?o}"
                + "}"
                + "where {"
                + "graph ?g {?s foaf:name ?o}"
                + "}";
        String q = "select * where {graph ?g {?s foaf:name ?o}}";
        String q2 = "select * where {?s foaf:name ?o}";

        QueryProcess exec = QueryProcess.create(getDataManager());

        exec.query(insert);
        Mappings map = exec.query(q2);
        //System.out.println("map:\n" + map);
        assertEquals(1, map.size());

        exec.query(delete);
        map = exec.query(q);
        //System.out.println("map:\n" + map);
        assertEquals(0, map.size());
    }

    @Test
    public void update() throws EngineException {
        String insert
                = "insert {}"
                + "where {"
                + "bind (xt:insert(us:test, us:John, foaf:name, 'John') as ?e)"
                + "}";
        String delete
                = "delete {}"
                + "where {"
                + "bind (xt:delete(us:test, us:John, foaf:name, 'John') as ?e)"
                + "}";
        String q = "select * where {graph ?g {?s foaf:name ?o}}";
        String q2 = "select * where {?s foaf:name ?o}";

        QueryProcess exec = QueryProcess.create(getDataManager());

        exec.query(insert);
        Mappings map = exec.query(q);
        //System.out.println("map:\n" + map);
        assertEquals(1, map.size());

        exec.query(delete);
        map = exec.query(q);
        //System.out.println("map:\n" + map);
        assertEquals(0, map.size());
    }

    @Test
    public void transformer() throws LoadException, EngineException {
        Transformer t = Transformer.createWE(QueryProcess.create(getDataManager()), Transformer.TURTLE);
        String res = t.transform();
//        System.out.println(res);
//        System.out.println(res.length());
        assertEquals(true, res != null);
        assertEquals(true, res.length() > 10000);
    }

    //@Test
    public void ruleengine() throws LoadException, EngineException {
        RuleEngine re = RuleEngine.create(Graph.create(), getDataManager());
        re.setProfile(RuleEngine.Profile.OWLRL);
        //re.setTrace(true);
        re.process();

        QueryProcess exec = QueryProcess.create(getDataManager());
        String q = "select * from kg:rule where {?s ?p ?o}";
        Mappings map = exec.query(q);
        //System.out.println(map);
        assertEquals(120, map.size());
    }

    static DataManager getDataManager() {
        return dataManager;
    }

    static void setDataManager(DataManager m) {
        dataManager = m;
    }

    public static Graph getGraph() {
        return graph;
    }

    public static void setGraph(Graph aGraph) {
        graph = aGraph;
    }

}
