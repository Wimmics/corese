package test.jsonld;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.corese.kgengine.QueryResults;
import fr.inria.corese.kgram.api.core.Entity;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.logic.Entailment;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.LoadException;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Test;
import test.rdfa.RdfaTestHelper;

/**
 * JSON-LD parser testing The test focuses on testing query data from resolved
 * json-ld document using corese, not on testing the parser itself
 *
 * @author Fuqi Song wimmics inria w3s
 * @date 12 Feb. 2014 new
 */
public class JsonldParserTest {

    private static Graph graph;
    private static final String ROOT = RdfaTestHelper.class.getClassLoader().getResource("data").getPath() + "/jsonld/";

    private static final String NAMED_GRAPH_TEST_FILE = "toRdf-0115-in.jsonld";

    @Test
    public void testLoadJsonldFileFromFile() {
        String file = ROOT + "compact-0005-in.jsonld";
        try {
            loadJsonld(file);
        } catch (LoadException ex) {
            fail();
        }
    }

    @Test
    public void testLoadJsonldFileFromUrl() {
        String file = "http://json-ld.org/test-suite/tests/compact-0005-in.jsonld";
        try {
            loadJsonld(file);
        } catch (LoadException ex) {
            fail();
        }
    }

    @Test
    public void testLoadJsonldFileNagative() {
        String file = ROOT + "this-file-does-not-exist.jsonld";
        try {
            loadJsonld(file);
            fail("no exception thrown.");
        } catch (LoadException e) {
            Assert.assertTrue(e instanceof LoadException);
        }
    }

    @Test
    public void testNamedGraph() throws LoadException {
        loadJsonld(ROOT + NAMED_GRAPH_TEST_FILE);
        int counter = 0;
        String graphName = "http://example/g";
        boolean containedNamedGraph = false;
        boolean containedDefaultGraph = false;

        for (Node n : graph.getGraphNodes()) {

            String label = n.getLabel();
            if (label.equals(graphName)) {
                containedNamedGraph = true;
            }

            if (label.equals(Entailment.DEFAULT)) {
                containedDefaultGraph = true;
            }

            counter++;
        }

        Assert.assertEquals("number of graphs:", 3, counter);
        Assert.assertTrue("contained named graph [" + graphName + "]", containedNamedGraph);
        Assert.assertTrue("contained default graph", containedDefaultGraph);
    }

    @Test
    public void testBlankNode() throws LoadException, EngineException {
        //number, names...
        loadJsonld(ROOT + "normalize-0020-in.jsonld");
        int counter = 0;
        for (Entity n : graph.getBlankNodes()) {
            if (((Node) n).isBlank()) {
                counter++;
            }
        }
        Assert.assertEquals("number of graphs:", 3, counter);
    }

    @Test
    //also test jsonld map
    public void testLangTag() throws LoadException, EngineException {
        loadJsonld(ROOT + "toRdf-0070-in.jsonld");
        String sparql = "ask {<http://example.com/queen> <http://example.com/vocab/label> "
                + " \"Die Königin\"@de, \"Ihre Majestät\"@de, \"The Queen\"@en.}";

        Assert.assertTrue("test languages tags", askModel(sparql));
    }

    @Test
    public void testDatatype() throws LoadException, EngineException {
        loadJsonld(ROOT + "toRdf-0006-in.jsonld");
        String sparql = "ask {<http://greggkellogg.net/foaf#me> <http://purl.org/dc/terms/created>"
                + " \"1957-02-27\"^^xsd:date.}";
        Assert.assertTrue("test languages tags", askModel(sparql));
    }

    @Test
    public void testQueryNormal() throws LoadException, EngineException {
        String sparql = "PREFIX ex:<http://example.com/>\n"
                + "PREFIX ex2:<http://example.com/vocab/>\n"
                + "select ?lang \n"
                + "where {ex:queen ex2:label ?lang}";
        loadJsonld(ROOT + "toRdf-0070-in.jsonld");

        QueryProcess exec = QueryProcess.create(graph);
        Mappings map = exec.query(sparql);
        Assert.assertEquals("query test 1", 3, map.size());
    }

    @Test
    public void testQueryFromNamedGraph() throws LoadException, EngineException {

        loadJsonld(ROOT + NAMED_GRAPH_TEST_FILE);
        QueryProcess exec = QueryProcess.create(graph);
        String sparql1 = "PREFIX ex:<http://example/>\n"
                + "select ?x \n"
                + "from ex:g\n"
                + "where { ex:s1 ex:p1 ?x}";

        Mappings map = exec.query(sparql1);

        Assert.assertEquals("query test: from named graph", 1, map.size());
        String sparql2 = "PREFIX ex:<http://example/>\n"
                + "select ?x \n"
                + "from ex:g\n"
                + "where { ex:s2 ex:p2 ?x}";

        map = exec.query(sparql2);

        Assert.assertEquals("query test: from named graph", 0, map.size());
    }

    @Test
    public void testQueryUnion() throws LoadException, EngineException {
        String sparql = "prefix ex: <http://example.org/cars/for-sale#>\n"
                + "prefix gr:<http://purl.org/goodrelations/v1#>\n"
                + "select ?c\n"
                + "where{\n"
                + "ex:tesla gr:hasPriceSpecification ?b. "
                + "?b gr:hasCurrencyValue ?c.\n"
                + "}";
        loadJsonld(ROOT + "product.jsonld");

        QueryProcess exec = QueryProcess.create(graph);
        Mappings map = exec.query(sparql);
        Assert.assertEquals("query test 2", 1, map.size());
    }

    //execute Sparql statement using ASK model
    private boolean askModel(String sparql) throws EngineException {

        QueryProcess exec = QueryProcess.create(graph);
        Mappings map = exec.query(sparql);
        QueryResults res = QueryResults.create(map);

        return (res.isAsk() && map.size() != 0);
    }

    //load JSON-LD document to corese graph
    private void loadJsonld(String fileUri) throws LoadException {
        graph = Graph.create(true);
        Load ld = Load.create(graph);
        ld.loadWE(fileUri);
    }

    @Test
    public void testLoadExpanded() {
        String file = ROOT + "product_expanded.jsonld";
        try {
            loadJsonld(file);
        } catch (LoadException ex) {
            fail();
        }
    }

    @Test
    public void testLoadFlatened() {
        String file = ROOT + "product_flattened.jsonld";
        try {
            loadJsonld(file);
        } catch (LoadException ex) {
            fail();
        }
    }

    @Test
    public void testLoadFramed() {
        String file = ROOT + "product_framed.jsonld";
        try {
            loadJsonld(file);
        } catch (LoadException ex) {
            fail();
        }
    }
}
