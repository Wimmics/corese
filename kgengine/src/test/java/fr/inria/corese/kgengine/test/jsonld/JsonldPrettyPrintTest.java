package fr.inria.corese.kgengine.test.jsonld;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.LoadException;
import fr.inria.corese.kgtool.print.JSONLDFormat;
import java.io.File;
import java.io.IOException;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Test;
import fr.inria.corese.kgengine.test.rdfa.RdfaTestHelper;

/**
 * Test JSONLDFormat.java
 *
 * @author Fuqi Song wimmics inria i3s
 */
public class JsonldPrettyPrintTest {

    private Graph g;
    private static final String ROOT = RdfaTestHelper.class.getClassLoader().getResource("data").getPath() + "/jsonld/";

    void loadSource(String src) {
        g = Graph.create();

        // create a loader
        Load ld = Load.create(g);

        ld.load(src);
    }

    /**
     *
     * @param src file that needs to validate
     * @param compare true: compare the two graphs; false: just check if the
     * output can be loaded by corese
     * @return result true|false
     * @throws IOException
     */
    boolean validate(String src, boolean compare) throws IOException {
        // 1. load json-ld file to corese
        loadSource(src);

        // 2. process graph into json-ld format
        //    and write to file
        JSONLDFormat jf = JSONLDFormat.create(g);
        String temp = "temp.jsonld";
        jf.write(temp);

        // 3. read the newly created file to corese graph
        Graph gg = Graph.create();
        Load ld = Load.create(gg);
        try {
            ld.loadWE(temp);
        } catch (LoadException ex) {
            System.out.println(ex.toString());
            return false;
        }

        // 3.0 delete the temporary file 
        File f = new File(temp);
        f.delete();
//        System.out.println(src);
//        System.out.println(g + "\n" + g.toString2());
//        for (Entity node : g.getAllNodes()) {
//            System.out.println(node);
//        }
//        System.out.println("======");
//        for (Entity node : g.getEdges()) {
//            System.out.println(node);
//        }
//
//        System.out.println("---------------\n" + gg + "\n" + gg.toString2());
//        for (Entity node : gg.getAllNodes()) {
//            System.out.println(node);
//        }
//        System.out.println("======");
//        for (Entity node : gg.getEdges()) {
//            System.out.println(node);
//        }
        if (compare) {
            //4. compare the two graphs\
            QueryProcess qp = QueryProcess.create(gg);
            Mappings map = qp.query(g);

            return map.size() > 0;
        } else {
            return true;
        }
    }

    @Test
    public void testContext() {
        String src = "http://json-ld.org/test-suite/tests/toRdf-0027-in.jsonld";
        loadSource(src);

        JSONLDFormat jf = JSONLDFormat.create(g);
        String output = jf.toString();
        //System.out.println("test context\n"+output);
        Assert.assertTrue("Prefix test:", output.contains("\"http://example.org/\""));
    }

    @Test
    public void testSingleDefaultGraph() {
        loadSource(ROOT + "person.jsonld");

        JSONLDFormat jf = JSONLDFormat.create(g);
        String output = jf.toString();
        //System.out.println("test single dfault " + output);
        Assert.assertTrue("test defalut", output.contains("@graph"));
    }

    @Test
    // 1 test the output format conforming to json-ld (can be parsed by parser)
    // 2 test the graph info is correct ( compare with original graph)
    public void testOutputFormat() throws IOException {
        boolean b1 = validate(ROOT + "product.jsonld", true);
        boolean b2 = validate(ROOT + "person.jsonld", true);
        boolean b3 = validate(ROOT + "library.jsonld", true);
        boolean b4 = validate(ROOT + "recipe.jsonld", true);
        boolean b5 = validate(ROOT + "place.jsonld", true);
        boolean b6 = validate(ROOT + "toRdf-0115-in.jsonld", true);//multiple graphs
        boolean b7 = validate(ROOT + "normalize-0020-in.jsonld", true);
        boolean b8 = validate(ROOT + "compact-0005-in.jsonld", true);
        boolean b9 = validate(ROOT + "n-triples.nt", true);//n-triples
        boolean b90 = validate(ROOT + "n-triples.jsonld", true);//n-triples
        //boolean b10 = validate(ROOT + "toRdf-0070-in.jsonld", true);
        boolean b11 = validate(ROOT + "test.rdf", true);//rdf

        Assert.assertTrue("product.jsonld", b1);
        Assert.assertTrue("person.jsonld", b2);
        Assert.assertTrue("library.jsonld", b3);
        Assert.assertTrue("recipe.jsonld", b4);
        Assert.assertTrue("place.jsonld", b5);
        Assert.assertTrue("toRdf-0115-in.jsonld", b6);
        Assert.assertTrue("normalize-0020-in.jsonld", b7);
        Assert.assertTrue("compact-0005-in.jsonld", b8);
        Assert.assertTrue("n-triples.nt", b9);
        Assert.assertTrue("n-triples.jsonld", b90);
        //Assert.assertTrue("toRdf-0070-in.jsonld", b10);
        Assert.assertTrue("test.rdf", b11);
    }

    @Test
    public void testQuery() {
        loadSource(ROOT + "product.jsonld");

        QueryProcess exec = QueryProcess.create(g);

        // define a SPARQL query
        String q1 = "construct{?x rdf:type ?y} where {?x rdf:type ?y}";

        // Execute a query
        Mappings map = null;
        try {
            map = exec.query(q1);
            JSONLDFormat jf = JSONLDFormat.create(map);
            String output = jf.toString();

            Assert.assertTrue("contains 2 @type:", output.split("@type").length == 3);
        } catch (EngineException ex) {
            fail();
        }
    }

    @Test
    public void testNoGraph() {
        loadSource(ROOT + "product.jsonld");

        QueryProcess exec = QueryProcess.create(g);

        // define a SPARQL query
        String q1 = "select * where {?x ?p ?y}";

        // Execute a query
        Mappings map = null;
        try {
            map = exec.query(q1);
            JSONLDFormat jf = JSONLDFormat.create(map);
            String output = jf.toString();
            Assert.assertTrue("no graph:", output.startsWith("No graph contained in the results.."));
        } catch (EngineException ex) {
            fail();
        }
    }

    @Test
    public void testNullGraph() {
        JSONLDFormat jf = JSONLDFormat.create((Graph) null);
        String output = jf.toString();
        Assert.assertTrue("Grah is null", output.startsWith("No graph contained in the results.."));
    }

    @Test//(timeout = 1000)
    public void testLargeData() throws IOException {
        boolean large = validate(ROOT + "cog-2012.ttl", false);
        Assert.assertTrue("Large data test:", large);
    }
}
