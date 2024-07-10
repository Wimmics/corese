package fr.inria.corese.command.programs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.print.CanonicalRdf10Format;
import picocli.CommandLine;

public class ShaclTest {

    private Shacl shacl = new Shacl();
    private CommandLine cmd = new CommandLine(shacl);

    private StringWriter out = new StringWriter();
    private StringWriter err = new StringWriter();

    private String inputRdfPath = SparqlTest.class.getResource("/fr/inria/corese/command/programs/shacl/inputRdf")
            .getPath();
    private String inputRdfPathRecursive = SparqlTest.class
            .getResource("/fr/inria/corese/command/programs/shacl/inputRdf-Recursive1")
            .getPath();
    private String inputShaclPath = SparqlTest.class.getResource("/fr/inria/corese/command/programs/shacl/inputShacl")
            .getPath();
    private String referencesPath = SparqlTest.class.getResource("/fr/inria/corese/command/programs/shacl/references")
            .getPath();
    private String resultsPath = SparqlTest.class.getResource("/fr/inria/corese/command/programs/shacl/results")
            .getPath();

    private static final String UUID_REGEX = "<urn:uuid:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}>";
    private static final String NEUTRAL_UUID = "<urn:uuid:00000000-0000-0000-0000-000000000000>";

    @Before
    public void setUp() throws Exception {
        PrintWriter out = new PrintWriter(this.out);
        PrintWriter err = new PrintWriter(this.err);
        cmd.setOut(out);
        cmd.setErr(err);
    }

    public boolean compareFiles(String filePath1, String filePath2, int format) throws IOException {
        // Get content of files
        String content1 = getStringContent(filePath1);
        String content2 = getStringContent(filePath2);

        // Remove UUIDs and Blank Nodes
        String clearContent1 = maskUUIDs(content1);
        String clearContent2 = maskUUIDs(content2);

        // Canonicalize RDF content
        String canonicallFile1 = canonicalize(clearContent1, format);
        String canonicallFile2 = canonicalize(clearContent2, format);

        return canonicallFile1.equals(canonicallFile2);
    }

    private String maskUUIDs(String content) {
        content = Pattern.compile(UUID_REGEX).matcher(content).replaceAll(NEUTRAL_UUID);
        return content;
    }

    private String getStringContent(String path) throws IOException {
        return new String(java.nio.file.Files.readAllBytes(Paths.get(path)));
    }

