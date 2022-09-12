package fr.inria.corese.jenaImpl;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.NodeImpl;
import fr.inria.corese.core.api.DataManager;
import fr.inria.corese.core.edge.EdgeImpl;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.RuleLoad;
import fr.inria.corese.core.logic.OWLProfile;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.rdf4j.Rdf4jDataManager;
import fr.inria.corese.rdf4jImpl.dataManager.Rdf4jDataManagerTest;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.storage.jenatdb1.ConvertJenaCorese;
import fr.inria.corese.storage.jenatdb1.JenaDataManager;

public class test2 {

    // Subjects Corese
    private Node edith_piaf_node_corese;
    private Node george_brassens_node_corese;

    // Properties Corese
    private Node isa_property_corese;

    // Objects Corese
    private Node singer_node_corese;

    // Contexts Corese
    private Node context2_corese;
    private Node default_context_corese;

    // Statements Corese
    private Edge statement_0_corese;
    private Edge statement_bonus_corese;

    // Dataset
    private Dataset dataset;

    @Before
    public void init() {

        this.default_context_corese = NodeImpl.create(DatatypeMap.createResource(ExpType.DEFAULT_GRAPH));

        // Build jenaGraph
        String ex = "http://example.org/";

        // statement zero
        Resource edithPiafNode = ResourceFactory.createResource(ex + "EdithPiaf");
        Property isaProperty = RDF.type;
        Resource singerNode = ResourceFactory.createResource(ex + "Singer");
        Statement statement_0 = ResourceFactory.createStatement(edithPiafNode, isaProperty, singerNode);

        // first, second and third statements
        Property firstNameProperty = ResourceFactory.createProperty(ex, "firstName");
        Literal edithLiteral = ResourceFactory.createStringLiteral("Édith");
        Statement statement_1 = ResourceFactory.createStatement(edithPiafNode, firstNameProperty, edithLiteral);

        // bonus statement
        Resource georgeBrassensNode = ResourceFactory.createResource(ex + "GeorgeBrassens");

        /////////////////
        // Build graph //
        /////////////////
        this.dataset = DatasetFactory.create();

        Model model_default = dataset.getDefaultModel();
        model_default.add(statement_0);

        String context1 = ex + "context1";
        Model model_context1 = dataset.getNamedModel(context1);
        model_context1.add(statement_1);

        String context2 = ex + "context2";
        Model model_context2 = dataset.getNamedModel(context2);
        model_context2.add(statement_1);

        String context3 = ex + "context3";
        Model model_context3 = dataset.getNamedModel(context3);
        model_context3.add(statement_1);

        /////////////////////////////////////////////////
        // Convert from Jena to Corese format and Save //
        /////////////////////////////////////////////////

        // Subjects Corese
        this.edith_piaf_node_corese = ConvertJenaCorese.JenaNodeToCoreseNode(edithPiafNode.asNode());
        this.george_brassens_node_corese = ConvertJenaCorese.JenaNodeToCoreseNode(georgeBrassensNode.asNode());

        // Predicates Corese
        this.isa_property_corese = ConvertJenaCorese.JenaNodeToCoreseNode(isaProperty.asNode());

        // Predicates Jena
        firstNameProperty.asNode();

        // Objects Corese
        this.singer_node_corese = ConvertJenaCorese.JenaNodeToCoreseNode(singerNode.asNode());

        // Objects Jena
        edithLiteral.asNode();

        // Contexts Corese
        this.context2_corese = NodeImpl.create(DatatypeMap.createResource(context2));

        // Statements Corese
        this.statement_0_corese = EdgeImpl.create(this.default_context_corese, this.edith_piaf_node_corese,
                this.isa_property_corese,
                this.singer_node_corese);
        this.statement_bonus_corese = EdgeImpl.create(this.default_context_corese,
                this.george_brassens_node_corese,
                this.isa_property_corese, this.singer_node_corese);

    }

