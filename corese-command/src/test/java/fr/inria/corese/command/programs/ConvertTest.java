package fr.inria.corese.command.programs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import fr.inria.corese.command.utils.GraphUtils;
import fr.inria.corese.command.utils.format.EnumInputFormat;
import picocli.CommandLine;

public class ConvertTest {

    private Convert convert = new Convert();
    private CommandLine cmd = new CommandLine(convert);

    private StringWriter out = new StringWriter();
    private StringWriter err = new StringWriter();

    private String referencesPath = ConvertTest.class
            .getResource("/fr/inria/corese/command/programs/convert/references/")
            .getPath();
    private String resultPath = ConvertTest.class.getResource("/fr/inria/corese/command/programs/convert/results/")
            .getPath();

    @Before
    public void setUp() throws Exception {
        PrintWriter out = new PrintWriter(this.out);
        PrintWriter err = new PrintWriter(this.err);
        cmd.setOut(out);
        cmd.setErr(err);
    }

    private String readFileAsString(String path) {
        Path filePath = Paths.get(path);
        String data = "";
        try {
            data = new String(Files.readAllBytes(filePath));
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return data;
    }

    @Test
    public void testConvertTurtleToxml() {
        String pathRefBeatlesTTL = Paths.get(referencesPath, "beatles.ttl").toString();
        String pathRefBeatlesXML = Paths.get(referencesPath, "beatles.xml").toString();
        String pathOutBeatlesXML = Paths.get(resultPath, "ttlbeatles.xml").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesTTL, "RDFXML", "-o", pathOutBeatlesXML);

        String expectedOutput = readFileAsString(pathRefBeatlesXML);
        String actualOutput = readFileAsString(pathOutBeatlesXML);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertTurtleToJson() {
        String pathRefBeatlesTTL = Paths.get(referencesPath, "beatles.ttl").toString();
        String pathRefBeatlesJSON = Paths.get(referencesPath, "beatles.json").toString();
        String pathOutBeatlesJSON = Paths.get(resultPath, "ttlbeatles.json").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesTTL, "JSONLD", "-o", pathOutBeatlesJSON);

        String expectedOutput = readFileAsString(pathRefBeatlesJSON);
        String actualOutput = readFileAsString(pathOutBeatlesJSON);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertTurtleToTrig() {
        String pathRefBeatlesTTL = Paths.get(referencesPath, "beatles.ttl").toString();
        String pathRefBeatlesTRIG = Paths.get(referencesPath, "beatles.trig").toString();
        String pathOutBeatlesTRIG = Paths.get(resultPath, "ttlbeatles.trig").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesTTL, "TRIG", "-o", pathOutBeatlesTRIG);
        String expectedOutput = readFileAsString(pathRefBeatlesTRIG);
        String actualOutput = readFileAsString(pathOutBeatlesTRIG);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertTurtleToTurtle() {
        String pathRefBeatlesTTL = Paths.get(referencesPath, "beatles.ttl").toString();
        String pathOutBeatlesTTL = Paths.get(resultPath, "ttlbeatles.ttl").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesTTL, "TURTLE", "-o", pathOutBeatlesTTL);

        String expectedOutput = readFileAsString(pathRefBeatlesTTL);
        String actualOutput = readFileAsString(pathOutBeatlesTTL);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertXmltoXml() {
        String pathRefBeatlesXML = Paths.get(referencesPath, "beatles.xml").toString();
        String pathOutBeatlesXML = Paths.get(resultPath, "beatles.xml").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesXML, "RDFXML", "-o", pathOutBeatlesXML);

        String expectedOutput = readFileAsString(pathRefBeatlesXML);
        String actualOutput = readFileAsString(pathOutBeatlesXML);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertXmlToJson() {
        String pathRefBeatlesXML = Paths.get(referencesPath, "beatles.xml").toString();
        String pathRefBeatlesJSON = Paths.get(referencesPath, "beatles.json").toString();
        String pathOutBeatlesJSON = Paths.get(resultPath, "beatles.json").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesXML, "JSONLD", "-o", pathOutBeatlesJSON);

        String expectedOutput = readFileAsString(pathRefBeatlesJSON);
        String actualOutput = readFileAsString(pathOutBeatlesJSON);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertXmlToTrig() {
        String pathRefBeatlesXML = Paths.get(referencesPath, "beatles.xml").toString();
        String pathRefBeatlesTRIG = Paths.get(referencesPath, "beatles.trig").toString();
        String pathOutBeatlesTRIG = Paths.get(resultPath, "beatles.trig").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesXML, "TRIG", "-o", pathOutBeatlesTRIG);

        String expectedOutput = readFileAsString(pathRefBeatlesTRIG);
        String actualOutput = readFileAsString(pathOutBeatlesTRIG);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertXmlTiTurtle() {
        String pathRefBeatlesXML = Paths.get(referencesPath, "beatles.xml").toString();
        String pathRefBeatlesTTL = Paths.get(referencesPath, "beatles.ttl").toString();
        String pathOutBeatlesTTL = Paths.get(resultPath, "beatles.ttl").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesXML, "TURTLE", "-o", pathOutBeatlesTTL);

        String expectedOutput = readFileAsString(pathRefBeatlesTTL);
        String actualOutput = readFileAsString(pathOutBeatlesTTL);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertTrigToXml() {
        String pathRefBeatlesTRIG = Paths.get(referencesPath, "beatles.trig").toString();
        String pathRefBeatlesXML = Paths.get(referencesPath, "beatles.xml").toString();
        String pathOutBeatlesXML = Paths.get(resultPath, "trigbeatles.xml").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesTRIG, "RDFXML", "-o", pathOutBeatlesXML);

        String expectedOutput = readFileAsString(pathRefBeatlesXML);
        String actualOutput = readFileAsString(pathOutBeatlesXML);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertTrigToJson() {
        String pathRefBeatlesTRIG = Paths.get(referencesPath, "beatles.trig").toString();
        String pathRefBeatlesJSON = Paths.get(referencesPath, "beatles.json").toString();
        String pathOutBeatlesJSON = Paths.get(resultPath, "trigbeatles.json").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesTRIG, "JSONLD", "-o", pathOutBeatlesJSON);

        String expectedOutput = readFileAsString(pathRefBeatlesJSON);
        String actualOutput = readFileAsString(pathOutBeatlesJSON);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertTrigToTrig() {
        String pathRefBeatlesTRIG = Paths.get(referencesPath, "beatles.trig").toString();
        String pathOutBeatlesTRIG = Paths.get(resultPath, "trigbeatles.trig").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesTRIG, "TRIG", "-o", pathOutBeatlesTRIG);

        String expectedOutput = readFileAsString(pathRefBeatlesTRIG);
        String actualOutput = readFileAsString(pathOutBeatlesTRIG);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertTrigToTurtle() {
        String pathRefBeatlesTRIG = Paths.get(referencesPath, "beatles.trig").toString();
        String pathRefBeatlesTTL = Paths.get(referencesPath, "beatles.ttl").toString();
        String pathOutBeatlesTTL = Paths.get(resultPath, "trigbeatles.ttl").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesTRIG, "TURTLE", "-o", pathOutBeatlesTTL);

        String expectedOutput = readFileAsString(pathRefBeatlesTTL);
        String actualOutput = readFileAsString(pathOutBeatlesTTL);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertJsonToJson() {
        String pathRefBeatlesJSON = Paths.get(referencesPath, "beatles.json").toString();
        String pathOutBeatlesJSON = Paths.get(resultPath, "jsonbeatles.json").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesJSON, "JSONLD", "-o", pathOutBeatlesJSON);

        String expectedOutput = readFileAsString(pathRefBeatlesJSON);
        String actualOutput = readFileAsString(pathOutBeatlesJSON);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertJsonToTurtle() {
        String pathRefBeatlesJSON = Paths.get(referencesPath, "beatles.json").toString();
        String pathRefBeatlesTTL = Paths.get(referencesPath, "beatles.ttl").toString();
        String pathOutBeatlesTTL = Paths.get(resultPath, "jsonbeatles.ttl").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesJSON, "TURTLE", "-o", pathOutBeatlesTTL);

        String expectedOutput = readFileAsString(pathRefBeatlesTTL);
        String actualOutput = readFileAsString(pathOutBeatlesTTL);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertJsonToXml() {
        String pathRefBeatlesJSON = Paths.get(referencesPath, "beatles.json").toString();
        String pathRefBeatlesXML = Paths.get(referencesPath, "beatles.xml").toString();
        String pathOutBeatlesXML = Paths.get(resultPath, "jsonbeatles.xml").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesJSON, "RDFXML", "-o", pathOutBeatlesXML);

        String expectedOutput = readFileAsString(pathRefBeatlesXML);
        String actualOutput = readFileAsString(pathOutBeatlesXML);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertJsonToTrig() {
        String pathRefBeatlesJSON = Paths.get(referencesPath, "beatles.json").toString();
        String pathRefBeatlesTRIG = Paths.get(referencesPath, "beatles.trig").toString();
        String pathOutBeatlesTRIG = Paths.get(resultPath, "jsonbeatles.trig").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesJSON, "TRIG", "-o", pathOutBeatlesTRIG);

        String expectedOutput = readFileAsString(pathRefBeatlesTRIG);
        String actualOutput = readFileAsString(pathOutBeatlesTRIG);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertN3ToXml() {
        String pathRefBeatlesN3 = Paths.get(referencesPath, "beatles.n3").toString();
        String pathRefBeatlesXML = Paths.get(referencesPath, "beatles.xml").toString();
        String pathOutBeatlesXML = Paths.get(resultPath, "n3beatles.xml").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesN3, "RDFXML", "-o", pathOutBeatlesXML);

        String expectedOutput = readFileAsString(pathRefBeatlesXML);
        String actualOutput = readFileAsString(pathOutBeatlesXML);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertN3ToJson() {
        String pathRefBeatlesN3 = Paths.get(referencesPath, "beatles.n3").toString();
        String pathRefBeatlesJSON = Paths.get(referencesPath, "beatles.json").toString();
        String pathOutBeatlesJSON = Paths.get(resultPath, "n3beatles.json").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesN3, "JSONLD", "-o", pathOutBeatlesJSON);

        String expectedOutput = readFileAsString(pathRefBeatlesJSON);
        String actualOutput = readFileAsString(pathOutBeatlesJSON);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertN3ToTrig() {
        String pathRefBeatlesN3 = Paths.get(referencesPath, "beatles.n3").toString();
        String pathRefBeatlesTRIG = Paths.get(referencesPath, "beatles.trig").toString();
        String pathOutBeatlesTRIG = Paths.get(resultPath, "n3beatles.trig").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesN3, "TRIG", "-o", pathOutBeatlesTRIG);

        String expectedOutput = readFileAsString(pathRefBeatlesTRIG);
        String actualOutput = readFileAsString(pathOutBeatlesTRIG);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertN3ToTurtle() {
        String pathRefBeatlesN3 = Paths.get(referencesPath, "beatles.n3").toString();
        String pathRefBeatlesTTL = Paths.get(referencesPath, "beatles.ttl").toString();
        String pathOutBeatlesTTL = Paths.get(resultPath, "n3beatles.ttl").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesN3, "TURTLE", "-o", pathOutBeatlesTTL);

        String expectedOutput = readFileAsString(pathRefBeatlesTTL);
        String actualOutput = readFileAsString(pathOutBeatlesTTL);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertNTriplesToXml() {
        String pathRefBeatlesNT = Paths.get(referencesPath, "beatles.nt").toString();
        String pathRefBeatlesXML = Paths.get(referencesPath, "beatles.xml").toString();
        String pathOutBeatlesXML = Paths.get(resultPath, "ntbeatles.xml").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesNT, "RDFXML", "-o", pathOutBeatlesXML);

        String expectedOutput = readFileAsString(pathRefBeatlesXML);
        String actualOutput = readFileAsString(pathOutBeatlesXML);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertNTriplesToJson() {
        String pathRefBeatlesNT = Paths.get(referencesPath, "beatles.nt").toString();
        String pathRefBeatlesJSON = Paths.get(referencesPath, "beatles.json").toString();
        String pathOutBeatlesJSON = Paths.get(resultPath, "ntbeatles.json").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesNT, "JSONLD", "-o", pathOutBeatlesJSON);

        String expectedOutput = readFileAsString(pathRefBeatlesJSON);
        String actualOutput = readFileAsString(pathOutBeatlesJSON);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertNTriplesToTrig() {
        String pathRefBeatlesNT = Paths.get(referencesPath, "beatles.nt").toString();
        String pathRefBeatlesTRIG = Paths.get(referencesPath, "beatles.trig").toString();
        String pathOutBeatlesTRIG = Paths.get(resultPath, "ntbeatles.trig").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesNT, "TRIG", "-o", pathOutBeatlesTRIG);

        String expectedOutput = readFileAsString(pathRefBeatlesTRIG);
        String actualOutput = readFileAsString(pathOutBeatlesTRIG);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertNTriplesToTurtle() {
        String pathRefBeatlesNT = Paths.get(referencesPath, "beatles.nt").toString();
        String pathRefBeatlesTTL = Paths.get(referencesPath, "beatles.ttl").toString();
        String pathOutBeatlesTTL = Paths.get(resultPath, "ntbeatles.ttl").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesNT, "TURTLE", "-o", pathOutBeatlesTTL);

        String expectedOutput = readFileAsString(pathRefBeatlesTTL);
        String actualOutput = readFileAsString(pathOutBeatlesTTL);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertNQuadsToXml() {
        String pathRefBeatlesNQ = Paths.get(referencesPath, "beatles.nq").toString();
        String pathRefBeatlesXML = Paths.get(referencesPath, "beatles.xml").toString();
        String pathOutBeatlesXML = Paths.get(resultPath, "nqbeatles.xml").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesNQ, "RDFXML", "-o", pathOutBeatlesXML);

        String expectedOutput = readFileAsString(pathRefBeatlesXML);
        String actualOutput = readFileAsString(pathOutBeatlesXML);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertNQuadsToJson() {
        String pathRefBeatlesNQ = Paths.get(referencesPath, "beatles.nq").toString();
        String pathRefBeatlesJSON = Paths.get(referencesPath, "beatles.json").toString();
        String pathOutBeatlesJSON = Paths.get(resultPath, "nqbeatles.json").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesNQ, "JSONLD", "-o", pathOutBeatlesJSON);

        String expectedOutput = readFileAsString(pathRefBeatlesJSON);
        String actualOutput = readFileAsString(pathOutBeatlesJSON);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertNQuadsToTrig() {
        String pathRefBeatlesNQ = Paths.get(referencesPath, "beatles.nq").toString();
        String pathRefBeatlesTRIG = Paths.get(referencesPath, "beatles2.trig").toString();
        String pathOutBeatlesTRIG = Paths.get(resultPath, "nqbeatles.trig").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesNQ, "TRIG", "-o", pathOutBeatlesTRIG);

        String expectedOutput = readFileAsString(pathRefBeatlesTRIG);
        String actualOutput = readFileAsString(pathOutBeatlesTRIG);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertNQuadsToTurtle() {
        String pathRefBeatlesNQ = Paths.get(referencesPath, "beatles.nq").toString();
        String pathRefBeatlesTTL = Paths.get(referencesPath, "beatles.ttl").toString();
        String pathOutBeatlesTTL = Paths.get(resultPath, "nqbeatles.ttl").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesNQ, "TURTLE", "-o", pathOutBeatlesTTL);

        String expectedOutput = readFileAsString(pathRefBeatlesTTL);
        String actualOutput = readFileAsString(pathOutBeatlesTTL);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertRdfaToXml() {
        String pathRefBeatlesRdfa = Paths.get(referencesPath, "beatles.html").toString();
        String pathRefBeatlesXML = Paths.get(referencesPath, "beatles.xml").toString();
        String pathOutBeatlesXML = Paths.get(resultPath, "rdfabeatles.xml").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesRdfa, "RDFXML", "-o", pathOutBeatlesXML);

        String expectedOutput = readFileAsString(pathRefBeatlesXML);
        String actualOutput = readFileAsString(pathOutBeatlesXML);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertRdfaToJson() {
        String pathRefBeatlesRdfa = Paths.get(referencesPath, "beatles.html").toString();
        String pathRefBeatlesJSON = Paths.get(referencesPath, "beatles.json").toString();
        String pathOutBeatlesJSON = Paths.get(resultPath, "rdfabeatles.json").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesRdfa, "JSONLD", "-o", pathOutBeatlesJSON);

        String expectedOutput = readFileAsString(pathRefBeatlesJSON);
        String actualOutput = readFileAsString(pathOutBeatlesJSON);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertRdfaToTrig() {
        String pathRefBeatlesRdfa = Paths.get(referencesPath, "beatles.html").toString();
        String pathRefBeatlesTRIG = Paths.get(referencesPath, "beatles.trig").toString();
        String pathOutBeatlesTRIG = Paths.get(resultPath, "rdfabeatles.trig").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesRdfa, "TRIG", "-o", pathOutBeatlesTRIG);

        String expectedOutput = readFileAsString(pathRefBeatlesTRIG);
        String actualOutput = readFileAsString(pathOutBeatlesTRIG);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertRdfaToTurtle() {
        String pathRefBeatlesRdfa = Paths.get(referencesPath, "beatles.html").toString();
        String pathRefBeatlesTTL = Paths.get(referencesPath, "beatles.ttl").toString();
        String pathOutBeatlesTTL = Paths.get(resultPath, "rdfabeatles.ttl").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesRdfa, "TURTLE", "-o", pathOutBeatlesTTL);

        String expectedOutput = readFileAsString(pathRefBeatlesTTL);
        String actualOutput = readFileAsString(pathOutBeatlesTTL);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testConvertWithSameInputAndOutputPath() {
        String inputPath = Paths.get(referencesPath, "beatles.ttl").toString();
        int exitCode = cmd.execute("-i", inputPath, "TURTLE", "-o", inputPath);
        assertEquals(1, exitCode);
        assertEquals(out.toString(), "");
        assertTrue(err.toString().trim().startsWith("Input path cannot be the same as output path."));
    }

    @Test
    public void testConvertWithInvalidInputPath() {
        String inputPath = "invalid_path.ttl";
        String outputPath = Paths.get(resultPath, "ttlbeatles.ttl").toString();

        int exitCode = cmd.execute("-i", inputPath, "TURTLE", "-o", outputPath);
        assertEquals(1, exitCode);
        assertEquals(out.toString(), "");
        assertTrue(err.toString().trim()
                .startsWith("Error while loading : invalid_path.ttl (No such file or directory)"));
    }

    @Test
    public void testConvertWithInvalidOutputPath() {
        String inputPath = Paths.get(referencesPath, "beatles.ttl").toString();
        String outputPath = "/invalid/path/for/output.ttl";

        int exitCode = cmd.execute("-i", inputPath, "TURTLE", "-o", outputPath);
        assertEquals(1, exitCode);
        assertEquals(out.toString(), "");
        assertTrue(err.toString().trim().startsWith(
                "/invalid/path/for/output.ttl (No such file or directory)"));
    }

    @Test
    public void testGraphUtilsLoadWithInvalidFormat() {
        InputStream input = new ByteArrayInputStream("<rdf></rdf>".getBytes());
        try {
            GraphUtils.load(input, EnumInputFormat.JSONLD);
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Failed to parse RDF file.", e.getMessage());
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }

}