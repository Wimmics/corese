package fr.inria.corese.command.programs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import fr.inria.corese.command.utils.format.EnumInputFormat;
import fr.inria.corese.command.utils.rdf.RdfDataLoader;
import fr.inria.corese.core.Graph;
import picocli.CommandLine;

public class ConvertTest {

    private Convert convert = new Convert();
    private CommandLine cmd = new CommandLine(convert);

    private StringWriter out = new StringWriter();
    private StringWriter err = new StringWriter();

    private String inputFile = ConvertTest.class
            .getResource("/fr/inria/corese/command/programs/convert/input/")
            .getPath();
    private String referencesPath = ConvertTest.class
            .getResource("/fr/inria/corese/command/programs/convert/references/")
            .getPath();
    private String resultPath = ConvertTest.class
            .getResource("/fr/inria/corese/command/programs/convert/results/")
            .getPath();

    private boolean compareFiles(String filePath1, String filePath2) {
        // Create two sets to store the lines of each file
        Set<String> file1Lines = new HashSet<>();
        Set<String> file2Lines = new HashSet<>();

        try {
            // Read the first file and store each line in the set
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath1))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    file1Lines.add(line);
                }
            }

            // Read the second file and store each line in the set
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath2))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    file2Lines.add(line);
                }
            }

            // Check if both sets are equal
            return file1Lines.equals(file2Lines);

        } catch (IOException e) {
            e.printStackTrace();
            return false; // Return false if an error occurs
        }
    }

    @Before
    public void setUp() throws Exception {
        PrintWriter out = new PrintWriter(this.out);
        PrintWriter err = new PrintWriter(this.err);
        cmd.setOut(out);
        cmd.setErr(err);
    }

    @Test
    public void testConvertTurtleToxml() {
        String pathinputBeatlesTTL = Paths.get(inputFile, "beatles.ttl").toString();
        String pathRefBeatlesXML = Paths.get(referencesPath, "ttl.beatles.rdf").toString();
        String pathOutBeatlesXML = Paths.get(resultPath, "ttl.beatles.rdf").toString();

        int exitCode = cmd.execute("-i", pathinputBeatlesTTL, "-of", "RDFXML", "-o", pathOutBeatlesXML);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesXML, pathOutBeatlesXML));
    }

    @Test
    public void testConvertTurtleToJsonld() {
        String pathInputBeatlesTTL = Paths.get(inputFile, "beatles.ttl").toString();
        String pathRefBeatlesJSON = Paths.get(referencesPath, "ttl.beatles.jsonld").toString();
        String pathOutBeatlesJSON = Paths.get(resultPath, "ttl.beatles.jsonld").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesTTL, "-of", "JSONLD", "-o", pathOutBeatlesJSON);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesJSON, pathOutBeatlesJSON));
    }

    @Test
    public void testConvertTurtleToTrig() {
        String pathInputBeatlesTTL = Paths.get(inputFile, "beatles.ttl").toString();
        String pathRefBeatlesTRIG = Paths.get(referencesPath, "ttl.beatles.trig").toString();
        String pathOutBeatlesTRIG = Paths.get(resultPath, "ttl.beatles.trig").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesTTL, "-of", "TRIG", "-o", pathOutBeatlesTRIG);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesTRIG, pathOutBeatlesTRIG));
    }

    @Test
    public void testConvertTurtleToTurtle() {
        String pathInputBeatlesTTL = Paths.get(inputFile, "beatles.ttl").toString();
        String pathRefBeatlesTTL = Paths.get(referencesPath, "ttl.beatles.ttl").toString();
        String pathOutBeatlesTTL = Paths.get(resultPath, "ttl.beatles.ttl").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesTTL, "-of", "TURTLE", "-o", pathOutBeatlesTTL);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesTTL, pathOutBeatlesTTL));
    }

    @Test
    public void testConvertTurtleToNt() {
        String pathInputBeatlesTTL = Paths.get(inputFile, "beatles.ttl").toString();
        String pathRefBeatlesNT = Paths.get(referencesPath, "ttl.beatles.nt").toString();
        String pathOutBeatlesNT = Paths.get(resultPath, "ttl.beatles.nt").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesTTL, "-of", "NTRIPLES", "-o", pathOutBeatlesNT);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesNT, pathOutBeatlesNT));
    }

    @Test
    public void testConvertTurtleToNq() {
        String pathInputBeatlesTTL = Paths.get(inputFile, "beatles.ttl").toString();
        String pathRefBeatlesNQ = Paths.get(referencesPath, "ttl.beatles.nq").toString();
        String pathOutBeatlesNQ = Paths.get(resultPath, "ttl.beatles.nq").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesTTL, "-of", "NQUADS", "-o", pathOutBeatlesNQ);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesNQ, pathOutBeatlesNQ));
    }

    @Test
    public void testConvertXmltoXml() {
        String pathInputBeatlesXML = Paths.get(inputFile, "beatles.rdf").toString();
        String pathRefBeatlesXML = Paths.get(referencesPath, "rdf.beatles.rdf").toString();
        String pathOutBeatlesXML = Paths.get(resultPath, "rdf.beatles.rdf").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesXML, "-of", "RDFXML", "-o", pathOutBeatlesXML);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesXML, pathOutBeatlesXML));
    }

    @Test
    public void testConvertXmlToJsonld() {
        String pathInputBeatlesXML = Paths.get(inputFile, "beatles.rdf").toString();
        String pathRefBeatlesJSON = Paths.get(referencesPath, "rdf.beatles.jsonld").toString();
        String pathOutBeatlesJSON = Paths.get(resultPath, "rdf.beatles.jsonld").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesXML, "-of", "JSONLD", "-o", pathOutBeatlesJSON);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesJSON, pathOutBeatlesJSON));
    }

    @Test
    public void testConvertXmlToTrig() {
        String pathInputBeatlesXML = Paths.get(inputFile, "beatles.rdf").toString();
        String pathRefBeatlesTRIG = Paths.get(referencesPath, "rdf.beatles.trig").toString();
        String pathOutBeatlesTRIG = Paths.get(resultPath, "rdf.beatles.trig").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesXML, "-of", "TRIG", "-o", pathOutBeatlesTRIG);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesTRIG, pathOutBeatlesTRIG));
    }

    @Test
    public void testConvertXmlTiTurtle() {
        String pathInputBeatlesXML = Paths.get(inputFile, "beatles.rdf").toString();
        String pathRefBeatlesTTL = Paths.get(referencesPath, "rdf.beatles.ttl").toString();
        String pathOutBeatlesTTL = Paths.get(resultPath, "rdf.beatles.ttl").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesXML, "-of", "TURTLE", "-o", pathOutBeatlesTTL);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesTTL, pathOutBeatlesTTL));
    }

    @Test
    public void testConvertXmlToNt() {
        String pathInputBeatlesXML = Paths.get(inputFile, "beatles.rdf").toString();
        String pathRefBeatlesNT = Paths.get(referencesPath, "rdf.beatles.nt").toString();
        String pathOutBeatlesNT = Paths.get(resultPath, "rdf.beatles.nt").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesXML, "-of", "NTRIPLES", "-o", pathOutBeatlesNT);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesNT, pathOutBeatlesNT));
    }

    @Test
    public void testConvertXmlToNq() {
        String pathInputBeatlesXML = Paths.get(inputFile, "beatles.rdf").toString();
        String pathRefBeatlesNQ = Paths.get(referencesPath, "rdf.beatles.nq").toString();
        String pathOutBeatlesNQ = Paths.get(resultPath, "rdf.beatles.nq").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesXML, "-of", "NQUADS", "-o", pathOutBeatlesNQ);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesNQ, pathOutBeatlesNQ));
    }

    @Test
    public void testConvertTrigToXml() {
        String pathInputBeatlesTRIG = Paths.get(inputFile, "beatles.trig").toString();
        String pathRefBeatlesXML = Paths.get(referencesPath, "trig.beatles.rdf").toString();
        String pathOutBeatlesXML = Paths.get(resultPath, "trig.beatles.rdf").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesTRIG, "-of", "RDFXML", "-o", pathOutBeatlesXML);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesXML, pathOutBeatlesXML));
    }

    @Test
    public void testConvertTrigToJsonld() {
        String pathInputBeatlesTRIG = Paths.get(inputFile, "beatles.trig").toString();
        String pathRefBeatlesJSON = Paths.get(referencesPath, "trig.beatles.jsonld").toString();
        String pathOutBeatlesJSON = Paths.get(resultPath, "trig.beatles.jsonld").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesTRIG, "-of", "JSONLD", "-o", pathOutBeatlesJSON);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesJSON, pathOutBeatlesJSON));
    }

    @Test
    public void testConvertTrigToTrig() {
        String pathInputBeatlesTRIG = Paths.get(inputFile, "beatles.trig").toString();
        String pathExpectBeatlesTRIG = Paths.get(referencesPath, "trig.beatles.trig").toString();
        String pathOutBeatlesTRIG = Paths.get(resultPath, "trig.beatles.trig").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesTRIG, "-of", "TRIG", "-o", pathOutBeatlesTRIG);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathExpectBeatlesTRIG, pathOutBeatlesTRIG));
    }

    @Test
    public void testConvertTrigToTurtle() {
        String pathInputBeatlesTRIG = Paths.get(inputFile, "beatles.trig").toString();
        String pathRefBeatlesTTL = Paths.get(referencesPath, "trig.beatles.ttl").toString();
        String pathOutBeatlesTTL = Paths.get(resultPath, "trig.beatles.ttl").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesTRIG, "-of", "TURTLE", "-o", pathOutBeatlesTTL);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesTTL, pathOutBeatlesTTL));
    }

    @Test
    public void testConvertTrigToNt() {
        String pathInputBeatlesTRIG = Paths.get(inputFile, "beatles.trig").toString();
        String pathRefBeatlesNT = Paths.get(referencesPath, "trig.beatles.nt").toString();
        String pathOutBeatlesNT = Paths.get(resultPath, "trig.beatles.nt").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesTRIG, "-of", "NTRIPLES", "-o", pathOutBeatlesNT);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesNT, pathOutBeatlesNT));
    }

    @Test
    public void testConvertTrigToNq() {
        String pathInputBeatlesTRIG = Paths.get(inputFile, "beatles.trig").toString();
        String pathRefBeatlesNQ = Paths.get(referencesPath, "trig.beatles.nq").toString();
        String pathOutBeatlesNQ = Paths.get(resultPath, "trig.beatles.nq").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesTRIG, "-of", "NQUADS", "-o", pathOutBeatlesNQ);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesNQ, pathOutBeatlesNQ));
    }

    @Test
    public void testConvertJsonldToXml() {
        String pathInputBeatlesJSONLD = Paths.get(inputFile, "beatles.jsonld").toString();
        String pathRefBeatlesXML = Paths.get(referencesPath, "jsonld.beatles.rdf").toString();
        String pathOutBeatlesXML = Paths.get(resultPath, "jsonld.beatles.rdf").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesJSONLD, "-of", "RDFXML", "-o", pathOutBeatlesXML);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesXML, pathOutBeatlesXML));
    }

    @Test
    public void testConvertJsonldToJsonld() {
        String pathInputBeatlesJSONLD = Paths.get(inputFile, "beatles.jsonld").toString();
        String pathRefBeatlesJSON = Paths.get(referencesPath, "jsonld.beatles.jsonld").toString();
        String pathOutBeatlesJSON = Paths.get(resultPath, "jsonld.beatles.jsonld").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesJSONLD, "-of", "JSONLD", "-o", pathOutBeatlesJSON);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesJSON, pathOutBeatlesJSON));
    }

    @Test
    public void testConvertJsonldToTrig() {
        String pathInputBeatlesJSONLD = Paths.get(inputFile, "beatles.jsonld").toString();
        String pathRefBeatlesTRIG = Paths.get(referencesPath, "jsonld.beatles.trig").toString();
        String pathOutBeatlesTRIG = Paths.get(resultPath, "jsonld.beatles.trig").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesJSONLD, "-of", "TRIG", "-o", pathOutBeatlesTRIG);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesTRIG, pathOutBeatlesTRIG));
    }

    @Test
    public void testConvertJsonldToTurtle() {
        String pathInputBeatlesJSONLD = Paths.get(inputFile, "beatles.jsonld").toString();
        String pathRefBeatlesTTL = Paths.get(referencesPath, "jsonld.beatles.ttl").toString();
        String pathOutBeatlesTTL = Paths.get(resultPath, "jsonld.beatles.ttl").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesJSONLD, "-of", "TURTLE", "-o", pathOutBeatlesTTL);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesTTL, pathOutBeatlesTTL));
    }

    @Test
    public void testConvertJsonldToNt() {
        String pathInputBeatlesJSONLD = Paths.get(inputFile, "beatles.jsonld").toString();
        String pathRefBeatlesNT = Paths.get(referencesPath, "jsonld.beatles.nt").toString();
        String pathOutBeatlesNT = Paths.get(resultPath, "jsonld.beatles.nt").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesJSONLD, "-of", "NTRIPLES", "-o", pathOutBeatlesNT);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesNT, pathOutBeatlesNT));
    }

    @Test
    public void testConvertJsonldToNq() {
        String pathInputBeatlesJSONLD = Paths.get(inputFile, "beatles.jsonld").toString();
        String pathRefBeatlesNQ = Paths.get(referencesPath, "jsonld.beatles.nq").toString();
        String pathOutBeatlesNQ = Paths.get(resultPath, "jsonld.beatles.nq").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesJSONLD, "-of", "NQUADS", "-o", pathOutBeatlesNQ);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesNQ, pathOutBeatlesNQ));
    }

    @Test
    public void testConvertNtToXml() {
        String pathInputBeatlesNT = Paths.get(inputFile, "beatles.nt").toString();
        String pathRefBeatlesXML = Paths.get(referencesPath, "nt.beatles.rdf").toString();
        String pathOutBeatlesXML = Paths.get(resultPath, "nt.beatles.rdf").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesNT, "-of", "RDFXML", "-o", pathOutBeatlesXML);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesXML, pathOutBeatlesXML));
    }

    @Test
    public void testConvertNtToJsonld() {
        String pathInputBeatlesNT = Paths.get(inputFile, "beatles.nt").toString();
        String pathRefBeatlesJSON = Paths.get(referencesPath, "nt.beatles.jsonld").toString();
        String pathOutBeatlesJSON = Paths.get(resultPath, "nt.beatles.jsonld").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesNT, "-of", "JSONLD", "-o", pathOutBeatlesJSON);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesJSON, pathOutBeatlesJSON));
    }

    @Test
    public void testConvertNtToTrig() {
        String pathInputBeatlesNT = Paths.get(inputFile, "beatles.nt").toString();
        String pathRefBeatlesTRIG = Paths.get(referencesPath, "nt.beatles.trig").toString();
        String pathOutBeatlesTRIG = Paths.get(resultPath, "nt.beatles.trig").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesNT, "-of", "TRIG", "-o", pathOutBeatlesTRIG);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesTRIG, pathOutBeatlesTRIG));
    }

    @Test
    public void testConvertNtToTurtle() {
        String pathInputBeatlesNT = Paths.get(inputFile, "beatles.nt").toString();
        String pathRefBeatlesTTL = Paths.get(referencesPath, "nt.beatles.ttl").toString();
        String pathOutBeatlesTTL = Paths.get(resultPath, "nt.beatles.ttl").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesNT, "-of", "TURTLE", "-o", pathOutBeatlesTTL);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesTTL, pathOutBeatlesTTL));
    }

    @Test
    public void testConvertNtToNt() {
        String pathInputBeatlesNT = Paths.get(inputFile, "beatles.nt").toString();
        String pathRefBeatlesNT = Paths.get(referencesPath, "nt.beatles.nt").toString();
        String pathOutBeatlesNT = Paths.get(resultPath, "nt.beatles.nt").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesNT, "-of", "NTRIPLES", "-o", pathOutBeatlesNT);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesNT, pathOutBeatlesNT));
    }

    @Test
    public void testConvertNtToNq() {
        String pathInputBeatlesNT = Paths.get(inputFile, "beatles.nt").toString();
        String pathRefBeatlesNQ = Paths.get(referencesPath, "nt.beatles.nq").toString();
        String pathOutBeatlesNQ = Paths.get(resultPath, "nt.beatles.nq").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesNT, "-of", "NQUADS", "-o", pathOutBeatlesNQ);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesNQ, pathOutBeatlesNQ));
    }

    @Test
    public void testConvertNqToXml() {
        String pathInputBeatlesNQ = Paths.get(inputFile, "beatles.nq").toString();
        String pathRefBeatlesXML = Paths.get(referencesPath, "nq.beatles.rdf").toString();
        String pathOutBeatlesXML = Paths.get(resultPath, "nq.beatles.rdf").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesNQ, "-of", "RDFXML", "-o", pathOutBeatlesXML);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesXML, pathOutBeatlesXML));
    }

    @Test
    public void testConvertNqToJsonld() {
        String pathInputBeatlesNQ = Paths.get(inputFile, "beatles.nq").toString();
        String pathRefBeatlesJSON = Paths.get(referencesPath, "nq.beatles.jsonld").toString();
        String pathOutBeatlesJSON = Paths.get(resultPath, "nq.beatles.jsonld").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesNQ, "-of", "JSONLD", "-o", pathOutBeatlesJSON);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesJSON, pathOutBeatlesJSON));
    }

    @Test
    public void testConvertNqToTrig() {
        String pathInputBeatlesNQ = Paths.get(inputFile, "beatles.nq").toString();
        String pathRefBeatlesTRIG = Paths.get(referencesPath, "nq.beatles.trig").toString();
        String pathOutBeatlesTRIG = Paths.get(resultPath, "nq.beatles.trig").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesNQ, "-of", "TRIG", "-o", pathOutBeatlesTRIG);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesTRIG, pathOutBeatlesTRIG));
    }

    @Test
    public void testConvertNqToTurtle() {
        String pathInputBeatlesNQ = Paths.get(inputFile, "beatles.nq").toString();
        String pathRefBeatlesTTL = Paths.get(referencesPath, "nq.beatles.ttl").toString();
        String pathOutBeatlesTTL = Paths.get(resultPath, "nq.beatles.ttl").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesNQ, "-of", "TURTLE", "-o", pathOutBeatlesTTL);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesTTL, pathOutBeatlesTTL));
    }

    @Test
    public void testConvertNqToNt() {
        String pathInputBeatlesNQ = Paths.get(inputFile, "beatles.nq").toString();
        String pathRefBeatlesNT = Paths.get(referencesPath, "nq.beatles.nt").toString();
        String pathOutBeatlesNT = Paths.get(resultPath, "nq.beatles.nt").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesNQ, "-of", "NTRIPLES", "-o", pathOutBeatlesNT);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesNT, pathOutBeatlesNT));
    }

    @Test
    public void testConvertNqToNq() {
        String pathInputBeatlesNQ = Paths.get(inputFile, "beatles.nq").toString();
        String pathRefBeatlesNQ = Paths.get(referencesPath, "nq.beatles.nq").toString();
        String pathOutBeatlesNQ = Paths.get(resultPath, "nq.beatles.nq").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesNQ, "-of", "NQUADS", "-o", pathOutBeatlesNQ);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesNQ, pathOutBeatlesNQ));
    }

    @Test
    public void testConvertRdfaToXml() {
        String pathInputStringHtml = Paths.get(inputFile, "beatles.html").toString();
        String pathRefBeatlesXML = Paths.get(referencesPath, "html.beatles.rdf").toString();
        String pathOutBeatlesXML = Paths.get(resultPath, "html.beatles.rdf").toString();

        int exitCode = cmd.execute("-i", pathInputStringHtml, "-of", "RDFXML", "-o", pathOutBeatlesXML);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesXML, pathOutBeatlesXML));
    }

    @Test
    public void testConvertRdfaToJsonld() {
        String pathInputStringHtml = Paths.get(inputFile, "beatles.html").toString();
        String pathRefBeatlesJSON = Paths.get(referencesPath, "html.beatles.jsonld").toString();
        String pathOutBeatlesJSON = Paths.get(resultPath, "html.beatles.jsonld").toString();

        int exitCode = cmd.execute("-i", pathInputStringHtml, "-of", "JSONLD", "-o", pathOutBeatlesJSON);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesJSON, pathOutBeatlesJSON));
    }

    @Test
    public void testConvertRdfaToTrig() {
        String pathInputStringHtml = Paths.get(inputFile, "beatles.html").toString();
        String pathRefBeatlesTRIG = Paths.get(referencesPath, "html.beatles.trig").toString();
        String pathOutBeatlesTRIG = Paths.get(resultPath, "html.beatles.trig").toString();

        int exitCode = cmd.execute("-i", pathInputStringHtml, "-of", "TRIG", "-o", pathOutBeatlesTRIG);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesTRIG, pathOutBeatlesTRIG));
    }

    @Test
    public void testConvertRdfaToTurtle() {
        String pathInputStringHtml = Paths.get(inputFile, "beatles.html").toString();
        String pathRefBeatlesTTL = Paths.get(referencesPath, "html.beatles.ttl").toString();
        String pathOutBeatlesTTL = Paths.get(resultPath, "html.beatles.ttl").toString();

        int exitCode = cmd.execute("-i", pathInputStringHtml, "-of", "TURTLE", "-o", pathOutBeatlesTTL);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesTTL, pathOutBeatlesTTL));
    }

    @Test
    public void testConvertRdfaToNt() {
        String pathInputStringHtml = Paths.get(inputFile, "beatles.html").toString();
        String pathRefBeatlesNT = Paths.get(referencesPath, "html.beatles.nt").toString();
        String pathOutBeatlesNT = Paths.get(resultPath, "html.beatles.nt").toString();

        int exitCode = cmd.execute("-i", pathInputStringHtml, "-of", "NTRIPLES", "-o", pathOutBeatlesNT);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesNT, pathOutBeatlesNT));
    }

    @Test
    public void testConvertRdfaToNq() {
        String pathInputStringHtml = Paths.get(inputFile, "beatles.html").toString();
        String pathRefBeatlesNQ = Paths.get(referencesPath, "html.beatles.nq").toString();
        String pathOutBeatlesNQ = Paths.get(resultPath, "html.beatles.nq").toString();

        int exitCode = cmd.execute("-i", pathInputStringHtml, "-of", "NQUADS", "-o", pathOutBeatlesNQ);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertTrue(compareFiles(pathRefBeatlesNQ, pathOutBeatlesNQ));
    }

    @Test
    public void testConvertWithSameInputAndOutputPath() {
        String inputPath = Paths.get(referencesPath, "beatles.ttl").toString();
        int exitCode = cmd.execute("-i", inputPath, "-of", "TURTLE", "-o", inputPath);
        assertEquals(1, exitCode);
        assertEquals(out.toString(), "");
        assertTrue(err.toString().trim().contains("Input path cannot be the same as output path."));
    }

    @Test
    public void testConvertWithInvalidInputPath() {
        String inputPath = "invalid_path.ttl";
        String outputPath = Paths.get(resultPath, "ttlbeatles.ttl").toString();

        int exitCode = cmd.execute("-i", inputPath, "-of", "TURTLE", "-o", outputPath);
        assertEquals(1, exitCode);
        assertEquals(out.toString(), "");
        assertTrue(err.toString().trim().contains("Failed to open RDF data file:"));
    }

    @Test
    public void testConvertWithInvalidOutputPath() {
        String inputPath = Paths.get(referencesPath, "beatles.ttl").toString();
        String outputPath = "/invalid/path/for/output.ttl";

        int exitCode = cmd.execute("-i", inputPath, "-of", "TURTLE", "-o", outputPath);
        assertEquals(1, exitCode);
        assertEquals(out.toString(), "");
        assertTrue(err.toString().trim().contains("Failed to open RDF data file:"));
    }

    @Test
    public void testGraphUtilsLoadWithInvalidFormat() {
        Path inputPath = Paths.get(referencesPath, "beatles.ttl");

        try {
            RdfDataLoader.loadFromFile(inputPath, EnumInputFormat.JSONLD, Graph.create(), null, false);
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Failed to open RDF data file:"));
        }
    }

}