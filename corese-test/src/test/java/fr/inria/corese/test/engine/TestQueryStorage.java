package fr.inria.corese.test.engine;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.api.DataManager;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.sparql.exceptions.EngineException;
//import fr.inria.corese.storage.jenatdb1.JenaDataManager;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test data manager
 * ldscript 
 * transformer
 * rule engine
 */
public class TestQueryStorage {

    private static final String STORAGE = "/user/corby/home/AADemoNew/storage/storagetest";
    private static final String STORAGE2 = "/user/corby/home/AADemoNew/storage/atest";
    private static final String DATASET = "/user/corby/home/AADemoNew/human/human.rdf";
    private static final String SHACL = "/user/corby/home/AADemoNew/human/shape1.ttl";
    private static final String SCHEMA = "/user/corby/home/AADemoNew/human/human.rdfs";
    private static final String DISTANCE = "/user/corby/home/AADemoNew/human/distance.ttl";

    static DataManager dataManager;

//    public static void main(String[] args) throws LoadException, EngineException {
//        new TestQueryStorage().test();
//    }
    
    @BeforeClass
    public static void start() throws LoadException {
        //init();
    }

    static void init() throws LoadException {
//        DataManager man = new JenaDataManager(STORAGE);
//        man.getCreateMetadataManager();
//        Graph g = Graph.create();
//        Load ld = Load.create(g, man);
//        ld.parse(SCHEMA);
//        ld.parse(DATASET);
//           setDataManager(man);
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

}
