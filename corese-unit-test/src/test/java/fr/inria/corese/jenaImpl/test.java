package fr.inria.corese.jenaImpl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDF;
import org.junit.Before;
import org.junit.Test;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.NodeImpl;
import fr.inria.corese.core.edge.EdgeImpl;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.logic.OWLProfile;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.storage.jenatdb1.ConvertJenaCorese;
import fr.inria.corese.storage.jenatdb1.JenaDataManager;

public class test {

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
        jt1dm = new JenaDataManager("/user/rceres/home/Downloads/test");

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

        jt1dm.insert(this.statement_0_corese);
        jt1dm.insert(this.statement_bonus_corese);

        System.out.println(jt1dm.graphSize());

        jt1dm.move(this.default_context_corese, this.context2_corese, false);
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

}