    @Test
    public void printGraphLocal() {
        JenaDataManager jt1dm;
        jt1dm = new JenaDataManager("/user/rceres/home/Downloads/test-jena/bd/");

        OutputStream object;
        try {
            object = new FileOutputStream("/user/rceres/home/Downloads/result.ttl");
            RDFDataMgr.write(object, jt1dm.getDataset(), RDFFormat.TRIG);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        jt1dm.close();

    }

    @Test
    public void buildGraphLocal() {
        JenaDataManager jt1dm;

        jt1dm = new JenaDataManager("/user/rceres/home/Downloads/test");

        jt1dm.startWriteTransaction();
        jt1dm.insert(this.statement_0_corese);
        jt1dm.insert(this.statement_bonus_corese);
        jt1dm.endWriteTransaction();

        jt1dm.startReadTransaction();
        System.out.println(jt1dm.graphSize());
        jt1dm.endReadTransaction();
        
        jt1dm.startWriteTransaction();
        jt1dm.move(this.default_context_corese, this.context2_corese, false);
        jt1dm.endWriteTransaction();

        jt1dm.close();
    }

    @Test
    public void sparql() throws EngineException {
        JenaDataManager jt1dm;
        jt1dm = new JenaDataManager("/user/rceres/home/Downloads/test");

        // Sparql query
        QueryProcess exec = QueryProcess.create(jt1dm);
        Mappings map = exec.query("select * where { ?s a <http://example.org/Singer> }");
        exec.query(
                "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                        + "insert {?p a foaf:Person }"
                        + "where { ?p a <http://example.org/Singer> }");

        // Print result
        for (Mapping m : map) {
            System.out.println(m);
        }
    }

    @Test
    public void close() throws EngineException {
        try (JenaDataManager jt1dm = new JenaDataManager("/user/rceres/home/Downloads/test")) {
            System.exit(1);
        }
    }

    @Test
    public void close2() throws EngineException {
        JenaDataManager jt1dm = new JenaDataManager("/user/rceres/home/Downloads/test");
        jt1dm.close();
    }

    @Test
    public void christopher() {

        // Create a new empty Graph
        Graph graph = Graph.create();

        // Create loader and parse file
        Load ld = Load.create(graph);
        try {
            ld.parse("/user/rceres/home/Downloads/turtle.ttl");
        } catch (LoadException e) {
            e.printStackTrace();
        }

        OWLProfile tc = new OWLProfile(graph);
        new OWLProfile(graph);
        try {
            tc.process(OWLProfile.OWL_RL);
        } catch (EngineException e) {
            e.printStackTrace();
        }
        tc.getMessage();

        // OWLProfile.OWL_RL
        // OWLProfile.OWL_QL
        // OWLProfile.OWL_EL
        // OWLProfile.OWL_TC
    }

    @Test
    public void parallele() throws EngineException {

        JenaDataManager jt1dm1 = new JenaDataManager("/user/rceres/home/Downloads/test");

        JenaDataManager jt1dm2 = new JenaDataManager("/user/rceres/home/Downloads/test");

        // Sparql query
        QueryProcess exec1 = QueryProcess.create(jt1dm1);
        QueryProcess exec2 = QueryProcess.create(jt1dm2);
        exec1.query("select * where { ?s a <http://example.org/Singer> }");
        exec2.query(
                "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                        + "insert {?p a foaf:Person }"
                        + "where { ?p a <http://example.org/Singer> }");
        exec2.query("select * where { ?s a <http://example.org/Singer> }");
        exec1.query(
                "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                        + "insert {?p a foaf:Person }"
                        + "where { ?p a <http://example.org/Singer> }");
    }

    @Test
    public void testPerformanceSPO() throws EngineException {
        String STORAGE_PATH = "/user/rceres/home/Documents/Corese/corese-learning/test/benchmark/storage/CoreseTdb";
        String query = "SELECT (COUNT(*) as ?cnt)"
                + "WHERE {"
                + "?s ?p ?o"
                + "}";

        JenaDataManager dm = new JenaDataManager(STORAGE_PATH);

        // dm.getDataset().begin(ReadWrite.READ);

        QueryProcess exec = QueryProcess.create(dm);
        Mappings map = exec.query(query);
        System.out.println(map.iterator().next().getValue("?cnt").intValue());
        // dm.getDataset().commit();
    }

    @Test
    public void errorSPARQL() throws EngineException {
        String STORAGE_PATH = "/user/rceres/home/Documents/Corese/corese-learning/test/benchmark/storage/DBPedia TDB1";
        JenaDataManager dm = new JenaDataManager(STORAGE_PATH);

        String query = "PREFIX dbo:<http://dbpedia.org/ontology/>"
                + " SELECT (COUNT (*) AS ?cnt) WHERE{"
                // + " ?s ?p ?o"
                + " ?s dbo:country <http://fr.dbpedia.org/resource/France>"
                + " ?s a dbo:Museum."
                + " ?s a ?p."
                + " }";

        QueryProcess exec = QueryProcess.create(dm);
        Mappings map = exec.query(query);

        System.out.println(map.iterator().next().getValue("?cnt").intValue());

    }

    @Test
    public void load() throws EngineException {

        //////////////////////////
        // Create a DataManager //
        //////////////////////////
        JenaDataManager dataManager = new JenaDataManager("/user/rceres/home/Downloads/test");
        QueryProcess exec = QueryProcess.create(dataManager);

        /////////////////
        // Insert data //
        /////////////////
        String insertQuery = "PREFIX dcterms: <http://purl.org/dc/terms/> "
                + "INSERT DATA { "
                + "    GRAPH <http://example/shelf_A> {"
                + "        <http://example/author> dcterms:name \"author\" ."
                + "        <http://example/book> dcterms:title \"book\" ;"
                + "                              dcterms:author <http://example/author> ."
                + "    }  "
                + "} ";
        exec.query(insertQuery);

        /////////////////
        // Query graph //
        /////////////////

        String querySPO = "SELECT * "
                + "WHERE { "
                + "?s ?p ?o. "
                + "}";

        Mappings map = exec.query(querySPO);

        // Print results
        for (Mapping m : map) {
            System.out.println(m);
        }

    }

    @Test
    public void filter() throws EngineException {

        //////////////////////////
        // Create a DataManager //
        //////////////////////////
        JenaDataManager dataManager = new JenaDataManager(
                "/user/rceres/home/Documents/Corese/corese-learning/test/benchmark/storage/DBPedia TDB1");
        QueryProcess exec = QueryProcess.create(dataManager);

        /////////////////
        // Query graph //
        /////////////////
        String query = ""
                + "PREFIX dbo: <http://dbpedia.org/ontology/> "
                + "PREFIX dbpedia-fr: <http://fr.dbpedia.org/resource/> "
                + "SELECT  DISTINCT ?o  ?y WHERE { "
                // + " ?o a dbo:Artwork. "
                + "    ?o dbo:creationYear ?y. "
                + "    FILTER( YEAR(?y) < 1900) "
                // + " FILTER( YEAR(?y) < 1900 && YEAR(?y) > 1850) "
                // + " ?o dbo:museum dbpedia-fr:Musée_du_Louvre. "
                + "} LIMIT 1";

        Mappings map = exec.query(query);

        // Print results
        for (Mapping m : map) {
            System.out.println(m);
        }

    }

    @Test
    public void demoEmailStorage() throws EngineException {

        //////////////////////////
        // Create a DataManager //
        //////////////////////////
        JenaDataManager dataManager = new JenaDataManager("/user/rceres/home/Downloads/test");
        QueryProcess exec = QueryProcess.create(dataManager);

        /////////////////
        // Insert data //
        /////////////////
        String insertQuery = "PREFIX dcterms: <http://purl.org/dc/terms/> "
                + "INSERT DATA { "
                + "    GRAPH <http://example/shelf_A> {"
                + "        <http://example/author> dcterms:name \"author\" ."
                + "        <http://example/book> dcterms:title \"book\" ;"
                + "                              dcterms:author <http://example/author> ."
                + "    }  "
                + "} ";
        exec.query(insertQuery);

        /////////////////
        // Query graph //
        /////////////////

        String querySPO = "SELECT * "
                + "WHERE { "
                + "?s ?p ?o. "
                + "}";

        Mappings map = exec.query(querySPO);

        // Print results
        for (Mapping m : map) {
            System.out.println(m);
        }

    }

    @Test
    public void rulesJenaDataset() throws LoadException, EngineException {

        /////////////////
        // DataManager //
        /////////////////

        // Create
        JenaDataManager dataManager = new JenaDataManager("/user/rceres/home/Downloads/test");

        // Load
        Load dataLoader = Load.create(new Graph());
        dataLoader.setDataManager(dataManager);
        dataLoader.parse("/user/rceres/home/Documents/Corese/corese-learning/example-file/rdf/forRule.ttl");

        ////////////////
        // RuleEngine //
        ////////////////

        // Create
        RuleEngine ruleEngine = RuleEngine.create(dataManager);

        // Load
        RuleLoad ruleLoad = RuleLoad.create(ruleEngine);
        ruleLoad.parse("/user/rceres/home/Documents/Corese/corese-learning/example-file/rule/sym-trans.rul");

        /////////////
        // Process //
        /////////////
        boolean result = ruleEngine.process();
        System.out.println(result);
    }

    @Test
    public void ruleDemo() throws LoadException, EngineException {

        // Create and load data in a graph
        Graph dataGraph = Graph.create();
        Load dataLoader = Load.create(dataGraph);
        dataLoader.parse("/user/rceres/home/Documents/Corese/corese-learning/example-file/rdf/forRule.ttl");

        // Create and load rules into a rules engine
        RuleEngine ruleEngine = RuleEngine.create(dataGraph);
        RuleLoad ruleLoader = RuleLoad.create(ruleEngine);
        ruleLoader.parse("/user/rceres/home/Documents/Corese/corese-learning/example-file/rule/sym-trans.rul");

        // Apply rules on graph
        ruleEngine.process();
    }

    @Test
    public void testTransaction() throws LoadException {
        DataManager dataManager = new JenaDataManager("/user/rceres/home/Downloads/test");

        // Load
        Load dataLoader = Load.create(new Graph());
        dataLoader.setDataManager(dataManager);
        dataLoader.parse("/user/rceres/home/Documents/Corese/corese-learning/example-file/rdf/forRule.ttl");

        assertEquals(false, dataManager.isInTransaction());
        assertEquals(false, dataManager.isInReadTransaction());
        assertEquals(false, dataManager.isInWriteTransaction());
        dataManager.startReadTransaction();
        assertEquals(true, dataManager.isInTransaction());
        assertEquals(true, dataManager.isInReadTransaction());
        assertEquals(false, dataManager.isInWriteTransaction());
        dataManager.endReadTransaction();
        dataManager.startWriteTransaction();
        assertEquals(true, dataManager.isInTransaction());
        assertEquals(false, dataManager.isInReadTransaction());
        assertEquals(true, dataManager.isInWriteTransaction());
        dataManager.abortTransaction();
        dataManager.endReadTransaction();
        assertEquals(false, dataManager.isInTransaction());
        assertEquals(false, dataManager.isInReadTransaction());
        assertEquals(false, dataManager.isInWriteTransaction());
    }

    @Test
    public void testTransactionLock() {
        DataManager dataManager = new JenaDataManager("/user/rceres/home/Downloads/test");

        System.out.println(dataManager.isInTransaction());
        System.out.println(dataManager.isInReadTransaction());
        System.out.println(dataManager.isInWriteTransaction());
    }

    @Test
    public void thread() {
        DataManager dataManager1 = new JenaDataManager("/user/rceres/home/Downloads/test");
        DataManager dataManager2 = new JenaDataManager("/user/rceres/home/Downloads/test");

        assertEquals(false, dataManager1.isInTransaction());
        assertEquals(false, dataManager1.isInReadTransaction());
        assertEquals(false, dataManager1.isInWriteTransaction());
        assertEquals(false, dataManager2.isInTransaction());
        assertEquals(false, dataManager2.isInReadTransaction());
        assertEquals(false, dataManager2.isInWriteTransaction());
        dataManager1.startReadTransaction();
        assertEquals(true, dataManager1.isInTransaction());
        assertEquals(true, dataManager1.isInReadTransaction());
        assertEquals(false, dataManager1.isInWriteTransaction());
        assertEquals(true, dataManager2.isInTransaction());
        assertEquals(true, dataManager2.isInReadTransaction());
        assertEquals(false, dataManager2.isInWriteTransaction());
        dataManager1.endReadTransaction();
        dataManager1.startWriteTransaction();
        assertEquals(true, dataManager1.isInTransaction());
        assertEquals(false, dataManager1.isInReadTransaction());
        assertEquals(true, dataManager1.isInWriteTransaction());
        dataManager1.abortTransaction();
        dataManager1.endReadTransaction();
        assertEquals(false, dataManager1.isInTransaction());
        assertEquals(false, dataManager1.isInReadTransaction());
        assertEquals(false, dataManager1.isInWriteTransaction());

        Graph graph;
    }

    @Test
    public void demoRDF4JDataManager() throws EngineException {

        //////////////////////////
        // Create a DataManager //
        //////////////////////////
        DataManager dataManager = new Rdf4jDataManager(new TreeModel());
        QueryProcess exec = QueryProcess.create(dataManager);

        /////////////////
        // Insert data //
        /////////////////
        String insertQuery = "PREFIX dcterms: <http://purl.org/dc/terms/> "
                + "INSERT DATA { "
                + "    GRAPH <http://example/shelf_A> {"
                + "        <http://example/author> dcterms:name \"author\" ."
                + "        <http://example/book> dcterms:title \"book\" ;"
                + "                              dcterms:author <http://example/author> ."
                + "    }  "
                + "} ";
        exec.query(insertQuery);

        /////////////////
        // Query graph //
        /////////////////

        String querySPO = "SELECT * "
                + "WHERE { "
                + "?s ?p ?o. "
                + "}";

        Mappings map = exec.query(querySPO);

        // Print results
        for (Mapping m : map) {
            System.out.println(m);
        }

    }
    
    @Test
    public void demoRDF4JDataManager2() throws LoadException, EngineException {

        /////////////////
        // DataManager //
        /////////////////

        // Create
        DataManager dataManager = new Rdf4jDataManager(new TreeModel());

        // Load
        Load dataLoader = Load.create(new Graph());
        dataLoader.setDataManager(dataManager);
        dataLoader.parse("/user/rceres/home/Documents/Corese/corese-learning/example-file/rdf/forRule.ttl");

        ////////////////
        // RuleEngine //
        ////////////////

        // Create
        RuleEngine ruleEngine = RuleEngine.create(dataManager);

        // Load
        RuleLoad ruleLoad = RuleLoad.create(ruleEngine);
        ruleLoad.parse("/user/rceres/home/Documents/Corese/corese-learning/example-file/rule/sym-trans.rul");

        /////////////
        // Process //
        /////////////
        boolean result = ruleEngine.process();
        System.out.println(result);
    }

    @Test
    public void sparql2() throws EngineException {
        JenaDataManager jt1dm = new JenaDataManager("/user/rceres/home/Downloads/test-jena/bd/");

        // String queryString = "SELECT * WHERE { ?s ?p ?o }";
        String queryString = "PREFIX ex:<http://example.org/> SELECT DISTINCT * WHERE {?s ?p ?o}";

        // Sparql query
        QueryProcess exec = QueryProcess.create(jt1dm);
        Mappings map = exec.query(queryString);

        // Print result
        for (Mapping m : map) {
            System.out.println(m);
        }
    }

    @Test
    public void sparql3() throws EngineException {
        JenaDataManager jt1dm = new JenaDataManager("/user/rceres/home/Downloads/test-jena/bd/");

        // String queryString = "SELECT * WHERE { ?s ?p ?o }";
        String queryString = "PREFIX ex:<http://example.org/> SELECT * FROM ex:context1 WHERE { GRAPH ?g {?s ?p ?o}}";

        // Sparql query
        QueryProcess exec = QueryProcess.create(jt1dm);
        Mappings map = exec.query(queryString);

        // Print result
        for (Mapping m : map) {
            System.out.println(m);
        }
    }

    @Test
    public void loadFromFileTestJena() throws LoadException, EngineException {

        /////////////////
        // DataManager //
        /////////////////

        // Create
        JenaDataManager dataManager = new JenaDataManager("/user/rceres/home/Downloads/test-jena/bd/");

        // Load
        Load dataLoader = Load.create(new Graph());
        dataLoader.setDataManager(dataManager);
        dataLoader.parse("/user/rceres/home/Downloads/edithPiafAll.ttl");
    }

    @Test
    public void InterWithDatasetTestJena() throws LoadException, EngineException {

        /////////////////
        // DataManager //
        /////////////////

        // Create
        JenaDataManager dataManager = new JenaDataManager("/user/rceres/home/Downloads/test-jena/bd/");

        // Define the namespace ex
        String ex = "http://example.org/";

        // Create and add IRIs to Graph
        
        Node edithPiafIRI = DatatypeMap.createResource(ex + "EdithPiaf").getNode();
        Node singerIRI = DatatypeMap.createResource(ex + "Singer").getNode();

        // Create and add properties to Graph
        Node rdfTypeProperty = DatatypeMap.createResource(RDF.type.toString()).getNode();

        // Contexts
        Node default_context_corese = NodeImpl.create(DatatypeMap.createResource(ExpType.DEFAULT_GRAPH));
        ArrayList<Node> contexts = new ArrayList<>();
        contexts.add(default_context_corese);

        // Add
        dataManager.startWriteTransaction();
        dataManager.insert(edithPiafIRI, rdfTypeProperty, singerIRI, contexts);
        dataManager.endWriteTransaction();
    }

    @Test
    public void InterWithRequestTestJena() throws LoadException, EngineException {

        /////////////////
        // DataManager //
        /////////////////

        // Create
        JenaDataManager dataManager = new JenaDataManager("/user/rceres/home/Downloads/test-jena/bd/");

        QueryProcess exec = QueryProcess.create(dataManager);

        String insertQuery = "PREFIX dcterms: <http://purl.org/dc/terms/> "
                + "INSERT DATA { "
                + "    <http://example/author> dcterms:name \"author\" ."
                + "    <http://example/book> dcterms:title \"book\" ;"
                + "                          dcterms:author <http://example/author> ."
                + "} ";
        exec.query(insertQuery);
    }

    @Test
    public void testAPIBigGraph() {

        String ex = "http://example.org/";
        Resource edithPiafNode = ResourceFactory.createResource(ex + "EdithPiaf");

        Function<Triple, Edge> convertIteratorQuadToEdge = new Function<Triple, Edge>() {
            @Override
            public Edge apply(Triple triple) {
                Quad quad = new Quad(
                        Quad.defaultGraphIRI, triple.getSubject(), triple.getPredicate(), triple.getObject());
                return ConvertJenaCorese.quadToEdge(quad);
            }
        };
        
        JenaDataManager dbPediaDataManager = new JenaDataManager(
                "/user/rceres/home/Documents/Corese/corese-learning/test/benchmark/storage/DBPedia TDB1/");
        Dataset tdb1 = dbPediaDataManager.getDataset();

        System.out.println("[named]         Size of named models: " + tdb1.getUnionModel().size());
        System.out.println("[default]      Size of default model: " + tdb1.getDefaultModel().size());

        org.apache.jena.graph.Graph union = union(tdb1.asDatasetGraph().getDefaultGraph(), tdb1.asDatasetGraph().getUnionGraph());

        ExtendedIterator<Triple> res = union.find(null, null, null);
        // ExtendedIterator<Triple> res = union.find(edithPiafNode.asNode(), null, null);

        Iterator<Edge> edges = Iterators.transform(res, convertIteratorQuadToEdge);

        int nb = 0;
        while(edges.hasNext() && nb < 10) {
            System.out.println(edges.next());
            nb++;
        }

        dbPediaDataManager.close();
    }

    @Test
    public void testAPINoDouble() {

        // String ex = "http://example.org/";
        // Resource edithPiafNode = ResourceFactory.createResource(ex + "EdithPiaf");

        Function<Triple, Edge> convertIteratorQuadToEdge = new Function<Triple, Edge>() {
            @Override
            public Edge apply(Triple triple) {
                Quad quad = new Quad(
                        Quad.defaultGraphIRI, triple.getSubject(), triple.getPredicate(), triple.getObject());
                return ConvertJenaCorese.quadToEdge(quad);
            }
        };

        JenaDataManager dbPediaDataManager = new JenaDataManager("/user/rceres/home/Downloads/test-jena/bd/");
        Dataset tdb1 = dbPediaDataManager.getDataset();

        System.out.println("[named]         Size of named models: " + tdb1.getUnionModel().size());
        System.out.println("[default]      Size of default model: " + tdb1.getDefaultModel().size());

        org.apache.jena.graph.Graph union = union(tdb1.asDatasetGraph().getUnionGraph(), tdb1.asDatasetGraph().getUnionGraph(), 
                tdb1.asDatasetGraph().getDefaultGraph());

        ExtendedIterator<Triple> res = union.find(null, null, null);

        Iterator<Edge> edges = Iterators.transform(res, convertIteratorQuadToEdge);

        while (edges.hasNext()) {
            System.out.println(edges.next());
        }

        dbPediaDataManager.close();
    }

    @Test
    public void mergePiaf() {

        JenaDataManager edithPiafDataManager = new JenaDataManager("/user/rceres/home/Downloads/test-jena/bd/");
        JenaDataManager dbPediaDataManager = new JenaDataManager(
                "/user/rceres/home/Documents/Corese/corese-learning/test/benchmark/storage/DBPedia TDB1/");

        Dataset edithPiaf = edithPiafDataManager.getDataset();
        Dataset tdb1 = dbPediaDataManager.getDataset();

        tdb1.addNamedModel("test", edithPiaf.getDefaultModel());

        edithPiafDataManager.close();
        dbPediaDataManager.close();
    }

    private org.apache.jena.graph.Graph union(org.apache.jena.graph.Graph... graphs) {
        org.apache.jena.graph.Graph result = org.apache.jena.graph.Graph.emptyGraph;

        for (org.apache.jena.graph.Graph graph : graphs) {
            result = new Union(result, graph);
        }

        return result;
    }
    
}
