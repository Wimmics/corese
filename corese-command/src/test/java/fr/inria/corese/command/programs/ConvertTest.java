package fr.inria.corese.command.programs;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import picocli.CommandLine;

public class ConvertTest {

    private Convert convert = new Convert();
    private CommandLine cmd = new CommandLine(convert);

    private StringWriter out = new StringWriter();
    private StringWriter err = new StringWriter();

    private String referencesPath = ConvertTest.class.getResource("/fr/inria/corese/command/programs/references/")
            .getPath();
    private String resultPath = ConvertTest.class.getResource("/fr/inria/corese/command/programs/results/")
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
        System.out.println("done");
    }

    @Test
    public void testConvertTurtleToTurtle() {
        String pathRefBeatlesTTL = Paths.get(referencesPath, "beatles.ttl").toString();
        String pathOutBeatlesTTL = Paths.get(resultPath, "ttlbeatles.ttl").toString();

        int exitCode = cmd.execute("-i", pathRefBeatlesTTL, "TURTLE", "-o", pathOutBeatlesTTL);

        String expectedOutput = readFileAsString(pathRefBeatlesTTL);
        String actualOutput = readFileAsString(pathOutBeatlesTTL);

        System.out.println(pathRefBeatlesTTL);
        System.out.println(pathOutBeatlesTTL);

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

}