    private String canonicalize(String content, int format) {

        // Content String to Input Stream
        InputStream is = new ByteArrayInputStream(content.getBytes());

        // Load RDF content into a Graph
        Graph graph = Graph.create();
        Load ld = Load.create(graph);

        try {
            ld.parse(is, format);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return Canonical RDF content
        return CanonicalRdf10Format.create(graph).toString();
    }

    @Test
    public void test1RDF1SHACLBeatlesOk() throws IOException {

        String inputRdf = Paths.get(this.inputRdfPath, "beatles-ok.ttl").toString();
        String inputShacl = Paths.get(this.inputShaclPath, "beatles-validator.ttl").toString();

        String expected = Paths.get(this.referencesPath, "beatles-ok.ttl").toString();
        String result = Paths.get(this.resultsPath, "beatles-ok.ttl").toString();

        int exitCode = cmd.execute(
                "-i", inputRdf,
                "-s", inputShacl,
                "-o", result);

        assertEquals(0, exitCode);
        assertEquals("", this.out.toString());
        assertEquals("", this.err.toString());
        assertTrue(this.compareFiles(expected, result, Load.TURTLE_FORMAT));
        assertNotEquals("", result);
    }

    @Test
    public void test1RDF1SHACLBeatlesErr() throws IOException {

        String inputRdf = Paths.get(this.inputRdfPath, "beatles-err.ttl").toString();
        String inputShacl = Paths.get(this.inputShaclPath, "beatles-validator.ttl").toString();

        String expected = Paths.get(this.referencesPath, "beatles-err.ttl").toString();
        String result = Paths.get(this.resultsPath, "beatles-err.ttl").toString();

        int exitCode = cmd.execute(
                "-i", inputRdf,
                "-s", inputShacl,
                "-o", result);

        assertEquals(0, exitCode);
        assertEquals("", this.out.toString());
        assertEquals("", this.err.toString());
        assertTrue(this.compareFiles(expected, result, Load.TURTLE_FORMAT));
        assertNotEquals("", result);
    }

    @Test
    public void test2RDF2SHACLBeatlesOk() throws IOException {

        String inputRdf1 = Paths.get(this.inputRdfPath, "beatles-ok.ttl").toString();
        String inputShacl1 = Paths.get(this.inputShaclPath, "beatles-validator.ttl").toString();

        String inputRdf2 = Paths.get(this.inputRdfPath, "person-ok.ttl").toString();
        String inputShacl2 = Paths.get(this.inputShaclPath, "person-validator.ttl").toString();

        String expected = Paths.get(this.referencesPath, "beatles-person-ok.ttl").toString();
        String result = Paths.get(this.resultsPath, "beatles-person-ok.ttl").toString();

        int exitCode = cmd.execute(
                "-i", inputRdf1,
                "-i", inputRdf2,
                "-s", inputShacl1,
                "-s", inputShacl2,
                "-o", result);

        assertEquals(0, exitCode);
        assertEquals("", this.out.toString());
        assertEquals("", this.err.toString());
        assertTrue(this.compareFiles(expected, result, Load.TURTLE_FORMAT));
        assertNotEquals("", result);
    }

    @Test
    public void test2RDF2SHACLBeatlesErr() throws IOException {

        String inputRdf1 = Paths.get(this.inputRdfPath, "beatles-err.ttl").toString();
        String inputShacl1 = Paths.get(this.inputShaclPath, "beatles-validator.ttl").toString();

        String inputRdf2 = Paths.get(this.inputRdfPath, "person-err.ttl").toString();
        String inputShacl2 = Paths.get(this.inputShaclPath, "person-validator.ttl").toString();

        String expected = Paths.get(this.referencesPath, "beatles-person-err.ttl").toString();
        String result = Paths.get(this.resultsPath, "beatles-person-err.ttl").toString();

        int exitCode = cmd.execute(
                "-i", inputRdf1,
                "-i", inputRdf2,
                "-s", inputShacl1,
                "-s", inputShacl2,
                "-o", result);

        assertEquals(0, exitCode);
        assertEquals("", this.out.toString());
        assertEquals("", this.err.toString());
        assertTrue(this.compareFiles(expected, result, Load.TURTLE_FORMAT));
        assertNotEquals("", result);
    }

    @Test
    public void test1RDFUrl1SHACLBeatlesOk() throws IOException {

        String inputRdf = "https://files.inria.fr/corese/data/unit-test/beatles.ttl";
        String inputShacl = Paths.get(this.inputShaclPath, "beatles-validator.ttl").toString();

        String expected = Paths.get(this.referencesPath, "beatles-ok.ttl").toString();
        String result = Paths.get(this.resultsPath, "beatles-ok.ttl").toString();

        int exitCode = cmd.execute(
                "-i", inputRdf,
                "-s", inputShacl,
                "-o", result);

        assertEquals(0, exitCode);
        assertEquals("", this.out.toString());
        assertEquals("", this.err.toString());
        assertTrue(this.compareFiles(expected, result, Load.TURTLE_FORMAT));
        assertNotEquals("", result);
    }

    @Test
    public void test1RDF1SHACLUrlBeatlesOk() throws IOException {

        String inputRdf = Paths.get(this.inputRdfPath, "beatles-ok.ttl").toString();
        String inputShacl = "https://files.inria.fr/corese/data/unit-test/beatles-validator.ttl";

        String expected = Paths.get(this.referencesPath, "beatles-ok.ttl").toString();
        String result = Paths.get(this.resultsPath, "beatles-ok.ttl").toString();

        int exitCode = cmd.execute(
                "-i", inputRdf,
                "-s", inputShacl,
                "-o", result);

        assertEquals(0, exitCode);
        assertEquals("", this.out.toString());
        assertEquals("", this.err.toString());
        assertTrue(this.compareFiles(expected, result, Load.TURTLE_FORMAT));
        assertNotEquals("", result);
    }

    @Test
    public void testRDFSHACLDirectoryBeatlesErr() throws IOException {

        String inputRdf = Paths.get(this.inputRdfPath).toString();
        String inputShacl = Paths.get(this.inputShaclPath).toString();

        String expected = Paths.get(this.referencesPath, "directory-err.ttl").toString();
        String result = Paths.get(this.resultsPath, "directory-err.ttl").toString();

        int exitCode = cmd.execute(
                "-i", inputRdf,
                "-s", inputShacl,
                "-o", result);

        assertEquals(0, exitCode);
        assertEquals("", this.out.toString());
        assertEquals("", this.err.toString());
        assertTrue(this.compareFiles(expected, result, Load.TURTLE_FORMAT));
        assertNotEquals("", result);
    }

    @Test
    public void test1RDF1SHACLBeatlesOkrdf() throws IOException {

        String inputRdf = Paths.get(this.inputRdfPath, "beatles-ok.rdf").toString();
        String inputShacl = Paths.get(this.inputShaclPath, "beatles-validator.rdf").toString();

        String expected = Paths.get(this.referencesPath, "beatles-ok.rdf").toString();
        String result = Paths.get(this.resultsPath, "beatles-ok.rdf").toString();

        int exitCode = cmd.execute(
                "-i", inputRdf,
                "-if", "rdfxml",
                "-s", inputShacl,
                "-sf", "rdfxml",
                "-o", result,
                "-of", "rdfxml");

        assertEquals(0, exitCode);
        assertEquals("", this.out.toString());
        assertEquals("", this.err.toString());
        assertTrue(this.compareFiles(expected, result, Load.RDFXML_FORMAT));
        assertNotEquals("", result);
    }

    @Test
    public void test1RDF1SHACLBeatlesOkjsonld() throws IOException {

        String inputRdf = Paths.get(this.inputRdfPath, "beatles-ok.jsonld").toString();
        String inputShacl = Paths.get(this.inputShaclPath, "beatles-validator.jsonld").toString();

        String expected = Paths.get(this.referencesPath, "beatles-ok.jsonld").toString();
        String result = Paths.get(this.resultsPath, "beatles-ok.jsonld").toString();

        int exitCode = cmd.execute(
                "-i", inputRdf,
                "-if", "jsonld",
                "-s", inputShacl,
                "-sf", "jsonld",
                "-o", result,
                "-of", "jsonld");

        assertEquals(0, exitCode);
        assertEquals("", this.out.toString());
        assertEquals("", this.err.toString());
        assertTrue(this.compareFiles(expected, result, Load.JSONLD_FORMAT));
        assertNotEquals("", result);
    }

    @Test
    public void testRDFSHACLDirectoryRecursiveBeatlesErr() throws IOException {

        String inputRdf = Paths.get(this.inputRdfPathRecursive).toString();
        String inputShacl = Paths.get(this.inputShaclPath).toString();

        String expected = Paths.get(this.referencesPath, "directory-err.ttl").toString();
        String result = Paths.get(this.resultsPath, "directory-err.ttl").toString();

        int exitCode = cmd.execute(
                "-i", inputRdf,
                "-s", inputShacl,
                "-o", result,
                "-R");

        assertEquals(0, exitCode);
        assertEquals("", this.out.toString());
        assertEquals("", this.err.toString());
        assertTrue(this.compareFiles(expected, result, Load.TURTLE_FORMAT));
        assertNotEquals("", result);
    }

}
