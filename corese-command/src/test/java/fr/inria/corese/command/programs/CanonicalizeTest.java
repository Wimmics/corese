package fr.inria.corese.command.programs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import picocli.CommandLine;

public class CanonicalizeTest {

    private Canonicalize canonicalize = new Canonicalize();
    private CommandLine cmd = new CommandLine(canonicalize);

    private StringWriter out = new StringWriter();
    private StringWriter err = new StringWriter();

    private String inputPath = CanonicalizeTest.class
            .getResource("/fr/inria/corese/command/programs/canonicalize/input/")
            .getPath();
    private String referencesPath = CanonicalizeTest.class
            .getResource("/fr/inria/corese/command/programs/canonicalize/references/")
            .getPath();
    private String resultPath = CanonicalizeTest.class
            .getResource("/fr/inria/corese/command/programs/canonicalize/results/")
            .getPath();

    @BeforeEach
    public void setUp() {
        PrintWriter out = new PrintWriter(this.out);
        PrintWriter err = new PrintWriter(this.err);
        cmd.setOut(out);
        cmd.setErr(err);
    }

    private String getStringContent(String path) {
        try {
            return new String(java.nio.file.Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test1InputFile() {
        String input = inputPath + "beatles.ttl";
        String expected = referencesPath + "beatles.nq";
        String output = resultPath + "beatles.nq";

        String[] args = { "-i", input, "-a", "rdfc-1.0-sha256", "-o", output };
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertEquals(getStringContent(expected), getStringContent(output));
    }

    @Test
    public void test1Url() {
        String input = "https://files.inria.fr/corese/data/unit-test/beatles.ttl";
        String expected = referencesPath + "beatles.nq";
        String output = resultPath + "beatles.nq";

        String[] args = { "-i", input, "-a", "rdfc-1.0-sha256", "-o", output };
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertEquals(getStringContent(expected), getStringContent(output));
    }

    @Test
    public void test1Directory() {
        String input = inputPath;
        String expected = referencesPath + "beatles.nq";
        String output = resultPath + "beatles.nq";

        String[] args = { "-i", input, "-a", "rdfc-1.0-sha256", "-o", output };
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertEquals(getStringContent(expected), getStringContent(output));
    }

    @Test
    public void test1DirectoryRecursive() {
        String input = inputPath;
        String expected = referencesPath + "recursive.nq";
        String output = resultPath + "recursive.nq";

        String[] args = { "-i", input, "-a", "rdfc-1.0-sha256", "-o", output, "-R" };
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertEquals(getStringContent(expected), getStringContent(output));
    }

    @Test
    public void testMultipleSources() {
        String input1 = inputPath + "beatles.ttl";
        String input2 = Paths.get(inputPath, "recursive-level1", "person.ttl").toString();
        String expected = referencesPath + "multiple.nq";
        String output = resultPath + "multiple.nq";

        String[] args = { "-i", input1, input2, "-a", "rdfc-1.0-sha256", "-o", output };
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertEquals(getStringContent(expected), getStringContent(output));
    }

    @Test
    public void testInputFormat() {
        String input = inputPath + "beatles.ttl";
        String expected = referencesPath + "beatles.nq";
        String output = resultPath + "beatles.nq";

        String[] args = { "-i", input, "-f", "text/turtle", "-a", "rdfc-1.0-sha256", "-o", output };
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertEquals(getStringContent(expected), getStringContent(output));
    }

    @Test
    public void testInputBadFormat() {
        String input = inputPath + "beatles.ttl";
        String output = resultPath + "beatles.nq";

        String[] args = { "-i", input, "-f", "rdfxml", "-a", "rdfc-1.0-sha256", "-o", output };
        int exitCode = cmd.execute(args);

        assertEquals(1, exitCode);
        assertEquals("", out.toString());
        assertTrue(err.toString().contains("Failed to parse RDF file."));
    }

    @Test
    public void testSha384() {
        String input = inputPath + "beatles.ttl";
        String expected = referencesPath + "beatles-sha384.nq";
        String output = resultPath + "beatles-sha384.nq";

        String[] args = { "-i", input, "-a", "rdfc-1.0-sha384", "-o", output };
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertEquals(getStringContent(expected), getStringContent(output));
    }

    @Test
    public void testDefaultAlgorithm() {
        String input = inputPath + "beatles.ttl";
        String expected = referencesPath + "beatles.nq";
        String output = resultPath + "beatles.nq";

        String[] args = { "-i", input, "-o", output };
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertEquals(getStringContent(expected), getStringContent(output));
    }

}
