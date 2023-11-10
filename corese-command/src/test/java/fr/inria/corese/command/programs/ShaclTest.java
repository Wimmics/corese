package fr.inria.corese.command.programs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

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
    private static final String BLANK_NODE_REGEX = "_:(b|bb)\\d+";

    @Before
    public void setUp() throws Exception {
        PrintWriter out = new PrintWriter(this.out);
        PrintWriter err = new PrintWriter(this.err);
        cmd.setOut(out);
        cmd.setErr(err);
    }

    public boolean compareFiles(String filePath1, String filePath2) throws IOException {
        String content1 = new String(Files.readAllBytes(Paths.get(filePath1)));
        String content2 = new String(Files.readAllBytes(Paths.get(filePath2)));

        String normalizedContent1 = sort(trimLines(removeUUIDsAndBlankNodes(content1)));
        String normalizedContent2 = sort(trimLines(removeUUIDsAndBlankNodes(content2)));

        return normalizedContent1.equals(normalizedContent2);
    }

    private String sort(String content) {
        String[] lines = content.split("\n");
        Arrays.sort(lines);
        return Arrays.stream(lines).collect(Collectors.joining("\n"));
    }

    private String removeUUIDsAndBlankNodes(String content) {
        content = Pattern.compile(UUID_REGEX).matcher(content).replaceAll("");
        content = Pattern.compile(BLANK_NODE_REGEX).matcher(content).replaceAll("");
        return content;
    }

    private String trimLines(String content) {
        return Arrays.stream(content.split("\n")).map(String::trim).collect(Collectors.joining("\n"));
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
        assertTrue(this.compareFiles(expected, result));
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
        assertTrue(this.compareFiles(expected, result));
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
        assertTrue(this.compareFiles(expected, result));
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
        assertTrue(this.compareFiles(expected, result));
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
        assertTrue(this.compareFiles(expected, result));
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
        assertTrue(this.compareFiles(expected, result));
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
        assertTrue(this.compareFiles(expected, result));
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
        assertTrue(this.compareFiles(expected, result));
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
        assertTrue(this.compareFiles(expected, result));
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
        assertTrue(this.compareFiles(expected, result));
    }

}
